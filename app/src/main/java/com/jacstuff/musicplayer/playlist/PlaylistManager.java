package com.jacstuff.musicplayer.playlist;

import com.jacstuff.musicplayer.db.album.Album;
import com.jacstuff.musicplayer.db.artist.Artist;
import com.jacstuff.musicplayer.db.playlist.Playlist;
import com.jacstuff.musicplayer.db.track.Track;

import java.util.List;

public interface PlaylistManager {


    void savePlaylist();
    Track getNextTrack();
    Track getPreviousTrack();
    Track getNextRandomUnPlayedTrack();
    Track selectTrack(int index);
    void addTracksFromStorage();
    List<Track> getTracks();
    void init();
    int getCurrentTrackIndex();
    void loadPlaylist(Playlist playlist);
    void addTrackToCurrentPlaylist(Track track);
    void addTrackToCurrentPlaylist(List<Track> tracks);
    int getNumberOfTracks();
    void enableShuffle();
    void disableShuffle();
    void onDestroy();
    void loadTracksFromArtist(Artist artist);
    void loadTracksFromAlbum(Album album);

    String getTrackNameAt(int position);
}
