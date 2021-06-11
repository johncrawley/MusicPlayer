package com.jacstuff.musicplayer.playlist;

import com.jacstuff.musicplayer.TrackDetails;

import java.util.List;

public interface PlaylistManager {


    void savePlaylist();
    String getNext();
    TrackDetails getNextRandomTrack();
    TrackDetails getNextRandomUnplayedTrack();
    TrackDetails getTrackDetails(int index);
    void refreshPlaylist();
    List<TrackDetails> getTracks();
    void init();
    int getCurrentTrackIndex();
    int getNumberOfTracks();

    String getTrackNameAt(int position);
}
