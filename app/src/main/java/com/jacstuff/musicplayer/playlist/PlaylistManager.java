package com.jacstuff.musicplayer.playlist;

import com.jacstuff.musicplayer.db.album.Album;
import com.jacstuff.musicplayer.db.artist.Artist;
import com.jacstuff.musicplayer.db.playlist.Playlist;
import com.jacstuff.musicplayer.db.track.Track;
import com.jacstuff.musicplayer.service.MediaPlayerService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PlaylistManager {

    void savePlaylist();
    Track getNextTrack();
    boolean hasTracksQueued();
    boolean hasBeenInitialized();
    boolean isUserPlaylistLoaded();
    boolean hasAnyTracks();
    Track getPreviousTrack();
    Track getNextRandomUnPlayedTrack();
    Track selectTrack(int index);
    void addTracksFromStorage(MediaPlayerService mediaPlayerService);
    List<Track> getTracks();
    void init();
    int getCurrentTrackIndex();
    boolean areAllTracksLoaded();
    void addTrackToQueue(Track track);
    void loadPlaylist(Playlist playlist);
    void loadAllTracksPlaylist();
    void addTrackToCurrentPlaylist(Track track);
    void addTracksToCurrentPlaylist(List<Track> tracks);
    void removeTrackFromCurrentPlaylist(Track track);
    int getNumberOfTracks();
    void enableShuffle();
    void disableShuffle();
    boolean isShuffleEnabled();
    void onDestroy();
    void addTracksFromArtistToCurrentPlaylist(String artistName);
    void addTracksFromAlbumToCurrentPlaylist(String albumName);

    Set<String> getArtists();
    void loadTracksFromAlbum(String albumName);
    Map<String, Album> getAlbums();
    ArrayList<String> getAlbumNames();
    void loadTracksFromArtist(String artistName);
    ArrayList<String> getArtistNames();

    String getTrackNameAt(int position);

    void deleteAll();
}
