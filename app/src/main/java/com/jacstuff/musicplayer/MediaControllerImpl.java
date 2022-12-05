package com.jacstuff.musicplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.jacstuff.musicplayer.db.track.Track;
import com.jacstuff.musicplayer.playlist.PlaylistManager;
import com.jacstuff.musicplayer.playlist.PlaylistManagerImpl;
import com.jacstuff.musicplayer.viewmodel.MainViewModel;

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


    private final PlaylistManager playlistManager;
    private MediaPlayer mediaPlayer;
    private final MediaPlayerView view;
    private Track currentTrack;
    private static Handler handler;
    private ScheduledExecutorService scheduledExecutor;

    private final ExecutorService executorService;
    private final TrackTimeUpdater trackTimeUpdater;
    private enum State { PLAYING, PAUSED, STOPPED}
    private State state;
    private boolean isRefreshing = false;
    private final MainViewModel viewModel;


    public MediaControllerImpl(Context context, final MediaPlayerView view, MainViewModel viewModel){
        mediaPlayer = new MediaPlayer();
        executorService = Executors.newSingleThreadExecutor();
        this.state = State.STOPPED;
        this.view = view;
        this.viewModel = viewModel;
        playlistManager = new PlaylistManagerImpl(context, view, viewModel);
        setupMediaPlayerListeners();
        setupHandler();
        trackTimeUpdater = new TrackTimeUpdater(mediaPlayer, handler);
    }


    private void setupMediaPlayerListeners(){
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            assignNextTrack();
            play();
        });
    }


    public List<Track> getTrackDetailsList(){
        return playlistManager.getTracks();
    }


    private void setupHandler(){
        handler = new Handler(Looper.getMainLooper()){
            public void handleMessage(@androidx.annotation.NonNull Message inputMessage){
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
        view.refreshTrackList(playlistManager.getTracks());
        view.scrollToListPosition(playlistManager.getCurrentTrackIndex());
    }


    @Override
    public void initPlaylistAndRefreshView(){
        initPlaylist();

    }


    public void finish(){
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
    }


    @Override
    public void scanForTracks() {
        if(isRefreshing){
            return;
        }
        isRefreshing = true;
        executorService.execute(() -> {
            playlistManager.addTracksFromStorage();
            Message msg = handler.obtainMessage(PLAYLIST_REFRESHED, null);
            msg.sendToTarget();
            isRefreshing = false;
        });
    }


    private void initPlaylist(){
        executorService.execute(() -> {
            playlistManager.init();
            Message msg = handler.obtainMessage(ASSIGN_NEXT_TRACK, null);
            msg.sendToTarget();
        });
    }


    @Override
    public void togglePlay(){
        if(currentTrack == null){
            return;
        }
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
    public Track getCurrentTrack(){
        return currentTrack;
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
        assignNextTrack(playlistManager.getTrackDetails(index));
        boolean previousTrackWasPlaying = state == State.PLAYING;
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


    private void assignNextTrack(Track track){
        currentTrack = track;
        if(currentTrack == null){
            return;
        }
        mediaPlayer.reset();
        if(currentTrack.getPathname() == null) {
            handleNullPathname();
            return;
        }
        initTrack();
    }


    public String getTrackNameAt(int position){
        return playlistManager.getTrackNameAt(position);
    }


    private void handleNullPathname(){
        if(currentTrack.getPathname() == null){
            view.setTrackInfo("");
            state = State.STOPPED;
        }
    }


    private void initTrack(){
        try {
            setTrackInfoOnView();
            mediaPlayer.setDataSource(currentTrack.getPathname());
            mediaPlayer.prepare();
            view.setTotalTrackTime(TimeConverter.convert(mediaPlayer.getDuration()));

        }catch (IOException e){
            e.printStackTrace();
        }
    }


    @Override
    public void next() {
        boolean previousTrackWasPlaying = state == State.PLAYING;
        this.stop();
        assignNextTrack();
        if(previousTrackWasPlaying) {
            this.play();
        }
    }


    private void setTrackInfoOnView(){
        if(currentTrack == null){
            return;
        }
        view.setTrackInfo(currentTrack.getName());
        view.setAlbumInfo(currentTrack.getAlbum());
        view.setArtistInfo(currentTrack.getArtist());
    }

}
