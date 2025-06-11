package com.jacstuff.musicplayer.service.playlist;

import com.jacstuff.musicplayer.service.db.entities.PlaylistType;

import java.util.List;

public record RandomTrackConfig (String playlistName, PlaylistType sourcePlaylistType, List<String> selectedItems, int numberOfTracksToAdd){
}
