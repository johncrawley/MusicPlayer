package com.jacstuff.musicplayer.service.helpers;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.jacstuff.musicplayer.service.MediaPlayerService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class BroadcastHelper {

    public static final String ACTION_REQUEST_STATUS = "com.j.crawley.music_player.requestStatus";
    public static final String ACTION_NOTIFY_VIEW_OF_STOP = "com.j.crawley.music_player.notifyViewOfStop";
    public static final String ACTION_NOTIFY_VIEW_OF_PLAYING = "com.j.crawley.music_player.notifyViewOfPlayInfo";
    public static final String ACTION_PLAY = "com.j.crawley.music_player.play";
    public static final String ACTION_PAUSE_PLAYER = "com.j.crawley.music_player.pausePlayer";
    public static final String ACTION_SELECT_PREVIOUS_TRACK = "com.j.crawley.music_player.selectPreviousTrack";
    public static final String ACTION_SELECT_NEXT_TRACK = "com.j.crawley.music_player.selectNextTrack";
    public static final String ACTION_NOTIFY_VIEW_OF_CONNECTING = "com.j.crawley.music_player.notifyViewOfPlay";

    private MediaPlayerService mediaPlayerService;
    Map<BroadcastReceiver, String> broadcastReceiverMap;
    private final AtomicBoolean shouldSkipBroadcastReceivedForTrackChange = new AtomicBoolean();

    public BroadcastHelper(MediaPlayerService mediaPlayerService){
        this.mediaPlayerService = mediaPlayerService;
        setupBroadcastReceivers();
    }


    public void onDestroy(){
        unregisterBroadcastReceivers(mediaPlayerService);
        mediaPlayerService = null;
    }


    public void notifyViewOfConnectingStatus(){
        sendBroadcast(ACTION_NOTIFY_VIEW_OF_CONNECTING);
    }


    private void setupBroadcastReceivers(){
        setupBroadcastReceiversMap();
        registerBroadcastReceivers(mediaPlayerService);
    }


    private void setupBroadcastReceiversMap(){
        broadcastReceiverMap = new HashMap<>();
        broadcastReceiverMap.put(serviceReceiverForPlay, ACTION_PLAY);
        broadcastReceiverMap.put(serviceReceiverForRequestStatus, ACTION_REQUEST_STATUS);
        broadcastReceiverMap.put(serviceReceiverForPause, ACTION_PAUSE_PLAYER);
        broadcastReceiverMap.put(serviceReceiverForNext, ACTION_SELECT_NEXT_TRACK);
        broadcastReceiverMap.put(serviceReceiverForPrevious, ACTION_SELECT_PREVIOUS_TRACK);
    }


    private void registerBroadcastReceivers(Service service){
        for(BroadcastReceiver bcr : broadcastReceiverMap.keySet()){
            IntentFilter intentFilter = new IntentFilter(broadcastReceiverMap.get(bcr));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                service.registerReceiver(bcr, intentFilter, Context.RECEIVER_EXPORTED);
            }
            else{
                service.registerReceiver(bcr, intentFilter);
            }
        }
    }


    private void unregisterBroadcastReceivers(Service service){
        for(BroadcastReceiver bcr : broadcastReceiverMap.keySet()){
            service.unregisterReceiver(bcr);
        }
    }


    private final BroadcastReceiver serviceReceiverForPlay = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            runOnAvailableMediaPlayerHelper(MediaPlayerHelper::onReceiveBroadcastForPlay);
        }
    };


    private final BroadcastReceiver serviceReceiverForNext = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleTrackChangeRequest(mediaPlayerService::loadNextTrack);
        }
    };


    private final BroadcastReceiver serviceReceiverForPrevious = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleTrackChangeRequest(mediaPlayerService::loadPreviousTrack);
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
            MediaPlayerHelper mediaPlayerHelper = mediaPlayerService.getMediaPlayerHelper();
            if(mediaPlayerHelper == null){
                return;
            }
            String broadcast = mediaPlayerHelper.isPlaying() ? ACTION_NOTIFY_VIEW_OF_PLAYING : ACTION_NOTIFY_VIEW_OF_STOP;
            sendBroadcast(broadcast);
        }
    };


    private void runOnAvailableMediaPlayerHelper(Consumer<MediaPlayerHelper> consumer){
        if(mediaPlayerService == null){
            return;
        }
        MediaPlayerHelper mediaPlayerHelper = mediaPlayerService.getMediaPlayerHelper();
        if(mediaPlayerHelper != null){
            consumer.accept(mediaPlayerHelper);
        }
    }


    private final BroadcastReceiver serviceReceiverForPause = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mediaPlayerService.pause();
        }
    };


    private void sendBroadcast(String action){
        mediaPlayerService.sendBroadcast(new Intent(action));
    }
}
