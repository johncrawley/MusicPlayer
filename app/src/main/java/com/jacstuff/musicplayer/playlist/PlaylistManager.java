package com.jacstuff.musicplayer.playlist;

import com.jacstuff.musicplayer.db.album.Album;
import com.jacstuff.musicplayer.db.artist.Artist;
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
    int getNumberOfTracks();
    void enableShuffle();
    void disableShuffle();
    void onDestroy();
    void loadTracksFromArtist(Artist artist);
    void loadTracksFromAlbum(Album album);

    String getTrackNameAt(int position);
}
