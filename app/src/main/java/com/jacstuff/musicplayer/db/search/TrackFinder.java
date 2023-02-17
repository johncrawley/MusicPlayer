package com.jacstuff.musicplayer.db.search;

import com.jacstuff.musicplayer.db.track.Track;
import com.jacstuff.musicplayer.playlist.TrackLoader;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TrackFinder {

    private final Map<String, List<Track>> resultsMap;
    private final TrackLoader trackLoader;


    public TrackFinder(TrackLoader trackLoader){
        this.trackLoader = trackLoader;
        resultsMap = new HashMap<>();
    }


    public List<Track> searchFor(String searchTerm){
        if(isTooShortToSearch(searchTerm)) {
            return Collections.emptyList();
        }
        String searchTermLC = searchTerm.toLowerCase();
        if(resultsMap.containsKey(searchTermLC)){
            return resultsMap.get(searchTermLC);
        }
        return createTracksListFor(searchTermLC);
    }


    public List<Track> createTracksListFor(String searchTerm){
        List<Track> tracks = getCachedOrDbResultsFor(searchTerm);
        resultsMap.put(searchTerm, tracks);
        return tracks;
    }


    private List<Track> getCachedOrDbResultsFor(String searchTerm){
        String prefix = getParentPrefix(searchTerm);
        return resultsMap.containsKey(prefix) ?
                getFilteredTracksFor(searchTerm, prefix)
                : trackLoader.getAllTracksContaining(searchTerm);
    }


    private String getParentPrefix(String str){
        return str.substring(0, str.length()-1);
    }


    private List<Track> getFilteredTracksFor(String prefix, String prePrefix){
        List<Track> results = resultsMap.get(prePrefix);
        if(results == null){
            return Collections.emptyList();
        }
       return results.stream().filter(t -> t.getSearchString().toLowerCase().contains(prefix)).collect(Collectors.toList());
    }


    public boolean isTooShortToSearch(String prefix){
        int MINIMUM_SEARCH_LENGTH = 3;
        return prefix.length() < MINIMUM_SEARCH_LENGTH;
    }
}
