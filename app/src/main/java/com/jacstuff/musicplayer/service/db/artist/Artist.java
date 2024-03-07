package com.jacstuff.musicplayer.service.db.artist;

import com.jacstuff.musicplayer.service.db.PlaylistStore;
import com.jacstuff.musicplayer.service.db.playlist.Playlist;
import com.jacstuff.musicplayer.service.db.track.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Artist implements PlaylistStore {

    private final long id;
    private final String name;
    private List<Track> tracks;
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


    @Override
    public Playlist getPlaylist(){
        Playlist playlist = new Playlist(name, Playlist.PlaylistType.ARTIST);
        playlist.setTracks(tracks);
        return playlist;
    }


    public List<Track> getTracks(){
        return tracks;
    }


    public void setTracks(List<Track> tracks){
        this.tracks = tracks;
    }


    public String getName(){
        return name;
    }

    public long getId(){
        return id;
    }
}
