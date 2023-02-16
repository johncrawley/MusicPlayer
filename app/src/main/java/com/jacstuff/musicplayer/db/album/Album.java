package com.jacstuff.musicplayer.db.album;

import com.jacstuff.musicplayer.db.track.Track;

import java.util.ArrayList;
import java.util.List;

public class Album {
    private final long id;
    private final String name;
    private final List<Track> tracks;

    public Album(long id, String name){
        this.id = id;
        this.name = name;
        tracks = new ArrayList<>(50);
    }


    public void addTrack(Track track){
        tracks.add(track);
    }


    public List<Track> getTracks(){
        return tracks;
    }


    public String getName(){
        return name;
    }


    public long getId(){
        return id;
    }
}
