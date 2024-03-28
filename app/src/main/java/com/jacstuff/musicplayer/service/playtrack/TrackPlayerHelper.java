package com.jacstuff.musicplayer.service.playtrack;

import android.media.MediaPlayer;
import android.util.Log;

import com.jacstuff.musicplayer.service.PlayTrackService;
import com.jacstuff.musicplayer.service.db.entities.Track;
import com.jacstuff.musicplayer.service.helpers.MediaPlayerHelper;
import com.jacstuff.musicplayer.service.helpers.MediaPlayerState;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class TrackPlayerHelper {

    private final PlayTrackService service;
    private Track currentTrack;
    private MediaPlayer mediaPlayer;
    private MediaPlayerState currentState;
    private ScheduledFuture<?> updateElapsedTimeFuture;
    private final ScheduledExecutorService executorService;
    private boolean hasEncounteredError;



    public TrackPlayerHelper(PlayTrackService playTrackService){
        this.service = playTrackService;
        executorService = Executors.newScheduledThreadPool(3);
    }



    public void createMediaPlayer(){
        mediaPlayer = new MediaPlayer();
        currentState = MediaPlayerState.STOPPED;
        mediaPlayer.setOnCompletionListener(this::onTrackFinished);
        setupErrorListener();
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
        service.updateNotification();
        onError();
    }


    private void onError(){
        setStateToStopped();
        service.notifyViewOfMediaPlayerStop();
        releaseAndResetMediaPlayer();
        createMediaPlayer();
    }


    private void releaseMediaPlayerAndLocks(){
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }


    private void stopPlayer(){
        releaseAndResetMediaPlayer();
        service.updateNotification();
    }


    void setStateToStopped(){
        currentState = MediaPlayerState.STOPPED;
    }


    private void releaseAndResetMediaPlayer(){
        try {
            if (mediaPlayer != null) {
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }catch (RuntimeException e){
            Log.i("TrackPlayerHelper", "releaseAndResetMediaPlayerAndWifiLock() exception:  " + e.getMessage());
            e.printStackTrace();
        }
    }



    private void onTrackFinished(MediaPlayer mediaPlayer){
        currentState = MediaPlayerState.FINISHED;
        stopUpdatingElapsedTimeOnView();
        mediaPlayer.reset();
        stop(true);
    }

    public void stop(boolean shouldUpdateMainView){
        stop(shouldUpdateMainView, true);
    }


    public void stopUpdatingElapsedTimeOnView(){
        if(updateElapsedTimeFuture == null){
            return;
        }
        updateElapsedTimeFuture.cancel(false);
    }


    public boolean hasEncounteredError(){
        return false;
    }


    public void onReceiveBroadcastForPlay(){

    }


    public boolean isPlaying(){
        return false;
    }


    public boolean isPaused(){
        return false;
    }


    public String getCurrentUrl(){
        return "";
    }


    public void stop(boolean shouldUpdateMainView, boolean shouldUpdateNotification){

    }

    public Track getCurrentTrack(){
        return currentTrack;
    }


    public void onDestroy(){

    }

}
