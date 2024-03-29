package com.jacstuff.musicplayer.view.trackplayer;

import android.graphics.Bitmap;

import com.jacstuff.musicplayer.service.db.entities.Track;

public interface TrackPlayerView {
   void updateViewsOnTrackAssigned();
   void setBlankTrackInfo();
   void setBlankTrackInfoOnMainView();
   void updateForConnecting();
   void stopUpdatingElapsedTimeOnView();
   void notifyMediaPlayerPlaying();
   void notifyMediaPlayerStopped();
   void notifyMediaPlayerPaused();
   void displayError(Track track);
   void setElapsedTime(long elapsedTime);
   void notifyThatFileDoesNotExist(Track track);
   void setAlbumArt(Bitmap bitmap);
   void displayInfoFrom(Track track);
}
