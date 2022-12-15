package com.jacstuff.musicplayer.playlist;

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
    void onDestroy();

    String getTrackNameAt(int position);
}
