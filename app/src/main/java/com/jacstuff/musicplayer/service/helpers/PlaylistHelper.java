package com.jacstuff.musicplayer.service.helpers;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.service.MediaNotificationManager;
import com.jacstuff.musicplayer.service.MediaPlayerService;
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
        executorService.execute(()->{
            playlistManager.addTracksFromStorage(mediaPlayerService);
            playlistManager.loadAllTracksPlaylist();
            mediaPlayerService.updateListViews(playlistManager);
            mediaPlayerService.setCurrentTrackAndUpdatePlayerViewVisibility();
            isScanningForTracks.set(false);
        });
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
            playlistManager.addTracksFromStorage(mediaPlayerService);
            mediaPlayerService.updateListViews(playlistManager);
            initTrackFinder();
            trackFinder.initCache();
            mediaPlayerService.setCurrentTrackAndUpdatePlayerViewVisibility();
            isScanningForTracks.set(false);
        });
    }


    public void loadTracksFromArtist(String artistName){
        playlistManager.loadTracksFromArtist(artistName);
        mediaPlayerService.updateViewTrackListAndDeselectList(playlistManager);
        mediaPlayerService.updateAlbumsView();
    }


    public void loadTracksFromAlbum(String albumName){
        boolean isAlbumLoaded = playlistManager.loadTracksFromAlbum(albumName);
        if(isAlbumLoaded){
            mediaPlayerService.updateViewTrackListAndDeselectList(playlistManager);
            mediaPlayerService.notifyViewToDeselectPlaylistAndArtistTabs();
            return;
        }
        mediaPlayerService.notifyViewOfAlbumNotLoaded(albumName);
    }


    public void loadTracksFromGenre(String genreName){
        boolean isGenreLoaded = playlistManager.loadTracksFromGenre(genreName);
        if(isGenreLoaded){
            mediaPlayerService.updateViewTrackListAndDeselectList(playlistManager);
            mediaPlayerService.notifyViewToDeselectEverythingButGenre();
            return;
        }
        mediaPlayerService.notifyViewOfAlbumNotLoaded(genreName);
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
        if (playlist.getId() == PlaylistManagerImpl.ALL_TRACKS_PLAYLIST_ID) {
            mediaPlayerService.updateAlbumsView();
        }
    }


    public void loadAlbumOfTrack(Track track){
        loadTracksFromAlbum(track.getAlbum());
    }


    public void loadArtistOfTrack(Track track){
        loadTracksFromArtist(track.getArtist());
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
