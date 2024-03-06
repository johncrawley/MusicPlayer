package com.jacstuff.musicplayer.service.db.album;

import com.jacstuff.musicplayer.service.db.TrackStore;
import com.jacstuff.musicplayer.service.db.track.Track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Album implements TrackStore {
    private final long id;
    private final String name;
    private final List<Track> allTracks;
    private final Map<String, Integer> artistsCount;
    private final Map<String, List<Track>> tracksMap;
    private String primaryArtist;
    private boolean hasVariousArtists;

    public Album(long id, String name){
        this.id = id;
        this.name = name;
        allTracks = new ArrayList<>(50);
        artistsCount = new HashMap<>();
        tracksMap = new HashMap<>();
    }


    public void addTrack(Track track) {
        allTracks.add(track);
    }


    public void addTracks(List<Track> tracks) {
        allTracks.addAll(tracks);
    }



    public void addTrack(Track track, String artistName) {
        if(tracksMap.containsKey(artistName)){
            addTrackToArtist(artistName, track);
            return;
        }
        artistsCount.put(artistName, 1);
    }


    public void setPrimaryArtist(String artistName){
        this.primaryArtist = artistName;
    }


    public void setPrimaryArtistAsVarious(){
        hasVariousArtists = true;
    }




    public Map<String, List<Track>>  getTracksMap(){
        return tracksMap;
    }


    public String getPrimaryArtist(){
        return primaryArtist;
    }


    public String getDisplayName(){
        if(hasVariousArtists){
            return "Various - " + getName();
        }
        return primaryArtist + " - " + name;
    }


    private void addTrackToArtist(String artistName, Track track){
        List<Track> tracksForArtist = tracksMap.computeIfAbsent(artistName, k -> new ArrayList<>(20));
        tracksForArtist.add(track);
    }


    public void addArtist(String artistName){
        if(artistsCount.containsKey(artistName)){
            addCountToArtist(artistName);
            return;
        }
        artistsCount.put(artistName, 1);
    }


    private void addCountToArtist(String artistName){
        Integer count = artistsCount.get(artistName);
        if(count == null){
            count = 0;
        }
        artistsCount.put(artistName, count + 1);
    }


    public List<Track> getTracks(){
        return allTracks;
    }


    public String getName(){
        return name;
    }


    public long getId(){
        return id;
    }
}
