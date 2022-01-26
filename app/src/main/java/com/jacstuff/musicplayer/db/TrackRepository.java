package com.jacstuff.musicplayer.db;

import com.jacstuff.musicplayer.Track;

import java.util.List;

public interface TrackRepository {

    void addTrack(Track track);
    void deleteTrack(Track track);
    List<Track> getAllTracks();

}
