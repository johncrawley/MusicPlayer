package com.jacstuff.musicplayer.service.db.playlist;

import com.jacstuff.musicplayer.service.db.track.Track;

import java.util.ArrayList;
import java.util.List;

public class Playlist {

    public enum PlaylistType {
        ALL_TRACKS(-100L, false),
        PLAYLIST(-1L, true),
        ALBUM(-10L, false),
        ARTIST(-20L, true),
        GENRE(-30L, true);

        final long defaultId;
        final boolean isUserPlaylist;

        PlaylistType(long defaultId, boolean isUserPlaylist){
            this.defaultId = defaultId;
            this.isUserPlaylist = isUserPlaylist;
        }


        public long getDefaultId(){
            return defaultId;
        }


        public boolean isUserPlaylist(){
            return isUserPlaylist;
        }

    }


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
        this.tracks = new ArrayList<>();
    }


    public Playlist(Long id, String name, boolean isUserPlaylist){
        this(id, name, PlaylistType.PLAYLIST, isUserPlaylist);
    }


    public Playlist(String name, PlaylistType type){
        this(type.getDefaultId(), name, type, type.isUserPlaylist());
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
