package com.jacstuff.musicplayer.service.db.genre;

import com.jacstuff.musicplayer.service.db.PlaylistStore;
import com.jacstuff.musicplayer.service.db.playlist.Playlist;
import com.jacstuff.musicplayer.service.db.playlist.PlaylistType;
import com.jacstuff.musicplayer.service.db.track.Track;

import java.util.ArrayList;
import java.util.List;

public class Genre implements PlaylistStore {

    private final long id;
    private final String name;
    private List<Track> allTracks;


    public Genre(long id, String name){
        this.id = id;
        this.name = name;
        allTracks = new ArrayList<>(500);
    }


    @Override
    public List<Track> getTracks(){
        return allTracks;
    }


    public void  setTracks(List<Track> tracks){
        allTracks = tracks;
    }


    @Override
    public Playlist getPlaylist(){
        Playlist playlist = new Playlist(name, PlaylistType.GENRE);
        playlist.setTracks(allTracks);
        return playlist;
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
