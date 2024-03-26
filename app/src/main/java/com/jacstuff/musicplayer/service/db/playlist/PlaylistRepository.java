package com.jacstuff.musicplayer.service.db.playlist;


import com.jacstuff.musicplayer.service.db.entities.Playlist;

import java.util.List;

public interface PlaylistRepository {

    void createPlaylist(String name);
    void deletePlaylist(Long playlistId);
    List<Playlist> getAllPlaylists();
    List<Playlist> getAllUserPlaylists();

}
