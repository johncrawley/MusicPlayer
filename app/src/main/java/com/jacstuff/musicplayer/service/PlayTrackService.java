package com.jacstuff.musicplayer.service;


import static com.jacstuff.musicplayer.service.playtrack.PlayTrackNotificationManager.PLAY_TRACK_NOTIFICATION_ID;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import com.jacstuff.musicplayer.OpenTrackActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.helpers.art.AlbumArtConsumer;
import com.jacstuff.musicplayer.service.helpers.art.AlbumArtRetriever;
import com.jacstuff.musicplayer.service.playtrack.PlayTrackBroadcastHelper;
import com.jacstuff.musicplayer.service.playtrack.PlayTrackNotificationManager;
import com.jacstuff.musicplayer.service.playtrack.TrackPlayerHelper;
public class PlayTrackService extends Service implements AlbumArtConsumer {

    private PlayTrackNotificationManager playTrackNotificationManager;
    private final IBinder binder = new LocalBinder();
    private TrackPlayerHelper trackPlayerHelper;
    private PlayTrackBroadcastHelper playTrackBroadcastHelper;
    private AlbumArtRetriever albumArtRetriever;


    public PlayTrackService() {
    }


    public void setActivity(OpenTrackActivity openTrackActivity){
    }


    @Override
    public void onCreate() {
        super.onCreate();
        trackPlayerHelper = new TrackPlayerHelper(this);
        playTrackBroadcastHelper = new PlayTrackBroadcastHelper(this);
        playTrackNotificationManager = new PlayTrackNotificationManager(getApplicationContext(), this, trackPlayerHelper);
        albumArtRetriever = new AlbumArtRetriever(this, getApplicationContext());
        moveToForeground();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        System.out.println("onTaskRemoved called");
        super.onTaskRemoved(rootIntent);
        this.stopSelf();
    }

    public void playUri(Uri uri){
        trackPlayerHelper.playTrackFrom(getApplicationContext(), uri);
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


    private void moveToForeground() {
        playTrackNotificationManager.init();
      //  Notification notification = playTrackNotificationManager.createNotification(getCurrentStatus(), "");
      //  startForeground(PLAY_TRACK_NOTIFICATION_ID, notification);
    }


    public TrackPlayerHelper getTrackPlayerHelper(){
        return trackPlayerHelper;
    }


    private void log(String msg){
        System.out.println("^^^ PlayTrackService: " + msg);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        log("Entered onDestroy()");
        playTrackBroadcastHelper.onDestroy();
        trackPlayerHelper.stop(false, false);
        trackPlayerHelper.onDestroy();
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
        if(trackPlayerHelper.hasEncounteredError()){
            resId = R.string.status_error;
        }
        else if(trackPlayerHelper.isPlaying()){
            resId = R.string.status_playing;
        }
        else if(trackPlayerHelper.isPaused()){
            resId = R.string.status_paused;
        }
        return getApplicationContext().getString(resId);
    }


    public String getReadyStatusStr(){
        return getApplicationContext().getString(R.string.status_ready);
    }

}