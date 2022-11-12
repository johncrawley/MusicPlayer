package com.jacstuff.musicplayer.db.search;

import com.jacstuff.musicplayer.db.playlist.PlaylistRepository;
import com.jacstuff.musicplayer.db.track.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TrackFinder {

    private PlaylistRepository playlistRepository;
    private final int MINIMUM_SEARCH_LENGTH = 4;
    private String lastPrefix;
    private Map<String, List<Track>> resultsMap;

    public TrackFinder(PlaylistRepository playlistRepository){
        this.playlistRepository = playlistRepository;
        resultsMap = new HashMap<>();
    }


    public List<Track> getTracksWithPrefix(String prefix){
        if(isTooShortToSearch(prefix)) {
            return Collections.emptyList();
        }
        if(resultsMap.containsKey(prefix)){
            return resultsMap.get(prefix);
        }
        if(prefix.length() < lastPrefix.length()){
            deleteListsWithKeysLongerThan(prefix.length());
        }
        lastPrefix = prefix;
        return new ArrayList<>();
    }


    private void deleteListsWithKeysLongerThan(int prefixLength){
        List<String> keysToBeRemoved = resultsMap.keySet().stream().filter(str -> str.length() > prefixLength).collect(Collectors.toList());
        for(String key : keysToBeRemoved){
            resultsMap.remove(key); //TODO: verify that this works!
        }
    }

    public boolean isTooShortToSearch(String prefix){
        return prefix.length() < MINIMUM_SEARCH_LENGTH;
    }
}
