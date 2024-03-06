package com.jacstuff.musicplayer.service.db.artist;

import com.jacstuff.musicplayer.service.db.TrackStore;
import com.jacstuff.musicplayer.service.db.track.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Artist implements TrackStore {

    private final long id;
    private final String name;
    private final List<Track> tracks;
    private final Set<String> albumNames;
    private ArrayList<String> sortedAlbumNames;


    public Artist(long id, String name){
        this.id = id;
        this.name = name;
        tracks = new ArrayList<>(50);
        albumNames = new HashSet<>();
    }


    public void addTrack(Track track){
        tracks.add(track);
    }


    public void addAlbumName(String albumName){
        albumNames.add(albumName);
    }


    public ArrayList<String> getAlbumNames(){
        return sortedAlbumNames == null ? createSortedAlbumNamesList() : sortedAlbumNames;
    }


    private ArrayList<String> createSortedAlbumNamesList(){
        sortedAlbumNames =  new ArrayList<>(albumNames);
        Collections.sort(sortedAlbumNames);
        return sortedAlbumNames;
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
