package com.jacstuff.musicplayer;

public class TrackDetails {

    private String pathname, name, artist, album, trackNumber, disc, directory;

    private  TrackDetails(String pathname, String name, String artist, String album, String trackNumber, String disc, String directory){

        this.pathname = pathname;
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.trackNumber = trackNumber;
        this.disc = disc;
        this.directory = directory;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }
    public String getName(){return name;}
    public String getTrackNumber(){return trackNumber;}
    public String getPathname(){return pathname;}
    public String getDisc(){return disc;}
    public String getDirectory(){return directory;}

    public static class Builder{


        private String pathname, name, artist, album, trackNumber,disc, directory;


        public TrackDetails build(){
            return new TrackDetails(pathname, name, artist, album, trackNumber,disc, directory);
        }

        public Builder createTrackWithPathname(String pathname){
            this.pathname = pathname;
            return this;
        }
        public Builder withName(String name){
            this.name = name;
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

        public Builder withTrackNumber(String trackNumber){
            this.trackNumber = trackNumber;
            return this;
        }

    }

}
