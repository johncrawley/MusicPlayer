package com.jacstuff.musicplayer.trackplayer.view;

import android.graphics.Bitmap;

import com.jacstuff.musicplayer.service.db.entities.Track;

public interface OpenTrackView {
   void updateViewsOnTrackAssigned();
   void setBlankTrackInfo();
   void setBlankTrackInfoOnMainView();
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
