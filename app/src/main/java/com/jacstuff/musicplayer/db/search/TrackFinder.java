package com.jacstuff.musicplayer.db.search;

import com.jacstuff.musicplayer.db.playlist.PlaylistRepository;
import com.jacstuff.musicplayer.db.track.Track;
import com.jacstuff.musicplayer.db.track.TrackRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TrackFinder {

    private final TrackRepository trackRepository;
    private final Map<String, List<Track>> resultsMap;


    public TrackFinder(TrackRepository trackRepository){
        this.trackRepository = trackRepository;
        resultsMap = new HashMap<>();
    }


    public List<Track> getTracksWithPrefix(String prefix){
        if(isTooShortToSearch(prefix)) {
            return Collections.emptyList();
        }
        if(resultsMap.containsKey(prefix)){
            return resultsMap.get(prefix);
        }
        return createListForPrefix(prefix);
    }


    public List<Track> createListForPrefix(String prefix){
        List<Track> tracks = getCachedOrDbResultsFor(prefix);
                resultsMap.put(prefix, tracks);
        return tracks;
    }


    private String getParentPrefix(String str){
       return str.substring(0, str.length()-1);
    }


    private List<Track> getCachedOrDbResultsFor(String prefix){
        String prePrefix = getParentPrefix(prefix);
        return resultsMap.containsKey(prePrefix) ?
                getFilteredTracksForPrefix(prefix, prePrefix)
                : trackRepository.getAllTracksStartingWith(prefix);
    }


    private List<Track> getFilteredTracksForPrefix(String prefix, String prePrefix){
        List<Track> results = resultsMap.get(prePrefix);
        return results.stream().filter(t -> t.getOrderedString().contains(prefix)).collect(Collectors.toList());
    }


    private void deleteListsWithKeysLongerThan(int prefixLength){
        List<String> keysToBeRemoved = resultsMap.keySet().stream().filter(str -> str.length() > prefixLength).collect(Collectors.toList());
        for(String key : keysToBeRemoved){
            resultsMap.remove(key); //TODO: verify that this works!
        }
    }


    public boolean isTooShortToSearch(String prefix){
        int MINIMUM_SEARCH_LENGTH = 3;
        return prefix.length() < MINIMUM_SEARCH_LENGTH;
    }
}
