package com.jacstuff.musicplayer.db.playlist;

import java.util.List;

public interface PlaylistRepository {

    void createPlaylist(String name);
    void deletePlaylist(Integer playlistId);
    void addTrackToPlaylist(Integer playlistId, Integer trackId);
    void removeTrackFromPlaylist(Integer playlistId, Integer trackId);
    void renamePlaylist(Integer playlistId, String updatedName);
    List<Playlist> getAllRepositories();

}
