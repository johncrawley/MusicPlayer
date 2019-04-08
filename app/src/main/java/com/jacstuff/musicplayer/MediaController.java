package com.jacstuff.musicplayer;

import java.util.List;

public interface MediaController {

    void play();

    void stop();

    void next();

    void pause();

    void togglePlay();

    void finish();

    void refreshPlaylist();

    String getTrackNameAt(int position);

    void selectTrack(int index);


    int getNumberOfTracks();
    void initPlaylistAndRefreshView();

    List<TrackDetails> getTrackDetailsList();
}