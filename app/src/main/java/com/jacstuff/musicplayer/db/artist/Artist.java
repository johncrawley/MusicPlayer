package com.jacstuff.musicplayer.db.artist;

import com.jacstuff.musicplayer.db.track.Track;

import java.util.ArrayList;
import java.util.List;

public class Artist {

    private long id;
    private String name;
    private final List<Track> tracks;


    public Artist(long id, String name){
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
