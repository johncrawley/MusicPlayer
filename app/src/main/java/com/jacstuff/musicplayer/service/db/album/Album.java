package com.jacstuff.musicplayer.service.db.album;

import com.jacstuff.musicplayer.service.db.PlaylistStore;
import com.jacstuff.musicplayer.service.db.playlist.Playlist;
import com.jacstuff.musicplayer.service.db.track.Track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Album implements PlaylistStore {
    private final long id;
    private final String name;
    private List<Track> allTracks;
    private final Map<String, Integer> artistsCount;


    public Album(long id, String name){
        this.id = id;
        this.name = name;
        allTracks = new ArrayList<>(50);
        artistsCount = new HashMap<>();
    }


    public void addTrack(Track track) {
        allTracks.add(track);
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


    public void setTracks(List<Track> tracks){
        this.allTracks = tracks;
    }


    @Override
    public Playlist getPlaylist(){
        Playlist playlist = new Playlist(name, Playlist.PlaylistType.ALBUM);
        playlist.setTracks(allTracks);
        return playlist;
    }


    public String getName(){
        return name;
    }


    public long getId(){
        return id;
    }
}
