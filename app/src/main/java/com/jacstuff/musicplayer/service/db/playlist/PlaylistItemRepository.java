package com.jacstuff.musicplayer.service.db.playlist;

import com.jacstuff.musicplayer.service.db.track.Track;

import java.util.List;

public interface PlaylistItemRepository {
    boolean addPlaylistItem(Track track, long playlistId);
    boolean isTrackAlreadyInPlaylist(Track track, long playlistId);
    void deletePlaylistItem(long  trackId);
    List<Track> getTracksForPlaylistId(long playlistId);
    void deleteAllPlaylistItems(long playlistId);
}
