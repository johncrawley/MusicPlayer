package com.jacstuff.musicplayer.fragments;

import java.util.ArrayList;
import java.util.List;

public class PlaylistLoadedNotifier {

    private final List<PlaylistLoadedObserver> observers;

    public PlaylistLoadedNotifier(){
        observers = new ArrayList<>();
    }


    public void addObserver(PlaylistLoadedObserver playlistLoadedObserver){
        observers.add(playlistLoadedObserver);
    }


    public void notifyObservers(){
        observers.forEach(PlaylistLoadedObserver::notifyOnPlaylistLoaded);
    }
}
