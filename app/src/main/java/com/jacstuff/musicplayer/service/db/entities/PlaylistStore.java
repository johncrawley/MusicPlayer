package com.jacstuff.musicplayer.service.db.entities;

import com.jacstuff.musicplayer.service.db.entities.Playlist;
import com.jacstuff.musicplayer.service.db.entities.Track;

import java.util.List;

public interface PlaylistStore {
    List<Track> getTracks();
    void setTracks(List<Track> tracks);
    Playlist getPlaylist();
}
