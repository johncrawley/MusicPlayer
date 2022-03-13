package com.jacstuff.musicplayer.db.track;

import java.util.List;

public interface TrackRepository {

    void addTrack(Track track);
    void deleteTrack(Track track);
    List<Track> getAllTracks();

}
