package com.jacstuff.musicplayer.service.db.genre;

import com.jacstuff.musicplayer.service.db.TrackStore;
import com.jacstuff.musicplayer.service.db.track.Track;

import java.util.ArrayList;
import java.util.List;

public class Genre implements TrackStore {

    private final long id;
    private final String name;
    private final List<Track> allTracks;


    public Genre(long id, String name){
        this.id = id;
        this.name = name;
        allTracks = new ArrayList<>(500);
    }

    @Override
    public List<Track> getTracks(){
        return allTracks;
    }


    public String getName(){
        return name;
    }


    public long getId(){
        return id;
    }


    public void addTrack(Track track){
        allTracks.add(track);
    }
}
