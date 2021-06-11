package com.jacstuff.musicplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.jacstuff.musicplayer.playlist.PlaylistManager;
import com.jacstuff.musicplayer.playlist.PlaylistManagerImpl;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.jacstuff.musicplayer.HandlerCode.ASSIGN_NEXT_TRACK;
import static com.jacstuff.musicplayer.HandlerCode.PLAYLIST_REFRESHED;
import static com.jacstuff.musicplayer.HandlerCode.UPDATE_TIME;

public class MediaControllerImpl implements MediaController {


    private PlaylistManager playlistManager;
    private MediaPlayer mediaPlayer;
    private MediaPlayerView view;
    private TrackDetails currentTrackDetails;
    private static Handler handler;
    private ScheduledExecutorService scheduledExecutor;

    private ExecutorService executorService;
    private TrackTimeUpdater trackTimeUpdater;
    private enum State { PLAYING, PAUSED, STOPPED}
    private State state;

    MediaControllerImpl(Context context, final MediaPlayerView view){

        playlistManager = new PlaylistManagerImpl(context);
        mediaPlayer = new MediaPlayer();
        executorService = Executors.newSingleThreadExecutor();

        this.state = State.STOPPED;
        this.view = view;
        setupMediaPlayerListeners();
        setupHandler();
        trackTimeUpdater = new TrackTimeUpdater(mediaPlayer, handler);
    }


    private void setupMediaPlayerListeners(){

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                assignNextTrack();
                play();
            }
        });
    }

    public List<TrackDetails> getTrackDetailsList(){
        return playlistManager.getTracks();
    }


    private void setupHandler(){
        handler = new Handler(Looper.getMainLooper()){


            public void handleMessage(Message inputMessage){

                switch(inputMessage.what){

                    case UPDATE_TIME:

                        String time = (String)inputMessage.obj;
                        view.setElapsedTime(time);
                        break;

                    case PLAYLIST_REFRESHED:
                        view.displayPlaylistRefreshedMessage();
                        updateViewTrackList();
                        break;

                    case ASSIGN_NEXT_TRACK:
                        assignNextTrack();
                        view.enableControls();
                        updateViewTrackList();
                }
            }
        };

    }


    private void updateViewTrackList(){
        List<TrackDetails> trackDetailsList = playlistManager.getTracks();
        log("track details list size: " + trackDetailsList.size());
        view.refreshTrackList(playlistManager.getTracks());
        view.scrollToListPosition(playlistManager.getCurrentTrackIndex());
    }

    @Override
    public void initPlaylistAndRefreshView(){
        initPlaylist();

    }

    public void finish(){
        log("Entered finish()");
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
    }


    private boolean isRefreshing = false;

    @Override
    public void refreshPlaylist() {
        if(isRefreshing){
            return;
        }
        isRefreshing = true;
        executorService.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        playlistManager.refreshPlaylist();
                        Message msg = handler.obtainMessage(PLAYLIST_REFRESHED, null);
                        msg.sendToTarget();
                        isRefreshing = false;
                    }
                });
    }


    private void initPlaylist(){

        executorService.execute(new Runnable(){
            public void run(){
                playlistManager.init();
                Message msg = handler.obtainMessage(ASSIGN_NEXT_TRACK, null);
                msg.sendToTarget();
            }
        });
    }



    @Override
    public void togglePlay(){

        switch(state){

            case PAUSED:
                resume();
                return;
            case PLAYING:
                pause();
                return;
            case STOPPED:
                play();

        }
    }

    private  void resume(){
        scheduledExecutor = new ScheduledThreadPoolExecutor(1);
        scheduledExecutor.scheduleAtFixedRate(trackTimeUpdater, 0, 1 ,TimeUnit.SECONDS);
        view.showPauseIcon();
        state = State.PLAYING;
        mediaPlayer.start();

    }

    @Override
    public void play() {

            scheduledExecutor = new ScheduledThreadPoolExecutor(1);
            scheduledExecutor.scheduleAtFixedRate(trackTimeUpdater, 0, 1 ,TimeUnit.SECONDS);
            view.showPauseIcon();
            state = State.PLAYING;
            mediaPlayer.start();
          //  mediaPlayer.setVolume();
    }

    @Override
    public void stop() {
        if(scheduledExecutor == null){
            return;
        }
        scheduledExecutor.shutdownNow();
        if(mediaPlayer == null){
            return;
        }
        this.state = State.STOPPED;
        mediaPlayer.stop();
    }


    @Override
    public void pause(){
        scheduledExecutor.shutdownNow();
        state = State.PAUSED;
        view.showPlayIcon();
        mediaPlayer.pause();
    }

    public void selectTrack(int index){
        boolean previousTrackWasPlaying = state == State.PLAYING;
        assignNextTrack(playlistManager.getTrackDetails(index));

        if(previousTrackWasPlaying){
            play();
        }
    }




    private void assignNextTrack(){
        assignNextTrack(playlistManager.getNextRandomUnplayedTrack());
        view.scrollToListPosition(playlistManager.getCurrentTrackIndex());
    }


    public int getNumberOfTracks(){
        return playlistManager.getNumberOfTracks();
    }


    private void assignNextTrack(TrackDetails trackDetails){
        currentTrackDetails = trackDetails;
        if(currentTrackDetails == null){
            log("Current Track Details is null");
            return;
        }
        String currentTrackPath = currentTrackDetails.getPathname();
        mediaPlayer.reset();
        log("current path: "+  currentTrackPath);
        if(currentTrackPath == null){
            view.setTrackInfo("");
            state = State.STOPPED;
            return;
        }
        try {
            setTrackInfoOnView();
            mediaPlayer.setDataSource(currentTrackPath);
            mediaPlayer.prepare();
            view.setTotalTrackTime(TimeConverter.convert(mediaPlayer.getDuration()));

        }catch (IOException e){
            log("File not found!");
        }
    }


    public String getTrackNameAt(int position){
        return playlistManager.getTrackNameAt(position);

    }


    @Override
    public void next() {
        boolean previousTrackWasPlaying = state == State.PLAYING;
        this.stop();
        assignNextTrack();
        if(previousTrackWasPlaying) {
            log("next() - not paused, so playing track!");
            this.play();
        }
    }


    private void log(String msg){
        Log.i("MediaControllerImpl", msg);

    }


    private void setTrackInfoOnView(){
        //MediaPlayer.TrackInfo[] trackInfo = mediaPlayer.getTrackInfo();
        //MediaPlayer.TrackInfo metadata = trackInfo[MEDIA_TRACK_TYPE_METADATA];
        //metadata.
        if(currentTrackDetails == null){
            return;
        }
        view.setTrackInfo(currentTrackDetails.getName());
        view.setAlbumInfo(currentTrackDetails.getAlbum());
        view.setArtistInfo(currentTrackDetails.getArtist());
    }


}
