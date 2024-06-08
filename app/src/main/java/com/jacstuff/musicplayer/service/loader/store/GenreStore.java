package com.jacstuff.musicplayer.service.loader.store;

import com.jacstuff.musicplayer.service.db.entities.Genre;
import com.jacstuff.musicplayer.service.db.entities.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GenreStore {

    private Map<String, Genre> genres;
    private int genreCount;

    public void init(){
        genres = new ConcurrentHashMap<>(100);
    }


    public Genre get(String genreName){
        return genres.get(genreName);
    }



    public ArrayList<String> getAllGenreNames(){
        if(genres == null){
            return new ArrayList<>();
        }
        ArrayList<String> names = new ArrayList<>(genres.keySet());
        Collections.sort(names);
        return names;
    }


    public void add(Track track){
        String genreName = track.getGenre();
        if(genreName.trim().isEmpty()){
            return;
        }
        genres.computeIfAbsent(genreName, k -> new Genre(genreCount++, k)).addTrack(track);
    }


}
