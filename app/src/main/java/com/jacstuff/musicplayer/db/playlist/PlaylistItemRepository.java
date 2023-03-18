package com.jacstuff.musicplayer.db.playlist;

import static android.provider.BaseColumns._ID;
import static com.jacstuff.musicplayer.db.DbContract.TracksEntry.TABLE_NAME;

import android.database.SQLException;

import com.jacstuff.musicplayer.db.DbContract;
import com.jacstuff.musicplayer.db.track.Track;

import java.util.List;

public interface PlaylistItemRepository {
    boolean addPlaylistItem(Track track, long playlistId);
    boolean isTrackAlreadyInPlaylist(Track track, long playlistId);
    void deletePlaylistItem(long  trackId);
    List<Track> getTracksForPlaylistId(long playlistId);
    void deleteAllPlaylistItems(long playlistId);
}
