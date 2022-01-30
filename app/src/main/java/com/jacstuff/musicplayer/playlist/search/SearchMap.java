package com.jacstuff.musicplayer.playlist.search;

import com.jacstuff.musicplayer.Track;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchMap {

    private final Map<String, List<SearchItem>> searchItemMap;


    public SearchMap(){
        searchItemMap = new HashMap<>(30_000);
    }

    public void buildMap(List<Track> tracks){
        for(int i=0; i< tracks.size(); i++){
            Track track = tracks.get(i);
            SearchItem searchItem = new SearchItem(i, track.getArtist(), track.getAlbum(), track.getName());
            //for(int i)

        }


    }



}
