package com.jacstuff.musicplayer;

public class Track {

    private final String pathname, name, artist, album, disc, directory, genre;
    private final long id, trackNumber;

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
