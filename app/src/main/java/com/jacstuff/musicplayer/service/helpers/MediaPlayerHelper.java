package com.jacstuff.musicplayer.service.helpers;

import android.content.Context;
import android.media.MediaPlayer;
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
        resetElapsedTime();
        stopUpdatingElapsedTime();
        if(track == null || track.getPathname() == null){
            mediaPlayerService.setBlankTrackInfoOnMainView();
            setStateToStopped();
            return;
        }
        setupCurrentTrack();
    }


    private void setupCurrentTrack(){
        assignAlbumArt(currentTrack);
        mediaPlayerService.updateViewsOnTrackAssigned();
        if(currentState == MediaPlayerState.PAUSED){
            stopPlayerAndElapsedTime();
        }
        else if(currentState == MediaPlayerState.PLAYING){
            stopPlayerAndSchedulePlay();
        }
    }


    private boolean isTrackPlayingOrPaused(){
        return currentState == MediaPlayerState.PLAYING || currentState == MediaPlayerState.PAUSED;
    }


    public void selectAndPlayTrack(Track track){
        cancelScheduledStoppageOfTrack();
        currentTrack = track;
        assignAlbumArt(track);
        stopPlayerAndSchedulePlay();
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
            hasEncounteredError = true;
        } catch(RuntimeException e){
            hasEncounteredError = true;
            mediaPlayerService.notifyMainViewThatFileDoesNotExist(track);
        }
        if(hasEncounteredError){
            mediaPlayerService.setBlankAlbumArt();
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


    public String getCurrentUrl(){
        return currentTrack == null ? "" : currentTrack.getPathname();
    }


    public void playTrack(){
        if(currentState == MediaPlayerState.STOPPED || currentState == MediaPlayerState.FINISHED){
            stopPlayerAndSchedulePlay();
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
        stopIfPlayingOrPaused();
        stopUpdatingElapsedTime();
        resetElapsedTime();
        if(shouldUpdateNotification) {
            mediaPlayerService.updateNotification();
        }
        mediaPlayerService.updateMainViewOfStop(shouldUpdateMainView);
        cancelScheduledStoppageOfTrack();
    }


    public boolean isPaused(){
        return currentState == MediaPlayerState.PAUSED;
    }


    private void stopIfPlayingOrPaused(){
        if(isTrackPlayingOrPaused()) {
            mediaPlayer.stop();
            currentState = MediaPlayerState.STOPPED;
            mediaPlayer.reset();
        }
    }


    private void resetElapsedTime(){
        elapsedTime = 0;
    }


    public void enabledStopAfterTrackFinishes(){
        if(currentState == MediaPlayerState.PLAYING) {
            shouldNextTrackPlayAfterCurrentTrackEnds = false;
        }
    }


    public void pauseMediaPlayer(){
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            stopUpdatingElapsedTime();
            currentState = MediaPlayerState.PAUSED;
        }
    }


    public void stopUpdatingElapsedTime(){
        if(updateElapsedTimeFuture == null || updateElapsedTimeFuture.isCancelled()){
            return;
        }
        updateElapsedTimeFuture.cancel(false);
    }


    private void onTrackFinished(MediaPlayer mediaPlayer){
        currentState = MediaPlayerState.FINISHED;
        stopUpdatingElapsedTime();
        mediaPlayer.reset();
        mediaPlayerService.loadNextTrack();
        if(shouldNextTrackPlayAfterCurrentTrackEnds) {
            stopPlayerAndSchedulePlay();
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
            stopPlayerAndSchedulePlay();
        }
    }


    private void stopPlayerAndSchedulePlay() {
        mediaPlayerService.updateViewsForConnecting();
        stopPlayerAndElapsedTime();
        executorService.schedule(this::startTrack, 1, TimeUnit.MILLISECONDS);
    }


    private void stopPlayerAndElapsedTime(){
        stopRunningMediaPlayer();
        stopUpdatingElapsedTime();
        resetElapsedTime();
        shouldNextTrackPlayAfterCurrentTrackEnds = true;
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
            printError(e);
            onError();
            hasEncounteredError = true;
            mediaPlayerService.displayErrorOnMainView(currentTrack);
        }finally{
            isPreparingTrack.set(false);
            mediaPlayerService.updateNotification();
        }
    }

    private void printError(Exception e){
        e.printStackTrace();
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
        mediaPlayer.setDataSource(currentTrack.getPathname());
    }


    public void startUpdatingElapsedTimeOnView(){
        updateElapsedTimeFuture = executorService.scheduleAtFixedRate(this::updateElapsedTimeOnView, 0L, 200L, TimeUnit.MILLISECONDS);
    }
}
