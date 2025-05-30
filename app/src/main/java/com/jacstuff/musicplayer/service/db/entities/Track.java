package com.jacstuff.musicplayer.service.db.entities;

import android.net.Uri;

import androidx.annotation.NonNull;

public class Track {

    private final String pathname, title, artist, album, genre;
    private String disc = "";
    private String orderedStr;
    private String searchStr;
    private final long id, trackNumber;
    private int index;
    private final long duration;
    private final String year;
    private final String bitrate;
    private Uri uri;
    private String cdAndTrackNumber;


    private Track(Builder builder){
        this.id = builder.id;
        this.pathname = builder.pathname;
        this.duration = builder.duration;
        this.title = builder.title;
        this.artist = builder.artist;
        this.album = builder.album;
        this.trackNumber = builder.trackNumber;
        this.index = builder.index;
        this.disc = builder.disc;
        this.genre = builder.genre;
        this.year = builder.year;
        this.bitrate = builder.bitrate;
        this.uri = builder.uri;
        createOrderedStr();
        createSearchStr();
        createDiscAndTrackNumSearchStr();
    }


    public Track(String path, String title, String artist, String album, String disc, String genre, String year, int duration, int trackNumber, String bitrate){
        this.id = -1;
        this.pathname = path;
        this.duration = duration;
        this.title = title;
        this.disc = disc;
        this.artist = artist;
        this.album = album;
        this.trackNumber = trackNumber;
        this.genre = genre;
        this.year = year;
        this.bitrate = bitrate;
        createOrderedStr();
        createSearchStr();
        createDiscAndTrackNumSearchStr();
    }


    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj == null || obj.getClass() != getClass()){
            return false;
        }
        var otherTrack = (Track)obj;
        if(this.pathname == null || otherTrack.pathname == null){
            return false;
        }
        return this.pathname.equals(otherTrack.getPathname());
    }


    @Override
    public int hashCode(){
        return pathname == null ? 0 : pathname.hashCode();
    }


    private void createOrderedStr(){
        String trackNumberPrefix = trackNumber < 10 ? "0" : "";
        String trackStr = trackNumberPrefix + trackNumber;
        orderedStr = artist + "-" + album + "-" + disc + "-" + trackStr;
        orderedStr = orderedStr.toLowerCase();
    }


    private void createSearchStr(){
        searchStr = artist + " " + album + " " + title;
        searchStr = searchStr.toLowerCase();
    }


    private void createDiscAndTrackNumSearchStr(){
        String prefix = trackNumber < 10 ? "000" :
                trackNumber < 100 ? "00" :
                        trackNumber < 1000 ? "0" :
                                "";
        cdAndTrackNumber = disc + "_" + prefix + trackNumber;
    }


    public String getOrderedString(){
        return orderedStr;
    }

    public String getDuplicateIdentifier(){ return artist + "-" + title + "-" + (duration / 1000);}

    public String getSearchString(){
        return searchStr;
    }

    public long getDuration(){
        return duration;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public String getBitrate() { return bitrate; }

    public String getGenre() {
        return genre;
    }

    public String getYear() {
        return year;
    }

    public String getTitle(){return title;}

    public long getTrackNumber(){return trackNumber;}

    public String getCdAndTrackNumber(){return cdAndTrackNumber;}

    public String getTrackNumberStr(){return String.valueOf(trackNumber);}

    public String getPathname(){return pathname;}

    public Uri getUri(){return uri;}

    public String getDisc(){return disc;}

    public long getId(){return id;}

    public void setIndex(int index){
        this.index = index;
    }

    public int getIndex(){
        return index;
    }


    @NonNull
    @Override
    public String toString(){
       return "::: Track name: " +  getTitle()
               + " artist: " + getArtist()
               + " album: " + getAlbum()
               + " trackNumber: " + getTrackNumber()
               + " pathname: " + getPathname();
    }


    public static class Builder{
        private String pathname, title, artist, album, disc, genre;
        private long id = -1;
        private long trackNumber;
        private long duration;
        private int index;
        private String year;
        private String bitrate;
        private Uri uri;

        public Track build(){
            return new Track(this);
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder createTrackWithPathname(String pathname){
            this.pathname = pathname;
            return this;
        }


        public Builder withTitle(String title){
            this.title = title;
            return this;
        }


        public Builder withGenre(String genre){
            this.genre = genre;
            return this;
        }


        public Builder withYear(String year){
            this.year = year;
            return this;
        }


        public Builder withUri(Uri uri){
            this.uri = uri;
            return this;
        }


        public Builder withBitrate(String bitrate){
            this.bitrate = bitrate;
            return this;
        }


        public Builder withIndex(long index){
            this.index = (int)index;
            return this;
        }


        public Builder withId(long id){
            this.id = id;
            return this;
        }


        public Builder duration(long duration){
            this.duration = duration;
            return this;
        }


        public Builder withAlbum(String album){
            this.album = album;
            return this;
        }


        public Builder withArtist(String artist){
            this.artist = artist;
            return this;
        }


        public Builder withDisc(String disc){
            this.disc = disc;
            return this;
        }


        public Builder withTrackNumber(long trackNumber){
            this.trackNumber = trackNumber;
            return this;
        }

    }

}
