package com.jacstuff.musicplayer.playlist;

import com.jacstuff.musicplayer.TrackDetails;

import java.util.List;

public interface PlaylistManager {


    void savePlaylist();
    String getNext();
    String getNextRandom();
    TrackDetails getNextRandomTrack();
    TrackDetails getNextRandomUnplayedTrack();
    TrackDetails getTrackDetails(int index);
    void refreshPlaylist();
    List<TrackDetails> getTrackDetailsList();
    void init();
    int getCurrentTrackIndex();
    int getNumberOfTracks();

    String getTrackNameAt(int position);
}
