package com.jacstuff.musicplayer.service.db.playlist;

import com.jacstuff.musicplayer.service.db.track.Track;

import java.util.List;

public class Playlist {

    public enum PlaylistType { PLAYLIST, ALBUM, ARTIST}
    private final PlaylistType playlistType;
    private final Long id;
    private String name;
    private List<Track> tracks;
    private final boolean isUserPlaylist;

    public Playlist(Long id, String name, PlaylistType playlistType, boolean isUserPlaylist){
        this.id = id;
        this.name = name;
        this.playlistType = playlistType;
        this.isUserPlaylist = isUserPlaylist;
    }


    public Playlist(Long id, String name, boolean isUserPlaylist){
        this(id, name, PlaylistType.PLAYLIST, isUserPlaylist);
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


    public PlaylistType getPlaylistType(){
        return playlistType;
    }

    public boolean isUserPlaylist(){
        return isUserPlaylist;
    }


    public void setTracks(List<Track> tracks){
        this.tracks = tracks;
    }

}
