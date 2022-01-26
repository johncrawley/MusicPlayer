package com.jacstuff.musicplayer.playlist;

import com.jacstuff.musicplayer.Track;

import java.util.List;

public interface PlaylistManager {


    void savePlaylist();
    String getNext();
    Track getNextRandomTrack();
    Track getNextRandomUnplayedTrack();
    Track getTrackDetails(int index);
    void refreshPlaylist();
    List<Track> getTracks();
    void init();
    int getCurrentTrackIndex();
    int getNumberOfTracks();

    String getTrackNameAt(int position);
}
