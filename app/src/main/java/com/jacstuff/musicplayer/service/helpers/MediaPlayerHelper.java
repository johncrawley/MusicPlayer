package com.jacstuff.musicplayer.service.helpers;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.PowerManager;
import android.util.Log;

import com.jacstuff.musicplayer.service.MediaPlayerService;
import com.jacstuff.musicplayer.service.db.entities.Track;
import com.jacstuff.musicplayer.service.helpers.art.AlbumArtRetriever;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MediaPlayerHelper implements MediaPlayer.OnPreparedListener {

    private final MediaPlayerService mediaPlayerService;
    private MediaPlayer mediaPlayer;
    private MediaPlayerState currentState = MediaPlayerState.STOPPED;
    private boolean shouldNextTrackPlayAfterCurrentTrackEnds = true;
    public boolean hasEncounteredError;
    private ScheduledFuture<?> stopTrackFuture;
    private ScheduledFuture<?> updateElapsedTimeFuture;
    private final AtomicBoolean isPreparingTrack = new AtomicBoolean();
    private int elapsedTime;
    private Track currentTrack;
    private final ScheduledExecutorService executorService;


    public MediaPlayerHelper(MediaPlayerService mediaPlayerService){
        this.mediaPlayerService = mediaPlayerService;
        executorService = Executors.newScheduledThreadPool(3);
    }


    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayerService.setCpuWakeLock();
        mediaPlayer.start();
    }


    private void onError(){
        setStateToStopped();
        mediaPlayerService.notifyViewOfMediaPlayerStop();
        releaseAndResetMediaPlayer();
        createMediaPlayer();
    }


    public void loadNext(Track track){
        resetErrorStatus();
        loadTrack(track == null ? currentTrack : track);
        cancelScheduledStoppageOfTrack();
    }


    private void resetErrorStatus(){
        hasEncounteredError = false;
    }


    void loadTrack(Track track){
        if(isPreparingTrack.get()){
            return;
        }
        assignTrack(track);
        mediaPlayerService.scrollToPositionOf(track);
        mediaPlayerService.updateNotification();
    }


    public void onDestroy(){
        releaseMediaPlayerAndLocks();
        mediaPlayer.release();
        mediaPlayer = null;
    }


    private void releaseMediaPlayerAndLocks(){
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }


    public int getElapsedTime(){
        return elapsedTime;
    }


    public void stop(boolean shouldUpdateMainView){
        stop(shouldUpdateMainView, true);
    }


    public void assignTrack(Track track){
        currentTrack = track;
        elapsedTime = 0;
        if(currentTrack == null){
            return;
        }
        if(currentTrack.getPathname() == null) {
            handleNullPathname();
            return;
        }
        assignAlbumArt(track);
        if(hasEncounteredError){
            log("assignTrack() encountered error after invoking assignAlbumArt()");
            mediaPlayerService.setBlankAlbumArt();
        }
        mediaPlayerService.updateViewsOnTrackAssigned();
        select(currentTrack);
    }


    private void log(String msg){
        System.out.println("^^^ MediaPlayerHelper: " + msg);
    }


    public void selectAndPlayTrack(Track track){
        cancelScheduledStoppageOfTrack();
        currentTrack = track;
        assignAlbumArt(track);
        if(hasEncounteredError){
            mediaPlayerService.setBlankAlbumArt();
        }
        updateViewsEnsurePlayerStoppedAndSchedulePlay();
    }


    private void handleNullPathname(){
        if(currentTrack.getPathname() == null){
            mediaPlayerService.setBlankTrackInfoOnMainView();
            setStateToStopped();
        }
    }


    public void loadPreviousTrack(Track previousTrack){
        resetErrorStatus();
        loadTrack(previousTrack);
        cancelScheduledStoppageOfTrack();
    }


    private void stopAndResetTime(){
        stop(true);
        mediaPlayerService.resetElapsedTimeOnMainView();
    }


    public void stopPlayingInThreeMinutes(int numberOfMinutes){
        stopTrackFuture = executorService.schedule( this::stopAndResetTime, numberOfMinutes, TimeUnit.MINUTES);
    }


    private void assignAlbumArt(Track track){
        AlbumArtRetriever albumArtRetriever = mediaPlayerService.getAlbumArtRetriever();
        try{
            albumArtRetriever.assignAlbumArt(track);
        }
        catch(IOException e){
            e.printStackTrace();
        } catch(RuntimeException e){
            hasEncounteredError = true;
            mediaPlayerService.notifyMainViewThatFileDoesNotExist(track);
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
        mediaPlayerService.updateNotification();
        onError();
    }


    public Track getCurrentTrack(){
        return currentTrack;
    }


    public void playTrackFrom(Context context, Uri uri){
        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()){
            retriever.setDataSource(context, uri);
            Track track = Track.Builder
                    .newInstance()
                    .withTitle(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE))
                    .withAlbum(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM))
                    .withArtist(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST))
                    .withDisc(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER))
                    .withBitrate(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE))
                    .withYear(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR))
                    .withGenre(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE))
                    .withUri(uri)
                    .build();
            loadTrack(track);

        }catch(IOException | IllegalArgumentException e){
            e.printStackTrace();
        }
    }


    public String getCurrentUrl(){
        return currentTrack == null ? "" : currentTrack.getPathname();
    }


    public void playTrack(){
        if(currentState == MediaPlayerState.STOPPED || currentState == MediaPlayerState.FINISHED){
            updateViewsEnsurePlayerStoppedAndSchedulePlay();
        }
        else if(currentState == MediaPlayerState.PAUSED){
            resume();
        }
    }


    private void updateElapsedTimeOnView(){
        elapsedTime = mediaPlayer.getCurrentPosition();
        mediaPlayerService.setElapsedTimeOnView(elapsedTime);
    }


    public boolean isPlaying(){
        return currentState == MediaPlayerState.PLAYING;
    }


    public boolean hasEncounteredError(){
        return hasEncounteredError;
    }


    public void cancelScheduledStoppageOfTrack(){
        if(stopTrackFuture != null) {
            stopTrackFuture.cancel(false);
        }
    }


    public void stop(boolean shouldUpdateMainView, boolean shouldUpdateNotification){
        if(currentState == MediaPlayerState.PLAYING || currentState == MediaPlayerState.PAUSED) {
            mediaPlayer.stop();
            currentState = MediaPlayerState.STOPPED;
            mediaPlayer.reset();
        }
        mediaPlayerService.stopUpdatingElapsedTimeOnView();
        elapsedTime = 0;
        if(shouldUpdateNotification) {
            mediaPlayerService.updateNotification();
        }
        mediaPlayerService.updateMainViewOfStop(shouldUpdateMainView);
        cancelScheduledStoppageOfTrack();
    }


    private void select(Track track){
        MediaPlayerState oldState = currentState;
        stopTrackIfPlayingOrPaused();
        currentTrack = track;
        if(oldState == MediaPlayerState.PLAYING){
            updateViewsEnsurePlayerStoppedAndSchedulePlay();
        }
    }


    private void stopTrackIfPlayingOrPaused(){
        if(currentState == MediaPlayerState.PLAYING || currentState == MediaPlayerState.PAUSED){
            stop(false);
        }
    }


    public boolean isPaused(){
        return currentState == MediaPlayerState.PAUSED;
    }


    public void enabledStopAfterTrackFinishes(){
        if(currentState == MediaPlayerState.PLAYING) {
            shouldNextTrackPlayAfterCurrentTrackEnds = false;
        }
    }


    public void pauseMediaPlayer(){
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            stopUpdatingElapsedTimeOnView();
            currentState = MediaPlayerState.PAUSED;
        }
    }


    public void stopUpdatingElapsedTimeOnView(){
        if(updateElapsedTimeFuture == null){
            return;
        }
        updateElapsedTimeFuture.cancel(false);
    }


    private void onTrackFinished(MediaPlayer mediaPlayer){
        currentState = MediaPlayerState.FINISHED;
        stopUpdatingElapsedTimeOnView();
        mediaPlayer.reset();
        mediaPlayerService.loadNextTrack();
        if(shouldNextTrackPlayAfterCurrentTrackEnds) {
            updateViewsEnsurePlayerStoppedAndSchedulePlay();
        }
        else{
            stop(true);
        }
    }


    void resume(){
        currentState = MediaPlayerState.PLAYING;
        mediaPlayer.start();
        startUpdatingElapsedTimeOnView();
        mediaPlayerService.notifyMainViewOfMediaPlayerPlaying();
        mediaPlayerService.updateNotification();
    }


    public void seek(int milliseconds){
        if(currentState == MediaPlayerState.PLAYING || currentState == MediaPlayerState.PAUSED){
            mediaPlayer.seekTo(milliseconds);
        }
    }


    public void setCpuWakeLock(Context context){
            mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
    }


    void setStateToStopped(){
        currentState = MediaPlayerState.STOPPED;
    }


    void onReceiveBroadcastForPlay(){
        if(currentState == MediaPlayerState.PAUSED){
            resume();
        }
        else if(currentState == MediaPlayerState.STOPPED) {
            updateViewsEnsurePlayerStoppedAndSchedulePlay();
        }
    }


    private void updateViewsEnsurePlayerStoppedAndSchedulePlay() {
        mediaPlayerService.updateViewsForConnecting();
        stopRunningMediaPlayer();
        mediaPlayerService.stopUpdatingElapsedTimeOnView();
        elapsedTime = 0;
        shouldNextTrackPlayAfterCurrentTrackEnds = true;
        executorService.schedule(this::startTrack, 1, TimeUnit.MILLISECONDS);
    }


    void stopRunningMediaPlayer(){
        if(mediaPlayer != null && (mediaPlayer.isPlaying() || currentState == MediaPlayerState.PAUSED)){
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
        currentState = MediaPlayerState.STOPPED;
    }


    private void startTrack(){
        resetErrorStatus();
        try {
            isPreparingTrack.set(true);
            stopPlayer();
            createMediaPlayer();
            setDataSourceFromCurrentTrack();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepare();
            startUpdatingElapsedTimeOnView();
            currentState = MediaPlayerState.PLAYING;
            mediaPlayerService.notifyMainViewOfMediaPlayerPlaying();
        }catch (IOException e){
            log("IO exception trying to start track: " + currentTrack.getPathname());
            e.printStackTrace();
            onError();
            hasEncounteredError = true;
            mediaPlayerService.displayErrorOnMainView(currentTrack);
        }finally{
            isPreparingTrack.set(false);
            mediaPlayerService.updateNotification();
        }
    }


    private void stopPlayer(){
        releaseAndResetMediaPlayer();
        mediaPlayerService.updateNotification();
    }


    private void releaseAndResetMediaPlayer(){
        try {
            if (mediaPlayer != null) {
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }catch (RuntimeException e){
            Log.i("MediaPlayerHelper", "releaseAndResetMediaPlayerAndWifiLock() exception:  " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void createMediaPlayer(){
        mediaPlayer = new MediaPlayer();
        currentState = MediaPlayerState.STOPPED;
        mediaPlayer.setOnCompletionListener(this::onTrackFinished);
        setupErrorListener();
    }


    private void setDataSourceFromCurrentTrack() throws IOException{
        if(currentTrack.isUsingUri()){
            mediaPlayer.setDataSource(mediaPlayerService.getApplicationContext(), currentTrack.getUri());
            return;
        }
        mediaPlayer.setDataSource(currentTrack.getPathname());
    }


    public void startUpdatingElapsedTimeOnView(){
        updateElapsedTimeFuture = executorService.scheduleAtFixedRate(this::updateElapsedTimeOnView, 0L, 200L, TimeUnit.MILLISECONDS);
    }
}
