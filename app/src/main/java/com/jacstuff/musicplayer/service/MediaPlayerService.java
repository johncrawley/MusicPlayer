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
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
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
    public static final String ACTION_STOP_PLAYER = "com.j.crawley.music_player.stopPlayer";
    public static final String ACTION_CHANGE_STATION = "com.j.crawley.music_player.changeStation";
    public static final String ACTION_REQUEST_STATUS = "com.j.crawley.music_player.requestStatus";
    public static final String ACTION_UPDATE_STATION_COUNT = "com.j.crawley.music_player.updateStationCount";
    public static final String ACTION_PLAY_CURRENT = "com.j.crawley.music_player.playCurrent";

    public static final String ACTION_SELECT_PREVIOUS_TRACK = "com.j.crawley.music_player.selectPreviousTrack";
    public static final String ACTION_SELECT_NEXT_TRACK = "com.j.crawley.music_player.selectNextTrack";
    public static final String ACTION_NOTIFY_VIEW_OF_STOP = "com.j.crawley.music_player.notifyViewOfStop";
    public static final String ACTION_NOTIFY_VIEW_OF_CONNECTING = "com.j.crawley.music_player.notifyViewOfPlay";
    public static final String ACTION_NOTIFY_VIEW_OF_PLAYING = "com.j.crawley.music_player.notifyViewOfPlayInfo";
    public static final String ACTION_NOTIFY_VIEW_OF_ERROR = "com.j.crawley.music_player.notifyViewOfError";

    public static final String TAG_STATION_URL = "station_url";
    public static final String TAG_STATION_NAME = "station_name";
    public static final String TAG_STATION_COUNT = "station_count";

    private MediaPlayer mediaPlayer;
    public boolean hasEncounteredError;
    private boolean isPlaying;
    private String currentStationName  = "";
    private String currentUrl = "";
    private int stationCount;
    boolean wasInfoFound = false;
    private MediaNotificationManager mediaNotificationManager;
    private final ScheduledExecutorService executorService;
    private WifiManager.WifiLock wifiLock;
    Map<BroadcastReceiver, String> broadcastReceiverMap;

    public MediaPlayerService() {
        executorService = Executors.newScheduledThreadPool(3);
    }


    private final BroadcastReceiver serviceReceiverForStopPlayer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopPlayer();
        }
    };


    private final BroadcastReceiver serviceReceiverForStartPlayer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            currentUrl = intent.getStringExtra(TAG_STATION_URL);
            currentStationName = intent.getStringExtra(TAG_STATION_NAME);
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


    private final BroadcastReceiver serviceReceiverForUpdateStationCount = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int oldStationCount = stationCount;
            stationCount = intent.getIntExtra(TAG_STATION_COUNT, 0);
            if(stationCount != oldStationCount){
                mediaNotificationManager.updateNotification();
            }
        }
    };


    private final BroadcastReceiver serviceReceiverForChangeStation = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            currentStationName = intent.getStringExtra(TAG_STATION_NAME);
            currentUrl = intent.getStringExtra(TAG_STATION_URL);
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
        initWifiLock();
        setupBroadcastReceivers();
        mediaNotificationManager = new MediaNotificationManager(getApplicationContext(), this);
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


    private void initWifiLock(){
        wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "jcrawley.webRadio.wifiWakeLock");
    }


    private void setupBroadcastReceivers(){
        setupBroadcastReceiversMap();
        registerBroadcastReceivers();
    }


    private void setupBroadcastReceiversMap(){
        broadcastReceiverMap = new HashMap<>();
        broadcastReceiverMap.put(serviceReceiverForStopPlayer,          ACTION_STOP_PLAYER);
        broadcastReceiverMap.put(serviceReceiverForStartPlayer,         ACTION_START_PLAYER);
        broadcastReceiverMap.put(serviceReceiverForChangeStation,       ACTION_CHANGE_STATION);
        broadcastReceiverMap.put(serviceReceiverForPlayCurrent,         ACTION_PLAY_CURRENT);
        broadcastReceiverMap.put(serviceReceiverForUpdateStationCount,  ACTION_UPDATE_STATION_COUNT);
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
        if (wifiLock.isHeld()) {
            wifiLock.release();
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


    int getStationCount(){
        return stationCount;
    }


    public void play() {
        updateViewsForConnecting();
        wifiLock.acquire();
        stopRunningMediaPlayer();
        executorService.schedule(this::connectWithMediaPlayer, 1, TimeUnit.MILLISECONDS);
    }


    private void stopRunningMediaPlayer(){
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
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
        createNewMediaPlayer();
        prepareAndPlay();
        mediaNotificationManager.updateNotification();
    }


    private void createNewMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
        setCpuWakeLock();
    }


    private void setCpuWakeLock(){
        if (checkSelfPermission(Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED) {
            mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        }
    }


    private void prepareAndPlay(){
        try {
            assert mediaPlayer != null;
            mediaPlayer.setDataSource(this, Uri.parse(currentUrl));
            mediaPlayer.prepareAsync();
            setupOnInfoListener();
            setupOnErrorListener();
            mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start());
        } catch (IOException | RuntimeException e) {
            stopPlayer();
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
        stopPlayer(true);
    }


    private void stopPlayer(boolean notifyView){
        releaseAndResetMediaPlayerAndWifiLock();
        isPlaying = false;
        wasInfoFound = false;
        mediaNotificationManager.updateNotification();
        if(notifyView) {
            sendBroadcast(ACTION_NOTIFY_VIEW_OF_STOP);
        }
    }


    private void releaseAndResetMediaPlayerAndWifiLock(){
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            wifiLock.release();
        }
    }


    private void sendBroadcast(String action){
        sendBroadcast(new Intent(action));
    }

}
