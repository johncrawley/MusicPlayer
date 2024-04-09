package com.jacstuff.musicplayer.service;

import java.util.Optional;

public class ListIndexManager {

    private int artistIndex;
    private int albumIndex;
    private int genreIndex;
    private int playlistIndex;


    public void setArtistIndex(int index){
        this.artistIndex = index;
        albumIndex = -1;
        playlistIndex = -1;
        genreIndex = -1;
    }


    public void setAlbumIndex(int index){
        this.albumIndex = index;
        genreIndex = -1;
        playlistIndex = -1;
    }


    public void setGenreIndex(int index){
        log("Entered setGenreIndex() index: " + index);
        genreIndex = index;
        albumIndex = -1;
        artistIndex = -1;
        playlistIndex = -1;
    }


    public void setPlaylistIndex(int index){
        playlistIndex = index;
        genreIndex = -1;
        albumIndex = -1;
        artistIndex = -1;
    }


    private void log(String msg){
        System.out.println("^^^ ListIndexManager: " + msg);
    }


    public Optional<Integer> getAlbumIndex(){
        return getNonNegativeOrEmptyOf(albumIndex);
    }


    public Optional<Integer> getArtistIndex(){
        return getNonNegativeOrEmptyOf(artistIndex);
    }


    public Optional<Integer> getGenreIndex(){
        return getNonNegativeOrEmptyOf(genreIndex);
    }


    public Optional<Integer> getPlaylistIndex(){
        return getNonNegativeOrEmptyOf(playlistIndex);
    }


    public Optional<Integer> getNonNegativeOrEmptyOf(int x){
        return x < 0 ? Optional.empty() : Optional.of(x);
    }


    public void resetAllIndexes(){
        playlistIndex = -1;
        artistIndex = -1;
        albumIndex = -1;
        genreIndex = -1;
    }

}
