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
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;


import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.db.track.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MediaPlayerService extends Service {

    public static final String ACTION_START_PLAYER = "com.j.crawley.music_player.startPlayer";
    public static final String ACTION_PLAY = "com.j.crawley.music_player.play";
    public static final String ACTION_PAUSE_PLAYER = "com.j.crawley.music_player.pausePlayer";
    public static final String ACTION_STOP_PLAYER = "com.j.crawley.music_player.stopPlayer";
    public static final String ACTION_CHANGE_TRACK = "com.j.crawley.music_player.changeTrack";
    public static final String ACTION_REQUEST_STATUS = "com.j.crawley.music_player.requestStatus";
    public static final String ACTION_UPDATE_STATION_COUNT = "com.j.crawley.music_player.updateStationCount";

    public static final String ACTION_SELECT_PREVIOUS_TRACK = "com.j.crawley.music_player.selectPreviousTrack";
    public static final String ACTION_SELECT_NEXT_TRACK = "com.j.crawley.music_player.selectNextTrack";
    public static final String ACTION_NOTIFY_VIEW_OF_STOP = "com.j.crawley.music_player.notifyViewOfStop";
    public static final String ACTION_NOTIFY_VIEW_OF_PAUSE = "com.j.crawley.music_player.notifyViewOfPause";
    public static final String ACTION_NOTIFY_VIEW_OF_CONNECTING = "com.j.crawley.music_player.notifyViewOfPlay";
    public static final String ACTION_NOTIFY_VIEW_OF_PLAYING = "com.j.crawley.music_player.notifyViewOfPlayInfo";
    public static final String ACTION_NOTIFY_VIEW_OF_ERROR = "com.j.crawley.music_player.notifyViewOfError";

    public static final String TAG_TRACK_URL = "track_url";
    public static final String TAG_TRACK_NAME = "track_name";
    public static final String TAG_ARTIST_NAME = "artist_name";
    public static final String TAG_STATION_COUNT = "station_count";

    private MediaPlayer mediaPlayer;
    public boolean hasEncounteredError;
    private String currentTrackName = "";
    private String currentUrl = "";
    private int trackCount;
    boolean wasInfoFound = false;
    private MediaNotificationManager mediaNotificationManager;
    private final ScheduledExecutorService executorService;
    Map<BroadcastReceiver, String> broadcastReceiverMap;
    private enum MediaPlayerState { PAUSED, PLAYING, STOPPED}
    private MediaPlayerState currentState = MediaPlayerState.STOPPED;
    private MainActivity mainActivity;
    private List<Track> tracks;

    private final IBinder binder = new LocalBinder();


    public MediaPlayerService() {

        log("Entered MediaPlayerService()");
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


    public void updateTracks(List<Track> tracks){
        this.tracks = new ArrayList(tracks);
    }


    public void stop(){
        currentState = MediaPlayerState.STOPPED;
        mediaPlayer.stop();
        mediaPlayer.reset();
//            mediaPlayer.release();
    }


    private void stopRunningMediaPlayer(){
        log("Entered stopRunningMediaPlayer, current state: " + currentState);
        currentState = MediaPlayerState.STOPPED;
        if(mediaPlayer != null){
            log("mediaPlayer is not null");
            if(mediaPlayer.isPlaying()){
                log("mediaPlayer isPlaying()");
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
        }
    }


    public void selectTrack(String trackUrl, String trackName){
        MediaPlayerState oldState = currentState;
        log("selectTrack() Current state: " + currentState);

        if(currentState == MediaPlayerState.PLAYING || currentState == MediaPlayerState.PAUSED){
            log("current state is playing or paused, so stopping");
            stop();
        }
        currentUrl = trackUrl;
        currentTrackName = trackName;
        if(oldState == MediaPlayerState.PLAYING){
            play();
        }
    }


    public void setActivity(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }


    boolean isPlaying(){
        return currentState == MediaPlayerState.PLAYING;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        log("Entered onCreate()");
        mediaPlayer = new MediaPlayer();
        setupBroadcastReceivers();
        mediaNotificationManager = new MediaNotificationManager(getApplicationContext(), this);
        log("Entered service created, already setupBroadcastReceivers and notification manager");
        moveToForeground();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        log("Entered onDestroy()");
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
        broadcastReceiverMap.put(serviceReceiverForChangeTrack, ACTION_CHANGE_TRACK);
        broadcastReceiverMap.put(serviceReceiverForPlay, ACTION_PLAY);
        broadcastReceiverMap.put(serviceReceiverForUpdateTrackCount,  ACTION_UPDATE_STATION_COUNT);
        broadcastReceiverMap.put(serviceReceiverForRequestStatus,       ACTION_REQUEST_STATUS);
        broadcastReceiverMap.put(serviceReceiverForPause, ACTION_PAUSE_PLAYER);
    }


    public void playTrack(String url, String name){
        this.currentUrl = url;
        this.currentTrackName = name;
        if(currentState == MediaPlayerState.STOPPED){
            log("Entered playTrack, player was stopped, so calling play()");
            play();
        }
        else if(currentState == MediaPlayerState.PAUSED){
            resume();
        }
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
        return currentTrackName;
    }


    String getCurrentUrl(){
        return currentUrl;
    }


    int getTrackCount(){
        return trackCount;
    }


    public void play() {
        updateViewsForConnecting();
        stopRunningMediaPlayer();
        executorService.schedule(this::startTrack, 1, TimeUnit.MILLISECONDS);

    }


    private void startTrack(){
        hasEncounteredError = false;
        try {
            setCpuWakeLock();
            log("Entered startTrack, setting data source for url: "+  currentUrl);
            mediaPlayer.setDataSource(currentUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
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
            mainActivity.notifyPlayerPlaying();
            mediaNotificationManager.updateNotification();
    }



    private void updateViewsForConnecting(){
        sendBroadcast(ACTION_NOTIFY_VIEW_OF_CONNECTING);
        wasInfoFound = false;
        mediaNotificationManager.updateNotification();
    }


    private void connectWithMediaPlayer(){
        hasEncounteredError = false;
        if(currentUrl == null) {
            stopPlayer();
            hasEncounteredError = true;
            return;
        }
        setCpuWakeLock();
        prepareAndPlay();
        mediaNotificationManager.updateNotification();
    }


    private void setCpuWakeLock(){
        if (checkSelfPermission(Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED) {
            mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        }
    }


    private void prepareAndPlay(){
        try {
            mediaPlayer = MediaPlayer.create(this, Uri.parse(currentUrl));
            mediaPlayer.prepareAsync();
            setupOnInfoListener();
            setupOnErrorListener();
            mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start());
        } catch (RuntimeException e) {
            stopPlayer();
            e.printStackTrace();
            hasEncounteredError = true;
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
        log("Entered stopPlayer()");
        stopPlayer(true);
    }


    private void stopPlayer(boolean notifyView){
        log("stopPlayer(" + notifyView + ")");
        releaseAndResetMediaPlayer();
        wasInfoFound = false;
        log("stopPlayer(" + notifyView + ") about to updateNotification");
        mediaNotificationManager.updateNotification();
        log("stopPlayer(" + notifyView + ") Sending Broadcast to notify view of stop");
        if(notifyView) {
            sendBroadcast(ACTION_NOTIFY_VIEW_OF_STOP);
        }
    }


    public void pause(){
        pauseMediaPlayer();
        wasInfoFound = false;
        mediaNotificationManager.updateNotification();
        mainActivity.notifyMediaPlayerPaused();
    }


    private void pauseMediaPlayer(){
        log("Entered pauseMediaPlayer");
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            log("pauseMediaPlayer() - media player is not null and is currently playing");
            mediaPlayer.pause();
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


    private final BroadcastReceiver serviceReceiverForUpdateTrackCount = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int oldTrackCount = trackCount;
            trackCount = intent.getIntExtra(TAG_STATION_COUNT, 0);
            if(trackCount != oldTrackCount){
                mediaNotificationManager.updateNotification();
            }
        }
    };


    private final BroadcastReceiver serviceReceiverForChangeTrack = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            currentTrackName = intent.getStringExtra(TAG_TRACK_NAME);
            currentUrl = intent.getStringExtra(TAG_TRACK_URL);
            if(currentState == MediaPlayerState.PLAYING){
                stopPlayer(false);
                play();
            }
            hasEncounteredError = false;
            mediaNotificationManager.updateNotification();
        }
    };
}
