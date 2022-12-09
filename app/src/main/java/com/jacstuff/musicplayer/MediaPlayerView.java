package com.jacstuff.musicplayer;

import android.graphics.Bitmap;

import com.jacstuff.musicplayer.db.track.Track;

import java.util.List;

public interface MediaPlayerView {

    void setTrackInfo(String title);
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
    void updateTrackDetails();
    void setTrack(Track track);

   // void notifyOfScrollReady(int index);
}
