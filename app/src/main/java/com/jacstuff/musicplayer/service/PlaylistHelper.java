package com.jacstuff.musicplayer.service;

import android.app.NotificationManager;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.service.db.playlist.Playlist;
import com.jacstuff.musicplayer.service.db.search.TrackFinder;
import com.jacstuff.musicplayer.service.db.track.Track;
import com.jacstuff.musicplayer.service.playlist.PlaylistManager;
import com.jacstuff.musicplayer.service.playlist.PlaylistManagerImpl;
import com.jacstuff.musicplayer.service.playlist.PlaylistViewNotifier;
import com.jacstuff.musicplayer.service.playlist.PlaylistViewNotifierImpl;
import com.jacstuff.musicplayer.service.playlist.TrackLoader;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlaylistHelper {

    private AtomicBoolean isScanningForTracks;
    private PlaylistManager playlistManager;
    private final ScheduledExecutorService executorService;
    private final MediaPlayerService mediaPlayerService;
    private final MediaNotificationManager mediaNotificationManager;
    private PlaylistViewNotifier playlistViewNotifier;
    private boolean haveTracksBeenLoaded;
    private TrackLoader trackLoader;
    private TrackFinder trackFinder;

    public PlaylistHelper(MediaPlayerService mediaPlayerService){
        this.mediaPlayerService = mediaPlayerService;
        this.mediaNotificationManager = mediaPlayerService.getNotificationManager();
        executorService = Executors.newScheduledThreadPool(3);
    }


    public void loadTrackDataFromFilesystem(){
        isScanningForTracks.set(true);
        executorService.execute(()->{
            playlistManager.addTracksFromStorage(mediaPlayerService);
            playlistManager.loadAllTracksPlaylist();
            mediaPlayerService.updateListViews();
            mediaPlayerService.setCurrentTrackAndUpdatePlayerViewVisibility();
            isScanningForTracks.set(false);
        });
    }


    private void initTrackFinder(){
        if(trackFinder == null){
            trackFinder =  new TrackFinder(trackLoader);
        }
    }


    public List<Track> searchForTracks(String str){
        initTrackFinder();
        return trackFinder == null ? Collections.emptyList() : trackFinder.searchFor(str);
    }


    public void onSetActivity(MainActivity mainActivity){
        playlistViewNotifier = new PlaylistViewNotifierImpl(mainActivity);
        createPlaylistManagerAndTrackLoader();
        if(!haveTracksBeenLoaded){
            loadTrackDataFromFilesystem();
            haveTracksBeenLoaded = true;
            return;
        }
        mediaPlayerService.updateViews();
    }


    public void createPlaylistManagerAndTrackLoader(){
        if(playlistManager == null) {
            trackLoader = new TrackLoader(mediaPlayerService.getApplicationContext());
            playlistManager = new PlaylistManagerImpl(mediaPlayerService.getApplicationContext(), trackLoader);
        }
    }


    public void refreshTrackDataFromFilesystem(){
        if(isScanningForTracks.get()){
            return;
        }
        isScanningForTracks.set(true);
        executorService.execute(()->{
            playlistManager.addTracksFromStorage( mediaPlayerService);
            mediaPlayerService.updateListViews();
            initTrackFinder();
            trackFinder.initCache();
            isScanningForTracks.set(false);
        });
    }


    public void loadTracksFromArtist(String artistName){
        playlistManager.loadTracksFromArtist(artistName);
        mediaPlayerService.updateViewTrackListAndDeselectList();
        mediaPlayerService.updateAlbumsView();
    }


    public void loadTracksFromAlbum(String albumName){
        playlistManager.loadTracksFromAlbum(albumName);
        mediaPlayerService.updateViewTrackListAndDeselectList();
    }


    public void addTracksFromAristToCurrentPlaylist(String artistName){
        playlistManager.addTracksFromArtistToCurrentPlaylist(artistName, playlistViewNotifier);
        mediaPlayerService.updateViewTrackList();
    }


    public void addTracksFromAlbumToCurrentPlaylist(String albumName){
        playlistManager.addTracksFromAlbumToCurrentPlaylist(albumName, playlistViewNotifier);
        mediaPlayerService.updateViewTrackList();
    }


    public void loadPlaylist(Playlist playlist){
        playlistManager.loadPlaylist(playlist);
        mediaPlayerService.updateViewTrackListAndDeselectList();
        mediaPlayerService.updateAlbumsView();
    }


    public void addTrackToCurrentPlaylist(Track track){
        playlistManager.addTrackToCurrentPlaylist(track, playlistViewNotifier);
        mediaPlayerService.updateViewTrackList();
        mediaNotificationManager.updateNotification();
    }

    public void addTrackToPlaylist(Track track, Playlist playlist){
        playlistManager.addTrackToPlaylist(track, playlist, playlistViewNotifier);
        mediaPlayerService.updateViewTrackList();
        mediaNotificationManager.updateNotification();
    }


    public void removeTrackFromCurrentPlaylist(Track track){
        playlistManager.removeTrackFromCurrentPlaylist(track, playlistViewNotifier);
        mediaPlayerService.updateViewTrackList();
        mediaNotificationManager.updateNotification();
    }


}
