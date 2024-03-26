package com.jacstuff.musicplayer.service.playlist;

import com.jacstuff.musicplayer.service.db.entities.Track;

import java.util.ArrayList;
import java.util.List;

public class TrackHistory {

    private int currentHistoryIndex;
    private List<Track> trackHistory;

    public TrackHistory(){
        reset();
    }


    public void reset(){
        trackHistory = new ArrayList<>();
        resetCurrentIndex();
    }


    public Track getPreviousTrack(){
        currentHistoryIndex = Math.max(0, currentHistoryIndex-1);
        return trackHistory.isEmpty() ? null : trackHistory.get(currentHistoryIndex);
    }


    public Track getNextTrack(){
        currentHistoryIndex++;
        return trackHistory.get(currentHistoryIndex);
    }


    private void resetCurrentIndex(){
        currentHistoryIndex = -1;
    }


    public void add(Track track){
        trackHistory.add(track);
        currentHistoryIndex++;
    }


    public void removeHistoriesAfterCurrent(){
        if(isHistoryIndexOld()) {
            trackHistory = trackHistory.subList(0, currentHistoryIndex);
        }
    }


    public boolean isHistoryIndexOld(){
        return currentHistoryIndex < trackHistory.size() -1;
    }
}
