package com.jacstuff.musicplayer.service.db;

import com.jacstuff.musicplayer.service.db.track.Track;

import java.util.List;

public interface TrackStore {
    List<Track> getTracks();
}
