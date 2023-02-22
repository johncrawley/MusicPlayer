package com.jacstuff.musicplayer.playlist;

import com.jacstuff.musicplayer.db.album.Album;
import com.jacstuff.musicplayer.db.track.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AlbumOrganizer {

    private List<Album> organize(List<Album> albums){
        List<Album> additionalAlbums = new ArrayList<>();
        for(Album album : albums){
            additionalAlbums.addAll(splitAlbums(album));
        }
        return additionalAlbums;
    }

    /*

        scenario 1:  just one album with this name

        scenario 2:  just one album with this name, but 2 artists (one artist has only a few tracks)

        scenario 3: compilation album, multiple artists with a small number of tracks

        scenario 4: 2 or more artists with the same album name
                - need multiple new albums
        TODO: needs to be cleaned up
     */

    private List<Album> splitAlbums(Album album){
        List<Album> additionalAlbums = new ArrayList<>();
        Map<String, List<Track>> tracksMap = album.getTracksMap();
        if(tracksMap.size() < 2){
            return Collections.singletonList(album);
        }
        Album compilationAlbum = createCompilationAlbum(album.getName());

        for(String artist : tracksMap.keySet()){
            List<Track> tracks =  tracksMap.get(artist);
            if(tracks == null){
                continue;
            }

            if(tracks.size() > 5){
                Album additionalAlbum = new Album(System.currentTimeMillis(), album.getName());
                additionalAlbum.addTracks(tracks);
                additionalAlbum.setPrimaryArtist(artist);
                additionalAlbums.add(additionalAlbum);
            }
            else{
                compilationAlbum.addTracks(tracks);
            }
        }
        if(additionalAlbums.size() == 1){
            Album additionalAlbum = additionalAlbums.get(0);
            if(additionalAlbum != null){
                additionalAlbum.addTracks(compilationAlbum.getAllTracks());
            }
        }
        else{
            additionalAlbums.add(compilationAlbum);
        }
        return additionalAlbums;
    }


    private Album createCompilationAlbum(String albumName){
        Album compilationAlbum = new Album(System.currentTimeMillis(), albumName);
        compilationAlbum.setPrimaryArtistAsVarious();
        return compilationAlbum;
    }
}
