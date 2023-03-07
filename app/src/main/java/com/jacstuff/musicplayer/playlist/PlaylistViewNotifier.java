package com.jacstuff.musicplayer.playlist;

public interface PlaylistViewNotifier {

    void notifyViewOfTrackAddedToPlaylist();
    void notifyViewOfTrackAlreadyInPlaylist();
    void notifyViewOfMultipleTracksAddedToPlaylist(int numberOfTracks);
    void notifyViewOfTrackRemovedFromPlaylist(boolean success);

}
