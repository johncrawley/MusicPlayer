package com.jacstuff.musicplayer.service;


import static com.jacstuff.musicplayer.service.MediaNotificationManager.NOTIFICATION_ID;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;


import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.TimeConverter;
import com.jacstuff.musicplayer.db.track.Track;
import com.jacstuff.musicplayer.playlist.PlaylistManager;
import com.jacstuff.musicplayer.playlist.PlaylistManagerImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MediaPlayerService extends Service {

    public static final String ACTION_PLAY = "com.j.crawley.music_player.play";
    public static final String ACTION_PAUSE_PLAYER = "com.j.crawley.music_player.pausePlayer";
    public static final String ACTION_STOP_PLAYER = "com.j.crawley.music_player.stopPlayer";
    public static final String ACTION_REQUEST_STATUS = "com.j.crawley.music_player.requestStatus";

    public static final String ACTION_SELECT_PREVIOUS_TRACK = "com.j.crawley.music_player.selectPreviousTrack";
    public static final String ACTION_SELECT_NEXT_TRACK = "com.j.crawley.music_player.selectNextTrack";
    public static final String ACTION_NOTIFY_VIEW_OF_STOP = "com.j.crawley.music_player.notifyViewOfStop";
    public static final String ACTION_NOTIFY_VIEW_OF_CONNECTING = "com.j.crawley.music_player.notifyViewOfPlay";
    public static final String ACTION_NOTIFY_VIEW_OF_PLAYING = "com.j.crawley.music_player.notifyViewOfPlayInfo";
    public static final String ACTION_NOTIFY_VIEW_OF_ERROR = "com.j.crawley.music_player.notifyViewOfError";


    private MediaPlayer mediaPlayer;
    public boolean hasEncounteredError;
    boolean wasInfoFound = false;
    private MediaNotificationManager mediaNotificationManager;
    private final ScheduledExecutorService executorService;
    Map<BroadcastReceiver, String> broadcastReceiverMap;
    private enum MediaPlayerState { PAUSED, PLAYING, STOPPED, FINISHED}
    private MediaPlayerState currentState = MediaPlayerState.STOPPED;
    private MainActivity mainActivity;
    private List<Track> tracks;
    private PlaylistManager playlistManager;
    private boolean isScanningForTracks;
    private final IBinder binder = new LocalBinder();
    private Track currentTrack;
    private boolean shouldNextTrackPlayAfterCurrentTrackEnds = true;
    private ScheduledFuture<?> updateElapsedTimeFuture;


    public MediaPlayerService() {
        executorService = Executors.newScheduledThreadPool(3);
        tracks = new ArrayList<>();
    }


    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    public void scanForTracks(){
        if(isScanningForTracks){
            return;
        }
        executorService.execute(()->{
            isScanningForTracks = true;
            playlistManager.addTracksFromStorage();
            updateViewTrackList();
            isScanningForTracks = false;
        });
    }


    private void handleNullPathname(){
        if(currentTrack.getPathname() == null){
            mainActivity.setBlankTrackInfo();
            currentState = MediaPlayerState.STOPPED;
        }
    }


    public void displayPlaylistRefreshedMessage(int numberOfNewTracks){
        mainActivity.displayPlaylistRefreshedMessage(numberOfNewTracks);
    }


    public void stop(){
        stop(true);
    }


    public void stop(boolean shouldUpdateMainView){
        if(currentState == MediaPlayerState.PLAYING || currentState == MediaPlayerState.PAUSED) {
            mediaPlayer.stop();
            currentState = MediaPlayerState.STOPPED;
            mediaPlayer.reset();
        }
        stopUpdatingElapsedTimeOnView();
        if(shouldUpdateMainView) {
            mainActivity.notifyPlayerStopped();
        }
    }


    public void initPlaylistAndRefresh(){
        executorService.execute(() -> {
            playlistManager.init();
            loadNextTrack();
            mainActivity.enableControls();
            updateViewTrackList();
        });
    }


    private void updateViewTrackList(){
        mainActivity.updateTracksList(playlistManager.getTracks(), playlistManager.getCurrentTrackIndex());
    }


    public List<Track> getTrackList(){
        return playlistManager.getTracks();
    }


    public void selectTrack(int index){
        assignTrack(playlistManager.selectTrack(index));
    }


    public void loadNextTrack(){
        loadTrack(playlistManager.getNextTrack());
    }


    public void loadPreviousTrack(){
        loadTrack(playlistManager.getPreviousTrack());
    }


    public void loadTrack(Track track){
        assignTrack(track);
        mainActivity.scrollToPosition(track.getIndex());
        mediaNotificationManager.updateNotification();
    }


    public void stopAfterTrackFinishes(){
        if(currentState == MediaPlayerState.PLAYING) {
            shouldNextTrackPlayAfterCurrentTrackEnds = false;
        }
    }


    private void assignTrack(Track track){
        currentTrack = track;
        if(currentTrack == null){
            return;
        }
        if(currentTrack.getPathname() == null) {
            handleNullPathname();
            return;
        }
        mainActivity.setTrackInfoOnView(currentTrack);
        selectTrack(currentTrack);
    }


    private void stopRunningMediaPlayer(){
        currentState = MediaPlayerState.STOPPED;
        if(mediaPlayer != null){
            if(mediaPlayer.isPlaying()){
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
        }
    }


    public void selectTrack(Track track){
        MediaPlayerState oldState = currentState;
        if(currentState == MediaPlayerState.PLAYING || currentState == MediaPlayerState.PAUSED){
            stop(false);
        }
        currentTrack = track;
        if(oldState == MediaPlayerState.PLAYING){
            play();
        }
    }


    public void setActivity(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        playlistManager = new PlaylistManagerImpl(mainActivity.getApplicationContext());
        tracks = playlistManager.getTracks();
        mainActivity.onServiceReady(tracks);
    }


    boolean isPlaying(){
        return currentState == MediaPlayerState.PLAYING;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        createMediaPlayer();
        setupBroadcastReceivers();
        mediaNotificationManager = new MediaNotificationManager(getApplicationContext(), this);
        moveToForeground();
    }


    private void createMediaPlayer(){
        mediaPlayer = new MediaPlayer();
        log("Entered createMediaPlayer()");
        mediaPlayer.setOnCompletionListener(this::onTrackFinished);
    }


    private void onTrackFinished(MediaPlayer mediaPlayer){
        currentState = MediaPlayerState.FINISHED;
        stopUpdatingElapsedTimeOnView();
        mediaPlayer.reset();
        loadNextTrack();
        if(shouldNextTrackPlayAfterCurrentTrackEnds) {
            play();
        }else{
            stop();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBroadcastReceivers();
        releaseMediaPlayerAndLocks();
        stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        mediaNotificationManager.dismissNotification();
        this.stopSelf();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return Service.START_NOT_STICKY; // service is not restarted when terminated
    }


    private void setupBroadcastReceivers(){
        setupBroadcastReceiversMap();
        registerBroadcastReceivers();
    }


    private void setupBroadcastReceiversMap(){
        broadcastReceiverMap = new HashMap<>();
        broadcastReceiverMap.put(serviceReceiverForPlay, ACTION_PLAY);
        broadcastReceiverMap.put(serviceReceiverForRequestStatus,       ACTION_REQUEST_STATUS);
        broadcastReceiverMap.put(serviceReceiverForPause, ACTION_PAUSE_PLAYER);
        broadcastReceiverMap.put(serviceReceiverForNext, ACTION_SELECT_NEXT_TRACK);
        broadcastReceiverMap.put(serviceReceiverForPrevious, ACTION_SELECT_PREVIOUS_TRACK);
    }


    public void playTrack(){
        if(currentState == MediaPlayerState.STOPPED){
            play();
        }
        else if(currentState == MediaPlayerState.PAUSED){
            resume();
        }
    }


    public void startUpdatingElapsedTimeOnView(){
        updateElapsedTimeFuture = executorService.scheduleAtFixedRate(this::updateElapsedTimeOnView, 0L, 200L, TimeUnit.MILLISECONDS);
    }


    private void stopUpdatingElapsedTimeOnView(){
        if(updateElapsedTimeFuture == null){
            return;
        }
        updateElapsedTimeFuture.cancel(false);
    }


    private void updateElapsedTimeOnView(){
        mainActivity.setElapsedTime(TimeConverter.convert(mediaPlayer.getCurrentPosition()));
    }


    public int getNumberOfTracks(){
        return playlistManager.getNumberOfTracks();
    }


    private void releaseMediaPlayerAndLocks(){
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }


    private void moveToForeground(){
        mediaNotificationManager.init();
        Notification notification = mediaNotificationManager.createNotification(getCurrentStatus(), "");
        startForeground(NOTIFICATION_ID, notification);
    }


    String getCurrentStatus(){
        int resId = R.string.status_ready;
        if(hasEncounteredError){
            resId = R.string.status_error;
        }
        else if(currentState == MediaPlayerState.PLAYING){
            resId = R.string.status_playing;
        }
        else if(currentState == MediaPlayerState.PAUSED){
            resId = R.string.status_paused;
        }
        return getApplicationContext().getString(resId);
    }


    String getCurrentTrackName(){
        return currentTrack == null ? "" : currentTrack.getName();
    }


    String getCurrentUrl(){
        return currentTrack == null ? "" : currentTrack.getPathname();
    }


    int getTrackCount(){
        return tracks.size();
    }


    public void play() {
        updateViewsForConnecting();
        stopRunningMediaPlayer();
        shouldNextTrackPlayAfterCurrentTrackEnds = true;
        executorService.schedule(this::startTrack, 1, TimeUnit.MILLISECONDS);
    }


    private void startTrack(){
        hasEncounteredError = false;
        try {
            setCpuWakeLock();
            mediaPlayer.setDataSource(currentTrack.getPathname());
            mediaPlayer.prepare();
            mediaPlayer.start();
            startUpdatingElapsedTimeOnView();
            currentState = MediaPlayerState.PLAYING;
            mainActivity.notifyPlayerPlaying();
            mediaNotificationManager.updateNotification();
        }catch (IOException e){
            e.printStackTrace();
            currentState = MediaPlayerState.STOPPED;
        }
    }


    public void resume(){
            currentState = MediaPlayerState.PLAYING;
            mediaPlayer.start();
            startUpdatingElapsedTimeOnView();
            mainActivity.notifyPlayerPlaying();
            mediaNotificationManager.updateNotification();
    }


    private void updateViewsForConnecting(){
        sendBroadcast(ACTION_NOTIFY_VIEW_OF_CONNECTING);
        wasInfoFound = false;
        mediaNotificationManager.updateNotification();
    }


    private void setCpuWakeLock(){
        if (checkSelfPermission(Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED) {
            mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        }
    }


    private void setupOnInfoListener(){
        mediaPlayer.setOnInfoListener((mediaPlayer, i, i1) -> {
            updateStatusFromConnectingToPlaying();
            return false;
        });
    }


    private void updateStatusFromConnectingToPlaying(){
        if(!wasInfoFound){
            sendBroadcast(ACTION_NOTIFY_VIEW_OF_PLAYING);
            wasInfoFound = true;
            mediaNotificationManager.updateNotification();
        }
    }


    private void setupOnErrorListener(){
        mediaPlayer.setOnErrorListener((mediaPlayer, i, i1) -> {
            stopPlayer();
            handleConnectionError();
            return false;
        });
    }


    private void handleConnectionError(){
        hasEncounteredError = true;
        mediaNotificationManager.updateNotification();
        sendBroadcast(ACTION_NOTIFY_VIEW_OF_ERROR);
    }


    private void stopPlayer(){
        releaseAndResetMediaPlayer();
        wasInfoFound = false;
        mediaNotificationManager.updateNotification();
    }


    public void pause(){
        pauseMediaPlayer();
        wasInfoFound = false;
        mediaNotificationManager.updateNotification();
        mainActivity.notifyMediaPlayerPaused();
    }


    private void pauseMediaPlayer(){
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            stopUpdatingElapsedTimeOnView();
            currentState = MediaPlayerState.PAUSED;
        }
        else{
            log("Media player was either null or not playing");
        }
    }


    private void releaseAndResetMediaPlayer(){
        try {
            if (mediaPlayer != null) {
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }catch (RuntimeException e){
            log("releaseAndResetMediaPlayerAndWifiLock() exception:  " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void sendBroadcast(String action){
        sendBroadcast(new Intent(action));
    }


    private void log(String msg){
        System.out.println("^^^ MediaPlayerService: " +  msg);
    }


    private void registerBroadcastReceivers(){
        for(BroadcastReceiver bcr : broadcastReceiverMap.keySet()){
            IntentFilter intentFilter = new IntentFilter(broadcastReceiverMap.get(bcr));
            registerReceiver(bcr, intentFilter);
        }
    }


    private void unregisterBroadcastReceivers(){
        for(BroadcastReceiver bcr : broadcastReceiverMap.keySet()){
            unregisterReceiver(bcr);
        }
    }


    private final BroadcastReceiver serviceReceiverForPlay = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(currentState == MediaPlayerState.PAUSED){
                resume();
            }
            else if(currentState == MediaPlayerState.STOPPED) {
                play();
            }
        }
    };


    private final BroadcastReceiver serviceReceiverForNext = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadNextTrack();
        }
    };


    private final BroadcastReceiver serviceReceiverForPrevious = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadPreviousTrack();
        }
    };


    private final BroadcastReceiver serviceReceiverForRequestStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String broadcast = currentState == MediaPlayerState.PLAYING ? ACTION_NOTIFY_VIEW_OF_PLAYING : ACTION_NOTIFY_VIEW_OF_STOP;
            sendBroadcast(broadcast);
        }
    };


    private final BroadcastReceiver serviceReceiverForPause = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pause();
        }
    };


}
