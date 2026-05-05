package com.jacstuff.musicplayer.service.db.entities;

import java.util.ArrayList;
import java.util.List;

public class Playlist {

    private final PlaylistType playlistType;
    private final Long id;
    private String name;
    private List<Track> tracks;
    private final boolean isUserPlaylist;
    private int currentIndex;

    public Playlist(Long id, String name, PlaylistType playlistType, boolean isUserPlaylist){
        this.id = id;
        this.name = name;
        this.playlistType = playlistType;
        this.isUserPlaylist = isUserPlaylist;
        this.tracks = new ArrayList<>();
    }


    public void clear(){
        if(isUserPlaylist){
            tracks.clear();
        }
    }




    public Playlist(Long id, String name, boolean isUserPlaylist){
        this(id, name, PlaylistType.PLAYLIST, isUserPlaylist);
    }

    public void resetIndex(){
        currentIndex = -1;
    }


    public void incrementCurrentIndex(){
        currentIndex = currentIndex >= tracks.size() -1 ? 0 : currentIndex + 1;
    }


    public void decrementCurrentIndex(){
        currentIndex = currentIndex <= 0 ? tracks.size() -1 : currentIndex - 1;
    }


    public void setCurrentIndex(int index){
        currentIndex = index;
    }


    public int getCurrentIndex(){
        return currentIndex;
    }

    public void add(Track track){
        tracks.add(track);
    }

    public void remove(Track track){
        tracks.remove(track.getIndex());
    }


    public Track get(int index){
        var i = Math.max(0, Math.min(index, tracks.size()-1));
        return tracks.get(i);
    }

    public int size(){
        return tracks.size();
    }


    public Playlist(String name, PlaylistType type){
        this(type.getDefaultId(), name, type, type.isUserPlaylist());
    }


    public boolean isEmpty(){
        return tracks.isEmpty();
    }


    public void setName(String name){
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


    public PlaylistType getType(){
        return playlistType;
    }

    public boolean isUserPlaylist(){
        return isUserPlaylist;
    }


    public void setTracks(List<Track> tracks){
        this.tracks = tracks;
    }

}
