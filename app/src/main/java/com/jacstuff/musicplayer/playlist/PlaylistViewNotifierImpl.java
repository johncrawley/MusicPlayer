package com.jacstuff.musicplayer.playlist;

import com.jacstuff.musicplayer.MainActivity;

public class PlaylistViewNotifierImpl implements PlaylistViewNotifier{

    private final MainActivity mainActivity;

    public PlaylistViewNotifierImpl(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }


    @Override
    public void notifyViewOfTrackAddedToPlaylist() {
        mainActivity.notifyTrackAddedToPlaylist();
    }


    @Override
    public void notifyViewOfTrackAlreadyInPlaylist() {
        mainActivity.notifyTrackAlreadyInPlaylist();
    }


    @Override
    public void notifyViewOfMultipleTracksAddedToPlaylist(int numberOfTracks) {
        mainActivity.notifyTracksAddedToPlaylist(numberOfTracks);
    }


    @Override
    public void notifyViewOfTrackRemovedFromPlaylist(boolean success) {
        mainActivity.notifyTrackRemovedFromPlaylist(success);
    }


}
