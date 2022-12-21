package com.jacstuff.musicplayer.db.track;

public class Track {

    private final String pathname, name, artist, album, disc, directory, genre;
    private String orderedStr;
    private final long id, trackNumber;
    private int index;
    private long duration;


    private Track(Builder builder){
        this.id = builder.id;
        this.pathname = builder.pathname;
        this.duration = builder.duration;
        this.name = builder. name;
        this.artist = builder.artist;
        this.album = builder.album;
        this.trackNumber = builder.trackNumber;
        this.disc = builder.disc;
        this.directory = builder.directory;
        this.genre = builder.genre;
        createOrderedStr();
    }


    private void createOrderedStr(){
        String trackNumberPrefix = trackNumber < 10 ? "0" : "";
        String trackStr = trackNumberPrefix + trackNumber;
        this.orderedStr = artist + "-" + album + "-" + trackStr;
    }


    public String getOrderedString(){
        return orderedStr;
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


    public String getGenre() {
        return genre;
    }


    public String getName(){return name;}


    public long getTrackNumber(){return trackNumber;}


    public String getPathname(){return pathname;}


    public String getDisc(){return disc;}


    public String getDirectory(){return directory;}


    public long getId(){return id;}


    public void setIndex(int index){
        this.index = index;
    }

    public int getIndex(){
        return index;
    }

    @Override
    public String toString(){
       return "::: Track name: " +  getName()
               + " artist: " + getArtist()
               + " album: " + getAlbum()
               + " trackNumber: " + getTrackNumber()
               + " pathname: " + getPathname();
    }

    public static class Builder{


        private String pathname, name, artist, album, disc, directory, genre;
        private long id = -1;
        private long trackNumber;
        private long duration;

        public Track build(){
            return new Track(this);
        }


        public Builder createTrackWithPathname(String pathname){
            this.pathname = pathname;
            return this;
        }


        public Builder withName(String name){
            this.name = name;
            return this;
        }


        public Builder withGenre(String genre){
            this.genre = genre;
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


        public Builder withDirectory(String dir){
            this.directory = dir;
            return this;
        }


        public Builder withTrackNumber(long trackNumber){
            this.trackNumber = trackNumber;
            return this;
        }

    }

}
