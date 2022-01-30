package com.jacstuff.musicplayer.playlist.search;

public class SearchItem {

    private final int listPosition;
    private final String trackSummary;

    public SearchItem(int position, String artist, String album, String trackName){
        this.listPosition = position;
        this.trackSummary = artist + " - " + album + " : " + trackName;
    }

    public int getListPosition(){
        return listPosition;
    }

    public String getTrackSummary(){
        return trackSummary;
    }

}
