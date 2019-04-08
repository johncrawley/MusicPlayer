package com.jacstuff.musicplayer;

public class TrackDetailsParser {



    public TrackDetails parse(String pathname){

        String disc,album,artist;

        //storage/3033-3732/Music/Music/BandName/AlbumName/01 TrackName.mp3
        if(pathname == null){
            return null;
        }
        String[] parts = pathname.split("/");
        int lastPartIndex = parts.length -1;

        String trackDir = parseTrackDir(pathname);
        String trackName = parseTrackName(parts[lastPartIndex]);
        String trackNumber = parseTrackNumber(parts[lastPartIndex]);
        String firstFolder = parts[lastPartIndex -1];
        String secondFolder = parts[lastPartIndex - 2];

        if(firstFolder.toLowerCase().contains("disc")){
            disc = firstFolder;
            album = secondFolder;
            artist = parts[lastPartIndex - 3];
        }
        else{
            disc = "";
            album = firstFolder;
            artist = secondFolder;
        }

        return new TrackDetails.Builder()
                .createTrackWithPathname(pathname)
                .withName(trackName)
                .withTrackNumber(trackNumber)
                .withAlbum(album)
                .withDisc(disc)
                .withDirectory(trackDir)
                .withArtist(artist).build();
    }

    private String parseTrackName(String str){
        String trackName;
        int start = str.indexOf(" ");
        int end = str.lastIndexOf(".");
        if(areIndexesValid(start, end)){

            trackName = str.substring(start + 1, end);
        }
        else{
            trackName = str;
        }
        return trackName;
    }

    private String parseTrackNumber(String str){

        String trackNumber = "";
        int end = str.indexOf(" ");
        if(end > -1){
            trackNumber = str.substring(0, end);
        }
        return trackNumber;
    }

    private String parseTrackDir(String pathname){
        int lastSlash = pathname.lastIndexOf("/");
        if(lastSlash == -1){
            return "";
        }
        return pathname.substring(0, lastSlash);

    }


    private boolean areIndexesValid(int firstIndex, int lastIndex){
        return firstIndex != -1 && lastIndex != -1 || firstIndex < lastIndex;
    }

}
