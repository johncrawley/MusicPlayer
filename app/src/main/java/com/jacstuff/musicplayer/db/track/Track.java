package com.jacstuff.musicplayer.db.track;

public class Track {

    private final String pathname, name, artist, album, disc, directory, genre;
    private String orderedStr;
    private final long id, trackNumber;
    private int index;

    private Track(long id, String pathname, String name, String artist, String album, long trackNumber, String disc, String directory, String genre){

        this.id = id;
        this.pathname = pathname;
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.trackNumber = trackNumber;
        this.disc = disc;
        this.directory = directory;
        this.genre = genre;
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


    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
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

        public Track build(){
            return new Track(id, pathname, name, artist, album, trackNumber,disc, directory, genre);
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
