package com.jacstuff.musicplayer;

import com.jacstuff.musicplayer.db.track.Track;

import java.util.List;

public interface MediaController {

    void play();

    Track getCurrentTrack();

    void stop();

    void next();

    void pause();

    void togglePlay();

    void finish();

    void scanForTracks();

    String getTrackNameAt(int position);

    void selectTrack(int index);


    int getNumberOfTracks();
    void initPlaylistAndRefreshView();

    List<Track> getTrackDetailsList();
}