package com.jacstuff.musicplayer.service.playlist;

import com.jacstuff.musicplayer.service.db.entities.Playlist;
import com.jacstuff.musicplayer.service.db.entities.Track;
import com.jacstuff.musicplayer.service.MediaPlayerService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface PlaylistManager {

    boolean isUserPlaylistLoaded();
    boolean hasAnyTracks();
    int getNumberOfTracks();
    ArrayList<String> getAlbumNames();
    ArrayList<String> getArtistNames();
    ArrayList<String> getGenreNames();

    Playlist getCurrentPlaylist();
    Track getNextTrack();
    Optional<Track> getPreviousTrack();
    Track selectTrack(int index);
    void assignCurrentIndexIfApplicable(Track track);

    void addToTrackHistory(Track track);
    void addTracksFromStorage(MediaPlayerService mediaPlayerService);
    void addTrackToQueue(Track track);

    List<Playlist> getAllUserPlaylists();
    void loadPlaylist(Playlist playlist);
    void loadAllTracksPlaylist();
    boolean loadTracksFromAlbum(String albumName);
    boolean loadTracksFromGenre(String genreName);
    void loadTracksFromArtist(String artistName);

    void addTrackToCurrentPlaylist(Track track, PlaylistViewNotifier playlistViewNotifier);
    void addTrackToPlaylist(Track track, Playlist playlist, PlaylistViewNotifier playlistViewNotifier);
    void addTracksFromArtistToCurrentPlaylist(String artistName, PlaylistViewNotifier playlistViewNotifier);
    void addTracksFromAlbumToCurrentPlaylist(String albumName, PlaylistViewNotifier playlistViewNotifier);
    void removeTrackFromCurrentPlaylist(Track track, PlaylistViewNotifier playlistViewNotifier);

    int getCurrentIndexOf(Track track);
    int getCurrentIndex();

    void enableShuffle();
    void disableShuffle();
    boolean isShuffleEnabled();

}
