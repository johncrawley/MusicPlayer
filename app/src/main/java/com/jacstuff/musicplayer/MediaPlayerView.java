package com.jacstuff.musicplayer;

import android.graphics.Bitmap;

import com.jacstuff.musicplayer.db.track.Track;

import java.util.List;

public interface MediaPlayerView {

    void notifyCurrentlySelectedTrack(int item);

   // void notifyOfScrollReady(int index);
}
