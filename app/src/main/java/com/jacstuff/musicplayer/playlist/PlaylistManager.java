package com.jacstuff.musicplayer.playlist;

import com.jacstuff.musicplayer.db.album.Album;
import com.jacstuff.musicplayer.db.playlist.Playlist;
import com.jacstuff.musicplayer.db.track.Track;
import com.jacstuff.musicplayer.service.MediaPlayerService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PlaylistManager {

    boolean hasTracksQueued();
    boolean isUserPlaylistLoaded();
    boolean hasAnyTracks();
    int getNumberOfTracks();
    Set<String> getArtists();
    Map<String, Album> getAlbums();
    ArrayList<String> getAlbumNames();
    ArrayList<String> getArtistNames();

    List<Track> getTracks();
    Track getNextTrack();
    Track getPreviousTrack();
    Track getNextRandomUnPlayedTrack();
    Track selectTrack(int index);

    void addToTrackHistory(Track track);
    void addTracksFromStorage(MediaPlayerService mediaPlayerService);
    void addTrackToQueue(Track track);

    void loadPlaylist(Playlist playlist);
    void loadAllTracksPlaylist();
    void loadTracksFromAlbum(String albumName);
    void loadTracksFromArtist(String artistName);

    void addTrackToCurrentPlaylist(Track track, PlaylistViewNotifier playlistViewNotifier);
    void addTrackToPlaylist(Track track, Playlist playlist, PlaylistViewNotifier playlistViewNotifier);
    void addTracksFromArtistToCurrentPlaylist(String artistName, PlaylistViewNotifier playlistViewNotifier);
    void addTracksFromAlbumToCurrentPlaylist(String albumName, PlaylistViewNotifier playlistViewNotifier);
    void removeTrackFromCurrentPlaylist(Track track, PlaylistViewNotifier playlistViewNotifier);

    void enableShuffle();
    void disableShuffle();
    boolean isShuffleEnabled();

    void onlyDisplayMainArtists(boolean shouldOnlyDisplayMainArtists);
    String getTrackNameAt(int position);
    void deleteAll();
}
