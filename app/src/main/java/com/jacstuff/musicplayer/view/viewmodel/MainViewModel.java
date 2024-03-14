package com.jacstuff.musicplayer.view.viewmodel;

import android.graphics.Bitmap;

import com.jacstuff.musicplayer.service.db.track.Track;

import java.util.List;

import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    public boolean hasFourthTabBeenInitialized = false;
    public int currentTabIndex = 0;
    public Bitmap currentAlbumArt;

}
