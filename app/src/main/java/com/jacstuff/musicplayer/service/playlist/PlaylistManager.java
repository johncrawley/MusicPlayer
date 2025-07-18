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

    void addRandomTracksToPlaylist(RandomTrackConfig randomTrackConfig, PlaylistViewNotifier playlistViewNotifier);

    int getNumberOfTracks();
    ArrayList<String> getAlbumNames();
    ArrayList<String> getAllAlbumNamesAndClearCurrentArtist();
    ArrayList<String> getArtistNames();
    ArrayList<String> getGenreNames();

    Optional<String> getCurrentArtistName();

    Playlist getCurrentPlaylist();
    Optional<Track> getNextTrack();
    Optional<Track> getFirstTrack();
    Optional<Track> getPreviousTrack();
    Track selectTrack(int index);
    void assignCurrentIndexIfApplicable(Track track);

    void addToTrackHistory(Track track);
    void addTracksFromStorage(MediaPlayerService mediaPlayerService);
    void addTrackToQueue(Track track);

    List<Playlist> getAllUserPlaylists();
    List<Playlist> getAllPlaylists();
    void deletePlaylist(Playlist playlist);
    void clearTracksFromPlaylist(long playlistId, PlaylistViewNotifier playlistViewNotifier);
    void loadPlaylist(Playlist playlist);
    void loadAllTracksPlaylist();
    boolean loadTracksFromAlbum(String albumName);
    boolean loadAllTracksFromAlbum(String albumName);
    boolean loadTracksFromGenre(String genreName);
    void loadTracksFromArtist(String artistName);

    void addTrackToCurrentPlaylist(Track track, PlaylistViewNotifier playlistViewNotifier);
    void addTrackToPlaylist(Track track, Playlist playlist, PlaylistViewNotifier playlistViewNotifier);
    void addTracksFromArtistToCurrentPlaylist(String artistName, PlaylistViewNotifier playlistViewNotifier);
    void addRandomTracksFromArtistToCurrentPlaylist(String artistName, PlaylistViewNotifier playlistViewNotifier);
    void addTracksFromAlbumToCurrentPlaylist(String albumName, PlaylistViewNotifier playlistViewNotifier);
    void addRandomTracksFromAlbumToCurrentPlaylist(String artistName, PlaylistViewNotifier playlistViewNotifier);
    void removeTrackFromCurrentPlaylist(Track track, PlaylistViewNotifier playlistViewNotifier);

    int getCurrentIndexOf(Track track);
    int getCurrentIndex();

    void setShuffleState(boolean isEnabled);
    boolean isShuffleEnabled();

    boolean isPlaylistEmpty(long playlistId);

}
