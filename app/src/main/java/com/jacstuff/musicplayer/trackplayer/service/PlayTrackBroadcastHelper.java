package com.jacstuff.musicplayer.trackplayer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class PlayTrackBroadcastHelper {

    public static final String ACTION_REQUEST_STATUS = "com.j.crawley.music_player.playTrack.requestStatus";
    public static final String ACTION_NOTIFY_VIEW_OF_STOP = "com.j.crawley.music_player.playTrack.notifyViewOfStop";
    public static final String ACTION_NOTIFY_VIEW_OF_PLAYING = "com.j.crawley.music_player.playTrack.notifyViewOfPlayInfo";
    public static final String ACTION_PLAY = "com.j.crawley.music_player.playTrack.play";
    public static final String ACTION_PAUSE_PLAYER = "com.j.crawley.music_player.playTrack.pausePlayer";

    private PlayTrackService playTrackService;
    Map<BroadcastReceiver, String> broadcastReceiverMap;


    public PlayTrackBroadcastHelper(PlayTrackService playTrackService){
        this.playTrackService = playTrackService;
        setupBroadcastReceivers();
    }


    public void onDestroy(){
        unregisterBroadcastReceivers(playTrackService);
        playTrackService = null;
    }


    private void setupBroadcastReceivers(){
        setupBroadcastReceiversMap();
        registerBroadcastReceivers(playTrackService);
    }


    private void setupBroadcastReceiversMap(){
        broadcastReceiverMap = new HashMap<>();
        broadcastReceiverMap.put(serviceReceiverForPlay, ACTION_PLAY);
        broadcastReceiverMap.put(serviceReceiverForRequestStatus, ACTION_REQUEST_STATUS);
        broadcastReceiverMap.put(serviceReceiverForPause, ACTION_PAUSE_PLAYER);
    }


    private void registerBroadcastReceivers(Service service){
        for(BroadcastReceiver bcr : broadcastReceiverMap.keySet()){
            IntentFilter intentFilter = new IntentFilter(broadcastReceiverMap.get(bcr));
            service.registerReceiver(bcr, intentFilter);
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
            runIfAvailable(TrackPlayer::onReceiveBroadcastForPlay);
        }
    };


    private final BroadcastReceiver serviceReceiverForRequestStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TrackPlayer trackPlayer = playTrackService.getTrackPlayerHelper();
            if(trackPlayer == null){
                return;
            }
            String broadcast = trackPlayer.isPlaying() ? ACTION_NOTIFY_VIEW_OF_PLAYING : ACTION_NOTIFY_VIEW_OF_STOP;
            sendBroadcast(broadcast);
        }
    };


    private void runIfAvailable(Consumer<TrackPlayer> consumer){
        if(playTrackService == null){
            return;
        }
        TrackPlayer trackPlayer = playTrackService.getTrackPlayerHelper();
        if(trackPlayer != null){
            consumer.accept(trackPlayer);
        }
    }


    private final BroadcastReceiver serviceReceiverForPause = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            playTrackService.pause();
        }
    };


    private void sendBroadcast(String action){
        playTrackService.sendBroadcast(new Intent(action));
    }
}

