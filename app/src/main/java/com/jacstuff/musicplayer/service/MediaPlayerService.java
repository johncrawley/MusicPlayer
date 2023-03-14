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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;


import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.db.playlist.Playlist;
import com.jacstuff.musicplayer.db.search.TrackFinder;
import com.jacstuff.musicplayer.db.track.Track;
import com.jacstuff.musicplayer.playlist.PlaylistManager;
import com.jacstuff.musicplayer.playlist.PlaylistManagerImpl;
import com.jacstuff.musicplayer.playlist.PlaylistViewNotifier;
import com.jacstuff.musicplayer.playlist.PlaylistViewNotifierImpl;
import com.jacstuff.musicplayer.playlist.TrackLoader;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener{

    public static final String ACTION_PLAY = "com.j.crawley.music_player.play";
    public static final String ACTION_PAUSE_PLAYER = "com.j.crawley.music_player.pausePlayer";
    public static final String ACTION_REQUEST_STATUS = "com.j.crawley.music_player.requestStatus";

    public static final String ACTION_SELECT_PREVIOUS_TRACK = "com.j.crawley.music_player.selectPreviousTrack";
    public static final String ACTION_SELECT_NEXT_TRACK = "com.j.crawley.music_player.selectNextTrack";
    public static final String ACTION_NOTIFY_VIEW_OF_STOP = "com.j.crawley.music_player.notifyViewOfStop";
    public static final String ACTION_NOTIFY_VIEW_OF_CONNECTING = "com.j.crawley.music_player.notifyViewOfPlay";
    public static final String ACTION_NOTIFY_VIEW_OF_PLAYING = "com.j.crawley.music_player.notifyViewOfPlayInfo";

    private MediaPlayer mediaPlayer;
    public boolean hasEncounteredError;
    private MediaNotificationManager mediaNotificationManager;
    private final ScheduledExecutorService executorService;
    Map<BroadcastReceiver, String> broadcastReceiverMap;

    private enum MediaPlayerState { PAUSED, PLAYING, STOPPED, FINISHED}
    private MediaPlayerState currentState = MediaPlayerState.STOPPED;
    private MainActivity mainActivity;
    private PlaylistManager playlistManager;
    private final IBinder binder = new LocalBinder();
    private Track currentTrack;
    private boolean shouldNextTrackPlayAfterCurrentTrackEnds = true;
    private ScheduledFuture<?> updateElapsedTimeFuture;
    private int elapsedTime;
    private TrackFinder trackFinder;
    private ScheduledFuture<?> stopTrackFuture;
    private TrackLoader trackLoader;
    private boolean haveTracksBeenLoaded;
    private final AtomicBoolean shouldSkipBroadcastReceivedForTrackChange = new AtomicBoolean();
    private final AtomicBoolean isPreparingTrack = new AtomicBoolean();
    private final AtomicBoolean isScanningForTracks = new AtomicBoolean();
    private PlaylistViewNotifier playlistViewNotifier;
    private Bitmap currentAlbumArt;


    public MediaPlayerService() {
        executorService = Executors.newScheduledThreadPool(3);
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


    private void loadTrackDataFromFilesystem(){
        isScanningForTracks.set(true);
        executorService.execute(()->{
            playlistManager.addTracksFromStorage(this);
            playlistManager.loadAllTracksPlaylist();
            updateListViews();
            ensureATrackIsSelectedIfAvailable();
            isScanningForTracks.set(false);
        });
    }


    public void refreshTrackDataFromFilesystem(){
        if(isScanningForTracks.get()){
            return;
        }
        isScanningForTracks.set(true);
        executorService.execute(()->{
            playlistManager.addTracksFromStorage(this);
            updateListViews();
            initTrackFinder();
            trackFinder.initCache();
            isScanningForTracks.set(false);
        });
    }


    private void updateListViews(){
        updateViewTrackList();
        mainActivity.updateAlbumsList(playlistManager.getAlbumNames());
        mainActivity.updateArtistsList(playlistManager.getArtistNames());
    }


    public void updateArtistView(){
        mainActivity.updateArtistsList(playlistManager.getArtistNames());
    }


    private void updateAlbumsView(){
        mainActivity.updateAlbumsList(playlistManager.getAlbumNames());
    }



    private void ensureATrackIsSelectedIfAvailable(){
        if(currentTrack == null){
            if(playlistManager.hasAnyTracks()){
                loadNextTrack();
            }
            else{
                mainActivity.hidePlayerViews();
            }
        }
        else{
            mainActivity.showPlayerViews();
        }
    }


    public List<Track> getTracksForSearch(String str){
        initTrackFinder();
        return trackFinder == null ? Collections.emptyList() : trackFinder.searchFor(str);
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


    private void stop(boolean shouldUpdateMainView){
        stop(shouldUpdateMainView, true);
    }


    private void stop(boolean shouldUpdateMainView, boolean shouldUpdateNotification){
        if(currentState == MediaPlayerState.PLAYING || currentState == MediaPlayerState.PAUSED) {
            mediaPlayer.stop();
            currentState = MediaPlayerState.STOPPED;
            mediaPlayer.reset();
        }
        stopUpdatingElapsedTimeOnView();
        elapsedTime = 0;
        if(shouldUpdateNotification) {
            mediaNotificationManager.updateNotification();
        }
        if(shouldUpdateMainView) {
            if(mainActivity != null) {
                mainActivity.notifyMediaPlayerStopped();
            }
        }
        cancelFutures();
    }


    public void seek(int milliseconds){
        if(currentState == MediaPlayerState.PLAYING || currentState == MediaPlayerState.PAUSED){
            mediaPlayer.seekTo(milliseconds);
        }
    }


    private void cancelFutures(){
        if(stopTrackFuture != null) {
            stopTrackFuture.cancel(false);
        }
    }


    public void loadTracksFromArtist(String artistName){
        playlistManager.loadTracksFromArtist(artistName);
        updateViewTrackListAndDeselectList();
        updateAlbumsView();
    }


    public void loadTracksFromAlbum(String albumName){
        playlistManager.loadTracksFromAlbum(albumName);
        updateViewTrackListAndDeselectList();
    }


    public void addTracksFromAristToCurrentPlaylist(String artistName){
        playlistManager.addTracksFromArtistToCurrentPlaylist(artistName, playlistViewNotifier);
        updateViewTrackList();
    }


    public void addTracksFromAlbumToCurrentPlaylist(String albumName){
        playlistManager.addTracksFromAlbumToCurrentPlaylist(albumName, playlistViewNotifier);
        updateViewTrackList();
    }


    public void loadPlaylist(Playlist playlist){
        playlistManager.loadPlaylist(playlist);
        updateViewTrackListAndDeselectList();
        updateAlbumsView();
    }


    public void addTrackToCurrentPlaylist(Track track){
        playlistManager.addTrackToCurrentPlaylist(track, playlistViewNotifier);
        updateViewTrackList();
        mediaNotificationManager.updateNotification();
    }


    public void removeTrackFromCurrentPlaylist(Track track){
        playlistManager.removeTrackFromCurrentPlaylist(track, playlistViewNotifier);
        updateViewTrackList();
        mediaNotificationManager.updateNotification();
    }


    private void initTrackFinder(){
        if(trackFinder == null){
            trackFinder =  new TrackFinder(trackLoader);
        }
    }


    public PlaylistManager getPlaylistManager(){
        return playlistManager;
    }


    private void updateViewTrackList(){
        int currentTrackIndex = currentTrack == null ? -1 : currentTrack.getIndex();
        mainActivity.updateTracksList(playlistManager.getTracks(), currentTrackIndex);
    }


    private void updateViewTrackListAndDeselectList(){
        mainActivity.updateTracksList(playlistManager.getTracks(), -1);
    }


    public List<Track> getTrackList(){
        return playlistManager.getTracks();
    }


    public void selectAndPlayTrack(Track track){
        currentTrack = track;
        updateViewsEnsurePlayerStoppedAndSchedulePlay();
        playlistManager.addToTrackHistory(track);
        mainActivity.setTrackInfoOnView(currentTrack, 0);
        cancelFutures();
    }


    public void selectTrack(int index){
        assignTrack(playlistManager.selectTrack(index));
    }


    public void loadNextTrack(){
        loadNext();
        cancelFutures();
    }


    private void loadNext(){
        Track nextTrack = playlistManager.getNextTrack();
        if(playlistManager.hasTracksQueued()){
            loadTrackDeselectCurrentTrack(nextTrack);
            return;
        }
        loadTrack(nextTrack);
    }


    public void loadPreviousTrack(){
        loadTrack(playlistManager.getPreviousTrack());
        cancelFutures();
    }


    private void loadTrack(Track track){
        if(isPreparingTrack.get()){
            return;
        }
        assignTrack(track);
        mainActivity.scrollToPosition(track.getIndex());
        mediaNotificationManager.updateNotification();
    }


    private void loadTrackDeselectCurrentTrack(Track track){
        assignTrack(track);
        mainActivity.deselectCurrentTrack();
        mediaNotificationManager.updateNotification();
    }


    public void enableStopAfterTrackFinishes(){
        if(currentState == MediaPlayerState.PLAYING) {
            shouldNextTrackPlayAfterCurrentTrackEnds = false;
        }
    }


    public void stopPlayingInThreeMinutes(){
        stopPlayingInMinutes(3);
    }


    private void stopPlayingInMinutes(int minutes){
      stopTrackFuture = executorService.schedule( this::stopAndResetTime, minutes, TimeUnit.SECONDS);
    }


    private void stopAndResetTime(){
        stop(true);
        mainActivity.resetElapsedTime();
    }


    private void assignTrack(Track track){
        currentTrack = track;
        elapsedTime = 0;
        if(currentTrack == null){
            return;
        }
        if(currentTrack.getPathname() == null) {
            handleNullPathname();
            return;
        }
        mainActivity.setTrackInfoOnView(currentTrack, 0);
        if(currentState == MediaPlayerState.PAUSED){
            mainActivity.hideTrackSeekBar();
        }
        select(currentTrack);
    }


    private void select(Track track){
        MediaPlayerState oldState = currentState;
        if(currentState == MediaPlayerState.PLAYING || currentState == MediaPlayerState.PAUSED){
            stop(false);
        }
        currentTrack = track;
        if(oldState == MediaPlayerState.PLAYING){
            updateViewsEnsurePlayerStoppedAndSchedulePlay();
        }
    }


    public void setActivity(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        playlistViewNotifier = new PlaylistViewNotifierImpl(mainActivity);
        createPlaylistManagerAndTrackLoader();
        if(!haveTracksBeenLoaded){
            loadTrackDataFromFilesystem();
            haveTracksBeenLoaded = true;
            return;
        }
        updateViews();
    }


    private void setCoverArt(Track track){
        try(MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever()){
            mediaMetadataRetriever.setDataSource(track.getPathname());
            currentAlbumArt = retrieveAlbumArt(mediaMetadataRetriever);
            mainActivity.setAlbumArt(currentAlbumArt);
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    private Bitmap retrieveAlbumArt(MediaMetadataRetriever mediaMetadataRetriever){
        byte[] coverArt = mediaMetadataRetriever.getEmbeddedPicture();
        if (coverArt != null) {
            return BitmapFactory.decodeByteArray(coverArt, 0, coverArt.length);
        }
        return null;
    }


    private void createPlaylistManagerAndTrackLoader(){
        if(playlistManager == null) {
            trackLoader = new TrackLoader(getApplicationContext());
            playlistManager = new PlaylistManagerImpl(mainActivity.getApplicationContext(), trackLoader);
        }
    }


    private void updateViews(){
        if(currentTrack != null){
            mainActivity.setTrackInfoOnView(currentTrack, 0);
            mainActivity.setElapsedTime(elapsedTime);
            mainActivity.setAlbumArt(currentAlbumArt);
        }
        updateListViews();
    }


    public boolean isPlaying(){
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
        currentState = MediaPlayerState.STOPPED;
        mediaPlayer.setOnCompletionListener(this::onTrackFinished);
        setupErrorListener();
    }


    private void onTrackFinished(MediaPlayer mediaPlayer){
        currentState = MediaPlayerState.FINISHED;
        stopUpdatingElapsedTimeOnView();
        mediaPlayer.reset();
        loadNextTrack();
        if(shouldNextTrackPlayAfterCurrentTrackEnds) {
            updateViewsEnsurePlayerStoppedAndSchedulePlay();
        }
        else{
            stop();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBroadcastReceivers();
        releaseMediaPlayerAndLocks();
        stop(false, false);
        mediaPlayer.release();
        mediaPlayer = null;
        mediaNotificationManager.dismissNotification();
        mediaNotificationManager = null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return Service.START_NOT_STICKY; // service is not restarted when terminated
    }


    public void enableShuffle(){
        playlistManager.enableShuffle();
        mainActivity.notifyShuffleEnabled();
    }


    public boolean isShuffleEnabled(){
        return playlistManager.isShuffleEnabled();
    }


    public void disableShuffle(){
        playlistManager.disableShuffle();
        mainActivity.notifyShuffleDisabled();
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
        if(currentState == MediaPlayerState.STOPPED || currentState == MediaPlayerState.FINISHED){
            updateViewsEnsurePlayerStoppedAndSchedulePlay();
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
        elapsedTime = mediaPlayer.getCurrentPosition();
        mainActivity.setElapsedTime(elapsedTime);
    }


    int getTrackCount(){
        return playlistManager == null ? 0 : playlistManager.getNumberOfTracks();
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


    Track getCurrentTrack(){
        return currentTrack;
    }


    String getCurrentUrl(){
        return currentTrack == null ? "" : currentTrack.getPathname();
    }


    private void updateViewsEnsurePlayerStoppedAndSchedulePlay() {
        updateViewsForConnecting();
        stopRunningMediaPlayer();
        stopUpdatingElapsedTimeOnView();
        elapsedTime = 0;
        shouldNextTrackPlayAfterCurrentTrackEnds = true;
        executorService.schedule(this::startTrack, 1, TimeUnit.MILLISECONDS);
    }


    private void stopRunningMediaPlayer(){
        if(mediaPlayer != null && (mediaPlayer.isPlaying() || currentState == MediaPlayerState.PAUSED)){
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
        currentState = MediaPlayerState.STOPPED;
    }


    private void startTrack(){
        hasEncounteredError = false;
        try {
            isPreparingTrack.set(true);
            stopPlayer();
            createMediaPlayer();
            setCoverArt(currentTrack);
            mediaPlayer.setDataSource(currentTrack.getPathname());
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepare();
            startUpdatingElapsedTimeOnView();
            currentState = MediaPlayerState.PLAYING;
            mainActivity.notifyMediaPlayerPlaying();
            mediaNotificationManager.updateNotification();
        }catch (IOException e){
            e.printStackTrace();
            onError();
            mainActivity.displayError(currentTrack);
        }finally{
            isPreparingTrack.set(false);
        }
    }


    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        setCpuWakeLock();
        mediaPlayer.start();
    }


    private void onError(){
        currentState = MediaPlayerState.STOPPED;
        mainActivity.notifyMediaPlayerStopped();
        releaseAndResetMediaPlayer();
        createMediaPlayer();
    }


    public void resume(){
            currentState = MediaPlayerState.PLAYING;
            mediaPlayer.start();
            startUpdatingElapsedTimeOnView();
            mainActivity.notifyMediaPlayerPlaying();
            mediaNotificationManager.updateNotification();
    }


    private void updateViewsForConnecting(){
        sendBroadcast(ACTION_NOTIFY_VIEW_OF_CONNECTING);
        mediaNotificationManager.updateNotification();
    }


    private void setCpuWakeLock(){
        if (checkSelfPermission(Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED) {
            mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        }
    }


    private void setupErrorListener(){
        mediaPlayer.setOnErrorListener((mediaPlayer, i, i1) -> {
            stopPlayer();
            handleConnectionError();
            return false;
        });
    }


    private void handleConnectionError(){
        hasEncounteredError = true;
        mediaNotificationManager.updateNotification();
        onError();
    }


    private void stopPlayer(){
        releaseAndResetMediaPlayer();
        mediaNotificationManager.updateNotification();
    }


    public void pause(){
        pauseMediaPlayer();
        mediaNotificationManager.updateNotification();
        mainActivity.notifyMediaPlayerPaused();
        cancelFutures();
    }


    private void pauseMediaPlayer(){
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            stopUpdatingElapsedTimeOnView();
            currentState = MediaPlayerState.PAUSED;
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
                updateViewsEnsurePlayerStoppedAndSchedulePlay();
            }
        }
    };


    private final BroadcastReceiver serviceReceiverForNext = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleTrackChangeRequest(MediaPlayerService.this::loadNextTrack);
        }
    };


    private final BroadcastReceiver serviceReceiverForPrevious = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleTrackChangeRequest(MediaPlayerService.this::loadPreviousTrack);
        }
    };


    private void handleTrackChangeRequest(Runnable operation){
        if(shouldSkipBroadcastReceivedForTrackChange.get()){
            return;
        }
        shouldSkipBroadcastReceivedForTrackChange.set(true);
        operation.run();
        new Handler(Looper.getMainLooper())
                .postDelayed(()-> shouldSkipBroadcastReceivedForTrackChange.set(false),
                        600);
    }


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
