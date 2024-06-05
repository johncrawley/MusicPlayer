package com.jacstuff.musicplayer.service.db.search;

import com.jacstuff.musicplayer.service.db.entities.Track;
import com.jacstuff.musicplayer.service.loader.TrackLoader;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TrackFinder {

    private Map<String, List<Track>> resultsMap;
    private final TrackLoader trackLoader;


    public TrackFinder(TrackLoader trackLoader){
        this.trackLoader = trackLoader;
        initCache();
    }


    public void initCache(){
        resultsMap = new HashMap<>();
    }


    public List<Track> searchFor(String inputStr){
        String searchStr = inputStr.toLowerCase().trim();
        return resultsMap.getOrDefault(searchStr, searchForTerms(searchStr));
    }


    private List<Track> searchForTerms(String searchStr){
        return searchStr.contains(" ") ?
                searchForMultipleTerms(searchStr) : searchForTerm(searchStr);
    }


    private List<Track> searchForMultipleTerms(String searchStr){
        List<String> searchTerms = Arrays.asList(searchStr.split(" "));
        List<Track> initialResults = searchForTerm(searchTerms.get(0));
        List<Track> filteredResults = getFilteredResultsFromRemainingTerms(searchTerms, initialResults);
        resultsMap.put(searchStr, filteredResults);
        return filteredResults;
    }


    private List<Track> getFilteredResultsFromRemainingTerms(List<String> searchTerms, List<Track> initialResults){
        List<String> remainingTerms = searchTerms.subList(1, searchTerms.size());
        return initialResults.stream()
                .filter(t-> containsAll(t, remainingTerms))
                .collect(Collectors.toList());
    }
    

    private boolean containsAll(Track track, List<String> searchTerms){
        for(String searchTerm : searchTerms){
            if(!track.getSearchString().contains(searchTerm)){
                return false;
            }
        }
        return true;
    }


    public List<Track> searchForTerm(String searchTerm){
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
        Comparator<Track> startsWithSearchTermComparator = (t1, t2) -> {
            int result1 = t1.getSearchString().startsWith(searchTerm) ? 1 : -1;
            int result2 = t2.getSearchString().startsWith(searchTerm) ? 1 : -1;
            return result2 - result1;
        };
        List<Track> sortedTracks = tracks.stream().sorted(startsWithSearchTermComparator).collect(Collectors.toList());
        resultsMap.put(searchTerm, sortedTracks);
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
       return results.stream().filter(t -> t.getSearchString().contains(prefix)).collect(Collectors.toList());
    }


    public boolean isTooShortToSearch(String prefix){
        int MINIMUM_SEARCH_LENGTH = 1;
        return prefix.length() < MINIMUM_SEARCH_LENGTH;
    }
}
