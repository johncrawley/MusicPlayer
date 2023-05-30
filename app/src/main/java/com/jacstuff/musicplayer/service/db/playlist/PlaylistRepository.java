package com.jacstuff.musicplayer.service.db.playlist;


import java.util.List;

public interface PlaylistRepository {

    void createPlaylist(String name);
    void deletePlaylist(Long playlistId);
    List<Playlist> getAllPlaylists();
    List<Playlist> getAllUserPlaylists();

}
