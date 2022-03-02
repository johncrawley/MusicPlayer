package com.jacstuff.musicplayer.db.playlist;

import com.jacstuff.musicplayer.Track;

import java.util.List;

public class Playlist {

    private final Integer id;
    private final String name;
    private List<Track> tracks;

    public Playlist(Integer id, String name, List<Track> tracks){
        this.id = id;
        this.name = name;
        this.tracks = tracks;
    }


    public Integer getId(){
        return id;
    }


    public String getName(){
        return name;
    }


    public List<Track> getTracks(){
        return tracks;
    }


    public void setTracks(List<Track> tracks){
        this.tracks = tracks;
    }

}
