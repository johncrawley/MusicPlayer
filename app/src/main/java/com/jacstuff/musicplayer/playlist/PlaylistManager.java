package com.jacstuff.musicplayer.playlist;

import com.jacstuff.musicplayer.db.album.Album;
import com.jacstuff.musicplayer.db.artist.Artist;
import com.jacstuff.musicplayer.db.playlist.Playlist;
import com.jacstuff.musicplayer.db.track.Track;
import com.jacstuff.musicplayer.service.MediaPlayerService;

import java.util.List;

public interface PlaylistManager {

    void savePlaylist();
    Track getNextTrack();
    boolean hasTracksQueued();
    boolean hasBeenInitialized();
    boolean isUserPlaylistLoaded();
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
    void addTrackToCurrentPlaylist(Track track);
    void addTracksToCurrentPlaylist(List<Track> tracks);
    void removeTrackToCurrentPlaylist(Track track);
    int getNumberOfTracks();
    void enableShuffle();
    void disableShuffle();
    void onDestroy();
    void loadTracksFromArtist(Artist artist);
    void loadTracksFromAlbum(Album album);

    String getTrackNameAt(int position);
}
