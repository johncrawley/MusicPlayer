package com.jacstuff.musicplayer.db.album;

public class Album {
    private long id;
    private String name;

    public Album(long id, String name){
        this.id = id;
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public long getId(){
        return id;
    }
}
