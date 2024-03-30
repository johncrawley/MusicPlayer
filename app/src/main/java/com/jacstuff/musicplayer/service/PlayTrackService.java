package com.jacstuff.musicplayer.service;

import static com.jacstuff.musicplayer.service.MediaNotificationManager.NOTIFICATION_ID;

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
import com.jacstuff.musicplayer.view.trackplayer.TrackPlayerView;

public class PlayTrackService extends Service implements AlbumArtConsumer {

    private PlayTrackNotificationManager playTrackNotificationManager;
    private final IBinder binder = new LocalBinder();
    private TrackPlayerHelper trackPlayerHelper;
    private PlayTrackBroadcastHelper playTrackBroadcastHelper;
    private AlbumArtRetriever albumArtRetriever;
    private OpenTrackActivity activity;


    public PlayTrackService() {
    }


    public void setActivity(OpenTrackActivity openTrackActivity){
        this.activity = openTrackActivity;
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


    public void playUri(Uri uri){
        trackPlayerHelper.playTrackFrom(getApplicationContext(), uri);
    }


    public void setArt(Bitmap bitmap){

    }


    public void playTrack(){

    }


    public void stop(){

    }

    public void seek(int progress){

    }


    public boolean isPlaying(){
        return false;
    }


    public Bitmap getAlbumArtForNotification(){
        return albumArtRetriever.getAlbumArtForNotification();
    }

    public void updateNotification(){
        playTrackNotificationManager.updateNotification();
    }


    public void notifyViewOfMediaPlayerStop(){

    }


    private void moveToForeground() {
        playTrackNotificationManager.init();
        Notification notification = playTrackNotificationManager.createNotification(getCurrentStatus(), "");
        startForeground(NOTIFICATION_ID, notification);
    }


    public TrackPlayerHelper getTrackPlayerHelper(){
        return trackPlayerHelper;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
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