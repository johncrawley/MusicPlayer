package com.jacstuff.musicplayer.db.playlist;

import com.jacstuff.musicplayer.db.track.Track;

import java.util.List;

public class Playlist {

    private final Long id;
    private final String name;
    private List<Track> tracks;

    public Playlist(Long id, String name){
        this.id = id;
        this.name = name;
    }


    public Long getId(){
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
