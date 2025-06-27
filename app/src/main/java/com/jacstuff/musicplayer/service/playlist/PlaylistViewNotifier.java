package com.jacstuff.musicplayer.service.playlist;

public interface PlaylistViewNotifier {

    void notifyViewOfTrackAddedToPlaylist();
    void notifyViewOfTrackAlreadyInPlaylist();
    void notifyViewOfMultipleTracksAddedToPlaylist(int numberOfTracks);
    void notifyViewOfTrackRemovedFromPlaylist(boolean success);
    void notifyViewOfTracksRemovedFromPlaylist();

}
