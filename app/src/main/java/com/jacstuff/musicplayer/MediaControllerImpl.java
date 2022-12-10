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


    private final MediaPlayerView view;
    private Track currentTrack;
    private static Handler handler;
    private ScheduledExecutorService scheduledExecutor;
    private PlaylistManager playlistManager;

    private final ExecutorService executorService;
   // private final TrackTimeUpdater trackTimeUpdater;
    private enum State { PLAYING, PAUSED, STOPPED}
    private State state;
    private boolean isRefreshing = false;
    private final MainViewModel viewModel;


    public MediaControllerImpl(Context context, final MediaPlayerView view, MainViewModel viewModel){
        executorService = Executors.newSingleThreadExecutor();
        this.state = State.STOPPED;
        this.view = view;
        this.viewModel = viewModel;

        //setupMediaPlayerListeners();
        setupHandler();
      //  trackTimeUpdater = new TrackTimeUpdater(mediaPlayer, handler);
    }

/*
    private void setupMediaPlayerListeners(){
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            assignNextTrack();
            play();
        });
    }


 */


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

    private void log(String msg){
        System.out.println("^^^ MediaControllerImpl: " + msg);
    }

    private void updateViewTrackList(){
        log("Entered updateViewTrackList");
        view.refreshTrackList(playlistManager.getTracks());
        view.scrollToListPosition(playlistManager.getCurrentTrackIndex());
    }


    @Override
    public void initPlaylistAndRefreshView(){
        initPlaylist();
    }


    public void finish(){
    }


    private void initPlaylist(){
        executorService.execute(() -> {
            playlistManager.init();
            Message msg = handler.obtainMessage(ASSIGN_NEXT_TRACK, null);
            msg.sendToTarget();
        });
    }



    public void togglePlay(){
        if(currentTrack == null){
            return;
        }
        switch(state){
            case PAUSED:
                //resume();
                return;
            case PLAYING:
                pause();
                return;
            case STOPPED:
                play();
        }
    }


    @Override
    public void play() {

    }

    @Override
    public Track getCurrentTrack(){
        return currentTrack;
    }


    @Override
    public void pause(){
        state = State.PAUSED;
    }


    public void selectTrack(int index){
        assignNextTrack(playlistManager.getTrackDetails(index));
    }


    public int getNumberOfTracks(){
        return playlistManager.getNumberOfTracks();
    }


    @Override
    public void next() {
        assignNextTrack();
    }


    private void assignNextTrack(){
        assignNextTrack(playlistManager.getNextRandomUnplayedTrack());
        view.scrollToListPosition(playlistManager.getCurrentTrackIndex());
    }


    private void assignNextTrack(Track track){
        currentTrack = track;
        if(currentTrack == null){
            return;
        }
        if(currentTrack.getPathname() == null) {
            handleNullPathname();
            return;
        }
        setTrackInfoOnView();
        //view.setTotalTrackTime(TimeConverter.convert(mediaPlayer.getDuration()));
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


    private void setTrackInfoOnView(){
        if(currentTrack == null){
            return;
        }
        view.setTrackInfo(currentTrack.getName());
        view.setAlbumInfo(currentTrack.getAlbum());
        view.setArtistInfo(currentTrack.getArtist());
    }

}
