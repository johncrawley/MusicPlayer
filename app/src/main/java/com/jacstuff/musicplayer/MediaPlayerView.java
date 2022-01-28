package com.jacstuff.musicplayer;

import android.graphics.Bitmap;

import java.util.List;

public interface MediaPlayerView {

    void setTrackInfo(String title);
    void showPauseIcon();
    void showPlayIcon();
    void setAlbumInfo(String albumInfo);
    void setArtistInfo(String artistInfo);
    void setElapsedTime(String currentTime);
    void setTotalTrackTime(String totalTrackTime);
    void displayPlaylistRefreshedMessage();
    void displayPlaylistRefreshedMessage(int newTrackCount);
    void enableControls();
    void setCoverImage(Bitmap bitmap);
    void refreshTrackList(List<Track> trackDetailsList);
    void scrollToListPosition(int index);
    void notifyCurrentlySelectedTrack(int item);

   // void notifyOfScrollReady(int index);
}
