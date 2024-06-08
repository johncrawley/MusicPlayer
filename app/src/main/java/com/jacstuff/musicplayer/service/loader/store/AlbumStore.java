package com.jacstuff.musicplayer.service.loader.store;

import com.jacstuff.musicplayer.service.db.entities.Album;
import com.jacstuff.musicplayer.service.db.entities.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AlbumStore {


    private Map<String, Album> albums = new ConcurrentHashMap<>(5000);
    private ArrayList<String> allAlbumNames = new ArrayList<>();
    private int albumCount;


    public void init(){
        albums = new ConcurrentHashMap<>(5000);
    }


    public Album get(String albumName){
       return albums.get(albumName);
    }


    public ArrayList<String> getAllAlbumNames(){
        return albums == null ? new ArrayList<>() : allAlbumNames;
    }


    public List<Track> getTracksOf(String albumName){
        Album album =  albums.getOrDefault(albumName, new Album(-1, "null album!"));
        return album == null ? Collections.emptyList() : new ArrayList<>(album.getTracks());
    }


    public void initAllAlbumNames(){
        allAlbumNames = new ArrayList<>(albums.keySet());
        Collections.sort(allAlbumNames);
    }


    public void add(Track track){
        String albumName = track.getAlbum();
        if(!albums.containsKey(albumName)){
            albums.put(albumName, new Album(albumCount++, albumName));
        }
        Album album = albums.get(albumName);
        if(album != null){
            album.addTrack(track);
            album.addArtist(track.getArtist());
        }
    }

}
