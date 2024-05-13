package com.jacstuff.musicplayer.service.playlist;

import com.jacstuff.musicplayer.service.db.entities.Track;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexManager {

    private Map<String, Integer> trackPathsToIndexesMap;
    private final Map<String, Integer> allTracksPathsToIndexesMap;

    public IndexManager(){
        trackPathsToIndexesMap = new HashMap<>();
        allTracksPathsToIndexesMap = new HashMap<>();
    }


    public void setIndexForAddedTrack(Track track){
        int index = trackPathsToIndexesMap.size();
        track.setIndex(index);
        trackPathsToIndexesMap.put(track.getPathname(), index);
    }


    public int getIndexOf(Track track){
        Integer index = trackPathsToIndexesMap.get(track.getPathname());
        return index == null ? -1 : index;
    }


    public void setAllTracksIndexes(){
        trackPathsToIndexesMap = allTracksPathsToIndexesMap;
    }


    public void assignIndexesToTracks(List<Track> tracks){
        trackPathsToIndexesMap = new HashMap<>();
        for(int i = 0; i< tracks.size(); i++){
            assignIndexToTrack(tracks.get(i), i);
        }
    }


    public void assignIndexesToAllTracks(List<Track> tracks){
        allTracksPathsToIndexesMap.clear();
        for(int i = 0; i< tracks.size(); i++){
            allTracksPathsToIndexesMap.put(tracks.get(i).getPathname(), i);
        }
    }


    private void assignIndexToTrack(Track track, int index){
        track.setIndex(index);
        trackPathsToIndexesMap.put(track.getPathname(), index);
    }
}
