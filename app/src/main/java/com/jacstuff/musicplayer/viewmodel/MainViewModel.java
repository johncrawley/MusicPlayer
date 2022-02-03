package com.jacstuff.musicplayer.viewmodel;

import com.jacstuff.musicplayer.Track;

import java.util.List;

import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {


    public List<Track> tracks;
    public List<Integer> unplayedPathnameIndexes;
}
