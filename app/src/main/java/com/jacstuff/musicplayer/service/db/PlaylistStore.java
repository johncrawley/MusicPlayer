package com.jacstuff.musicplayer.service.db;

import com.jacstuff.musicplayer.service.db.playlist.Playlist;
import com.jacstuff.musicplayer.service.db.track.Track;

import java.util.List;

public interface PlaylistStore {
    List<Track> getTracks();
    void setTracks(List<Track> tracks);
    Playlist getPlaylist();
}
