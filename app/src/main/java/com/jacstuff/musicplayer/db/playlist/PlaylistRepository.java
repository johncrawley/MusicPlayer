package com.jacstuff.musicplayer.db.playlist;

import com.jacstuff.musicplayer.db.track.Track;

import java.util.List;

public interface PlaylistRepository {

    void createPlaylist(String name);
    void deletePlaylist(Long playlistId);
    void addTrackToPlaylist(Long playlistId, Long trackId);
    void removeTrackFromPlaylist(Long playlistId, Long trackId);
    void renamePlaylist(Long playlistId, String updatedName);
    List<Playlist> getAllPlaylists();
    List<Track> getAllTracksFromPlaylist(Long playlistId);

}
