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
import android.os.IBinder;
import android.os.PowerManager;


import com.jacstuff.musicplayer.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MediaPlayerService extends Service {

    public static final String ACTION_START_PLAYER = "com.j.crawley.music_player.startPlayer";
    public static final String ACTION_PAUSE_PLAYER = "com.j.crawley.music_player.startPlayer";
    public static final String ACTION_STOP_PLAYER = "com.j.crawley.music_player.stopPlayer";
    public static final String ACTION_CHANGE_TRACK = "com.j.crawley.music_player.changeTrack";
    public static final String ACTION_REQUEST_STATUS = "com.j.crawley.music_player.requestStatus";
    public static final String ACTION_UPDATE_STATION_COUNT = "com.j.crawley.music_player.updateStationCount";
    public static final String ACTION_PLAY_CURRENT = "com.j.crawley.music_player.playCurrent";

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
    private boolean isPlaying;
    private String currentStationName  = "";
    private String currentUrl = "";
    private int trackCount;
    boolean wasInfoFound = false;
    private MediaNotificationManager mediaNotificationManager;
    private final ScheduledExecutorService executorService;
    Map<BroadcastReceiver, String> broadcastReceiverMap;
    private enum MediaPlayerState { PAUSED, PLAYING, STOPPED}
    private MediaPlayerState currentState = MediaPlayerState.STOPPED;


    public MediaPlayerService() {

        log("Entered MediaPlayerService()");
        executorService = Executors.newScheduledThreadPool(3);
    }


    private final BroadcastReceiver serviceReceiverForStopPlayer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopPlayer();
        }
    };


    private final BroadcastReceiver serviceReceiverForPausePlayer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(currentState != MediaPlayerState.PLAYING){
                return;
            }
            pausePlayer();
        }
    };


    private final BroadcastReceiver serviceReceiverForStartPlayer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            currentUrl = intent.getStringExtra(TAG_TRACK_URL);
            log("entered serviceReceiverForStartPlayer.onReceive() track url: " + currentUrl);
            currentStationName = intent.getStringExtra(TAG_TRACK_NAME);
            if (currentState == MediaPlayerState.PAUSED){
                resume();
                return;
            }
            play();
        }
    };



    private final BroadcastReceiver serviceReceiverForPlayCurrent = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            play();
        }
    };


    private final BroadcastReceiver serviceReceiverForRequestStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String broadcast = isPlaying ? ACTION_NOTIFY_VIEW_OF_PLAYING : ACTION_NOTIFY_VIEW_OF_STOP;
            sendBroadcast(broadcast);
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
            currentStationName = intent.getStringExtra(TAG_TRACK_NAME);
            currentUrl = intent.getStringExtra(TAG_TRACK_URL);
            if(isPlaying){
                stopPlayer(false);
                play();
            }
            hasEncounteredError = false;
            mediaNotificationManager.updateNotification();
        }
    };


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    boolean isPlaying(){
        return isPlaying;
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
        unregisterBroadcastReceivers();
        releaseMediaPlayerAndLocks();
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
        broadcastReceiverMap.put(serviceReceiverForStopPlayer,          ACTION_STOP_PLAYER);
        broadcastReceiverMap.put(serviceReceiverForStartPlayer,         ACTION_START_PLAYER);
        broadcastReceiverMap.put(serviceReceiverForPausePlayer,         ACTION_PAUSE_PLAYER);
        broadcastReceiverMap.put(serviceReceiverForChangeTrack, ACTION_CHANGE_TRACK);
        broadcastReceiverMap.put(serviceReceiverForPlayCurrent,         ACTION_PLAY_CURRENT);
        broadcastReceiverMap.put(serviceReceiverForUpdateTrackCount,  ACTION_UPDATE_STATION_COUNT);
        broadcastReceiverMap.put(serviceReceiverForRequestStatus,       ACTION_REQUEST_STATUS);
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
        else if(isPlaying){
            resId = wasInfoFound ? R.string.status_playing : R.string.status_connecting;
        }
        return getApplicationContext().getString(resId);
    }


    String getCurrentStationName(){
        return currentStationName;
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


    public void resume(){
            currentState = MediaPlayerState.PLAYING;
            mediaPlayer.start();
    }


    private void stopRunningMediaPlayer(){
        currentState = MediaPlayerState.STOPPED;
        if(mediaPlayer != null){
            if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer.reset();
        }}
    }


    private void updateViewsForConnecting(){
        sendBroadcast(ACTION_NOTIFY_VIEW_OF_CONNECTING);
        isPlaying = true;
        wasInfoFound = false;
        mediaNotificationManager.updateNotification();
    }



    private void connectWithMediaPlayer(){
        isPlaying = true;
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


    private void startTrack(){
        isPlaying = true;
        hasEncounteredError = false;
        try {
            setCpuWakeLock();
            mediaPlayer.setDataSource(currentUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
            currentState = MediaPlayerState.PLAYING;
        }catch (IOException e){
            e.printStackTrace();
            currentState = MediaPlayerState.STOPPED;
        }
    }


    private void setCpuWakeLock(){
        if (checkSelfPermission(Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED) {
            mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        }
    }


    private void prepareAndPlay(){
        try {
            //assert mediaPlayer != null;
            //mediaPlayer.setDataSource(this, Uri.parse(currentUrl));
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
        isPlaying = false;
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


    private void pausePlayer(){
        pauseMediaPlayer();
        wasInfoFound = false;
        mediaNotificationManager.updateNotification();
        sendBroadcast(ACTION_NOTIFY_VIEW_OF_PAUSE);
    }


    private void pauseMediaPlayer(){
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            currentState = MediaPlayerState.PAUSED;
        }
    }


    private void releaseAndResetMediaPlayer(){
        try {
            if (mediaPlayer != null) {
                mediaPlayer.reset();
                mediaPlayer.release();
                isPlaying = false;
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
}
