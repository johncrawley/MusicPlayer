package com.jacstuff.musicplayer.service.playtrack;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.PowerManager;
import android.util.Log;

import com.jacstuff.musicplayer.service.PlayTrackService;
import com.jacstuff.musicplayer.service.db.entities.Track;
import com.jacstuff.musicplayer.service.helpers.MediaPlayerState;
import com.jacstuff.musicplayer.service.helpers.art.AlbumArtRetriever;
import com.jacstuff.musicplayer.view.trackplayer.TrackPlayerView;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TrackPlayerHelper implements MediaPlayer.OnPreparedListener {

    private final PlayTrackService service;
    private Track currentTrack;
    private MediaPlayer mediaPlayer;
    private MediaPlayerState currentState;
    private ScheduledFuture<?> updateElapsedTimeFuture;
    private final ScheduledExecutorService executorService;
    private boolean hasEncounteredError;
    private TrackPlayerView trackPlayerView;
    int elapsedTime = 0;

    private final AtomicBoolean isPreparingTrack = new AtomicBoolean(false);


    public TrackPlayerHelper(PlayTrackService playTrackService){
        this.service = playTrackService;
        executorService = Executors.newScheduledThreadPool(3);
    }

    public void setTrackPlayerView(TrackPlayerView trackPlayerView){
        this.trackPlayerView = trackPlayerView;
    }

    private long getDurationFrom(MediaMetadataRetriever retriever){
        var str = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        if(str == null || str.isBlank()){
            return 0L;
        }
        return Long.parseLong(str);
    }


    public void playTrackFrom(Context context, Uri uri){
        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()){
            retriever.setDataSource(context, uri);
            trackPlayerView.setAlbumArt(AlbumArtRetriever.retrieveAlbumArt(retriever));

            currentTrack = Track.Builder
                    .newInstance()
                    .withTitle(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE))
                    .withAlbum(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM))
                    .withArtist(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST))
                    .withDisc(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER))
                    .withBitrate(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE))
                    .withYear(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR))
                    .withGenre(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE))
                    .withUri(uri)
                    .duration(getDurationFrom(retriever))
                    .build();
            loadAndStartTrack();
            trackPlayerView.displayInfoFrom(currentTrack);

        }catch(IOException | IllegalArgumentException e){
            e.printStackTrace();
        }
    }


    public void seek(int milliseconds){
        if(currentState == MediaPlayerState.PLAYING || currentState == MediaPlayerState.PAUSED){
            mediaPlayer.seekTo(milliseconds);
        }
    }


    public void playOrResume(){
        if(currentState == MediaPlayerState.PAUSED){
            resume();
        }
        else{
            loadAndStartTrack();
        }
    }


    public void pause(){
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            stopUpdatingElapsedTimeOnView();
            currentState = MediaPlayerState.PAUSED;
            trackPlayerView.notifyMediaPlayerPaused();
        }
    }


    void resume(){
        currentState = MediaPlayerState.PLAYING;
        mediaPlayer.start();
        startUpdatingElapsedTimeOnView();
        trackPlayerView.notifyMediaPlayerPlaying();
        service.updateNotification();
    }


    public void loadAndStartTrack(){
        if(currentTrack == null){
            log("loadTrack() track is null, returning");
            return;
        }
        if(isPreparingTrack.get()){
            log("loadTrack, track is already being prepared, so exiting");
            return;
        }
        elapsedTime = 0;
        executorService.schedule(this::startTrack, 1, TimeUnit.MILLISECONDS);
        service.updateNotification();
    }


    private void startTrack(){
        resetErrorStatus();
        try {
            isPreparingTrack.set(true);
            stopPlayer();
            createAndPrepareMediaPlayer();
            startUpdatingElapsedTimeOnView();
            currentState = MediaPlayerState.PLAYING;
            trackPlayerView.notifyMediaPlayerPlaying();
        }catch (IOException e){
            log("IO exception trying to start track: " + currentTrack.getPathname());
            e.printStackTrace();
            onError();
            hasEncounteredError = true;
            trackPlayerView.displayError(currentTrack);
        }finally{
            isPreparingTrack.set(false);
            service.updateNotification();
        }
    }


    public void onPrepared(MediaPlayer mediaPlayer) {
        if (service.checkSelfPermission(Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED) {
            mediaPlayer.setWakeMode(service.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
         }
        mediaPlayer.start();
    }


    private void log(String msg){
        System.out.println("^^^ TrackPlayerHelper: " + msg);
    }


    public void startUpdatingElapsedTimeOnView(){
        updateElapsedTimeFuture = executorService.scheduleAtFixedRate(this::updateElapsedTimeOnView, 0L, 200L, TimeUnit.MILLISECONDS);
    }


    private void resetErrorStatus(){
        hasEncounteredError = false;
    }


    private void updateElapsedTimeOnView(){
        elapsedTime = mediaPlayer.getCurrentPosition();
        trackPlayerView.setElapsedTime(elapsedTime);
    }


    public void createAndPrepareMediaPlayer() throws IOException{
        mediaPlayer = new MediaPlayer();
        currentState = MediaPlayerState.STOPPED;
        mediaPlayer.setOnCompletionListener(this::onTrackFinished);
        setupErrorListener();
        mediaPlayer.setDataSource(service.getApplicationContext(), currentTrack.getUri());
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.prepare();
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
    }


    private void releaseMediaPlayerAndLocks(){
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }


    public void onDestroy(){
        releaseMediaPlayerAndLocks();
        mediaPlayer.release();
        mediaPlayer = null;
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


    public void stop(boolean shouldUpdateMainView, boolean shouldUpdateNotification){
        stopPlayer();
        if(shouldUpdateMainView){
            trackPlayerView.notifyMediaPlayerStopped();
        }
    }


    private void stopPlayer(){
        releaseAndResetMediaPlayer();
        service.updateNotification();
        setStateToStopped();
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


    public Track getCurrentTrack(){
        return currentTrack;
    }

}
