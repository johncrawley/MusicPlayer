package com.jacstuff.musicplayer.trackplayer.service;


import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import com.jacstuff.musicplayer.trackplayer.OpenTrackActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.helpers.art.AlbumArtConsumer;
import com.jacstuff.musicplayer.service.helpers.art.AlbumArtRetriever;

public class PlayTrackService extends Service implements AlbumArtConsumer {

    private PlayTrackNotificationManager playTrackNotificationManager;
    private final IBinder binder = new LocalBinder();
    private TrackPlayer trackPlayer;
    private PlayTrackBroadcastHelper playTrackBroadcastHelper;
    private AlbumArtRetriever albumArtRetriever;


    public PlayTrackService() {
    }


    public void setActivity(OpenTrackActivity openTrackActivity){
        if(trackPlayer.isTrackLoaded()){
            trackPlayer.updateView();
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        log("Entered onCreate()");
        trackPlayer = new TrackPlayer(this);
        playTrackBroadcastHelper = new PlayTrackBroadcastHelper(this);
        playTrackNotificationManager = new PlayTrackNotificationManager(getApplicationContext(), this, trackPlayer);
        albumArtRetriever = new AlbumArtRetriever(this, getApplicationContext());
        playTrackNotificationManager.init();
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        System.out.println("onTaskRemoved called");
        super.onTaskRemoved(rootIntent);
        this.stopSelf();
    }


    public void playUri(Uri uri){
        trackPlayer.playTrackFrom(getApplicationContext(), uri);
    }


    public void setArt(Bitmap bitmap){

    }


    public Bitmap getAlbumArtForNotification(){
        return albumArtRetriever.getAlbumArtForNotification();
    }


    public void updateNotification(){
        if(playTrackNotificationManager != null) {
            playTrackNotificationManager.updateNotification();
        }
    }



    public TrackPlayer getTrackPlayerHelper(){
        return trackPlayer;
    }


    private void log(String msg){
        System.out.println("^^^ PlayTrackService: " + msg);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        log("Entered onDestroy()");
        playTrackBroadcastHelper.onDestroy();
        trackPlayer.stop(false, false);
        trackPlayer.onDestroy();
        playTrackNotificationManager.dismissNotification();
        playTrackNotificationManager = null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY; // service is not restarted when terminated
    }


    public class LocalBinder extends Binder {
        public PlayTrackService getService() {
            return PlayTrackService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    public void pause(){

    }


    public String getCurrentStatus(){
        int resId = R.string.status_ready;
        if(trackPlayer.hasEncounteredError()){
            resId = R.string.status_error;
        }
        else if(trackPlayer.isPlaying()){
            resId = R.string.status_playing;
        }
        else if(trackPlayer.isPaused()){
            resId = R.string.status_paused;
        }
        return getApplicationContext().getString(resId);
    }


    public String getReadyStatusStr(){
        return getApplicationContext().getString(R.string.status_ready);
    }

}