package com.jacstuff.musicplayer.service.helpers;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.service.MediaNotificationManager;
import com.jacstuff.musicplayer.service.MediaPlayerService;
import com.jacstuff.musicplayer.service.db.entities.Playlist;
import com.jacstuff.musicplayer.service.db.search.TrackFinder;
import com.jacstuff.musicplayer.service.db.entities.Track;
import com.jacstuff.musicplayer.service.playlist.IndexManager;
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

    private final AtomicBoolean isScanningForTracks = new AtomicBoolean();
    private PlaylistManager playlistManager;
    private final ScheduledExecutorService executorService;
    private final MediaPlayerService mediaPlayerService;
    private MediaNotificationManager mediaNotificationManager;
    private PlaylistViewNotifier playlistViewNotifier;
    private boolean haveTracksBeenLoaded;
    private TrackLoader trackLoader;
    private TrackFinder trackFinder;

    public PlaylistHelper(MediaPlayerService mediaPlayerService){
        this.mediaPlayerService = mediaPlayerService;
        executorService = Executors.newScheduledThreadPool(3);
    }

    public void setMediaNotificationManager(MediaNotificationManager mediaNotificationManager){
        this.mediaNotificationManager = mediaNotificationManager;
    }


    private void initTrackFinder(){
        if(trackFinder == null){
            trackFinder =  new TrackFinder(trackLoader);
        }
    }


    public int getIndexOfCurrentTrack(){
        return playlistManager.getCurrentIndex();
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
        mediaPlayerService.updateViews(playlistManager);
    }


    public void loadTrackDataFromFilesystem(){
        isScanningForTracks.set(true);
        executorService.execute(() -> {
            playlistManager.addTracksFromStorage(mediaPlayerService);
            playlistManager.loadAllTracksPlaylist();
            mediaPlayerService.updateListViews(playlistManager);
            mediaPlayerService.setFirstTrackAndUpdateViewVisibility();
            isScanningForTracks.set(false);
        });
    }


    public void createPlaylistManagerAndTrackLoader(){
        if(playlistManager == null) {
            trackLoader = new TrackLoader(mediaPlayerService.getApplicationContext());
            playlistManager = new PlaylistManagerImpl(mediaPlayerService.getApplicationContext(), trackLoader, new IndexManager());
        }
    }


    public void refreshTrackDataFromFilesystem(){
        if(isScanningForTracks.get()){
            return;
        }
        isScanningForTracks.set(true);
        executorService.execute(()->{
            playlistManager.addTracksFromStorage(mediaPlayerService);
            mediaPlayerService.updateListViews(playlistManager);
            initTrackFinder();
            trackFinder.initCache();
            mediaPlayerService.setCurrentTrackAndUpdateViewVisibility();
            isScanningForTracks.set(false);
        });
    }


    public void loadTracksFromArtist(String artistName){
        loadTracksFromArtist(artistName, true);
    }


    public void loadTracksFromArtist(String artistName, boolean shouldAttemptAutoloadOfNextTrack){
        playlistManager.loadTracksFromArtist(artistName);
        mediaPlayerService.updateViewTrackListAndDeselectList(playlistManager);
        mediaPlayerService.updateAlbumsView();
        mediaPlayerService.notifyViewToDeselectNonArtistLists();
        if(shouldAttemptAutoloadOfNextTrack){
            autoLoadNextTrack();
        }
    }


    public void loadWholeAlbumOf(Track track){
        String albumName = track.getAlbum();
        boolean isAlbumLoaded = playlistManager.loadAllTracksFromAlbum(albumName);
        handleAlbumLoaded(albumName, isAlbumLoaded, false);
    }


    public void loadTracksFromAlbum(String albumName){
        loadTracksFromAlbum(albumName, true);
    }


    public void loadTracksFromAlbum(String albumName, boolean shouldAttemptAutoloadOfNextTrack){
        boolean isAlbumLoaded = playlistManager.loadTracksFromAlbum(albumName);
        handleAlbumLoaded(albumName, isAlbumLoaded, shouldAttemptAutoloadOfNextTrack);
    }


    public void handleAlbumLoaded(String albumName, boolean wasAlbumLoaded, boolean isNextTrackToBeLoaded){
        if(wasAlbumLoaded){
            mediaPlayerService.updateViewTrackListAndDeselectList(playlistManager);
            mediaPlayerService.notifyViewToDeselectPlaylistAndArtistTabs();
            if(isNextTrackToBeLoaded) {
                autoLoadNextTrack();
            }
            return;
        }
        mediaPlayerService.notifyViewOfAlbumNotLoaded(albumName);
    }


    public void loadTracksFromGenre(String genreName){
        boolean isGenreLoaded = playlistManager.loadTracksFromGenre(genreName);
        mediaPlayerService.updateAlbumsView();
        if(isGenreLoaded){
            mediaPlayerService.updateViewTrackListAndDeselectList(playlistManager);
            mediaPlayerService.notifyViewToDeselectEverythingButGenre();
            autoLoadNextTrack();
            return;
        }
        mediaPlayerService.notifyViewOfGenreNotLoaded(genreName);
    }


    public void addTracksFromAristToCurrentPlaylist(String artistName){
        playlistManager.addTracksFromArtistToCurrentPlaylist(artistName, playlistViewNotifier);
        mediaPlayerService.updateViewTrackList(playlistManager);
    }


    public void addTracksFromAlbumToCurrentPlaylist(String albumName){
        playlistManager.addTracksFromAlbumToCurrentPlaylist(albumName, playlistViewNotifier);
        mediaPlayerService.updateViewTrackList(playlistManager);
    }


    public void loadPlaylist(Playlist playlist) {
        playlistManager.loadPlaylist(playlist);
        mediaPlayerService.updateViewTrackListAndDeselectList(playlistManager);
        mediaPlayerService.updateAlbumsView();
        autoLoadNextTrack();
    }


    private void autoLoadNextTrack(){
        if(mediaPlayerService.getPreferencesHelper().isNextTrackLoadedAutomatically()){
            mediaPlayerService.loadNextTrack();
        }
    }


    public void loadArtistOfTrack(Track track){
        loadTracksFromArtist(track.getArtist(), false);
    }


    public PlaylistManager getPlaylistManager(){
        return playlistManager;
    }


    public void addTrackToCurrentPlaylist(Track track){
        playlistManager.addTrackToCurrentPlaylist(track, playlistViewNotifier);
        mediaPlayerService.updateViewTrackList(playlistManager);
        mediaNotificationManager.updateNotification();
    }


    public void addTrackToPlaylist(Track track, Playlist playlist){
        playlistManager.addTrackToPlaylist(track, playlist, playlistViewNotifier);
        mediaPlayerService.updateViewTrackList(playlistManager);
        mediaNotificationManager.updateNotification();
    }


    public void removeTrackFromCurrentPlaylist(Track track){
        playlistManager.removeTrackFromCurrentPlaylist(track, playlistViewNotifier);
        mediaPlayerService.updateViewTrackList(playlistManager);
        mediaNotificationManager.updateNotification();
    }


    public int getTrackCount(){
       return playlistManager == null ? 0 : playlistManager.getNumberOfTracks();
    }

}
