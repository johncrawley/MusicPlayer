package com.jacstuff.musicplayer;

import com.jacstuff.musicplayer.db.track.Track;

import java.util.List;

public class ListNotifier {

    private List<Track> tracks;
    private ListSubscriber subscriber;


    public void setTracks(List<Track> tracks){
        this.tracks = tracks;
        if(subscriber != null){
            subscriber.notifyListUpdated();
        }
    }


    public void registerSubscriber(ListSubscriber subscriber){
        this.subscriber = subscriber;
    }


    public List<Track> getList(){
        return tracks;
    }
}
