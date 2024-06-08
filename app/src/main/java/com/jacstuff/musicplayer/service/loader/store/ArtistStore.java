package com.jacstuff.musicplayer.service.loader.store;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.jacstuff.musicplayer.service.db.entities.Artist;
import com.jacstuff.musicplayer.service.db.entities.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ArtistStore {

    private Map<String, Artist> artists;
    private long artistCount;


    public void init(){
        artists = new ConcurrentHashMap<>(500);
    }


    public void add(Track track){
        String artistName = track.getArtist();
        if(!artists.containsKey(artistName)){
            artists.put(artistName, new Artist(artistCount++, artistName));
        }
        Artist artist = artists.get(artistName);
        if(artist != null){
            artist.addTrack(track);
            artist.addAlbumName(track.getAlbum());
        }
    }


    public Map<String, Artist> getAll(){
        return artists;
    }


    public Artist get(String artistName){
        return artists.get(artistName);
    }


    public List<Track> getTracksOf(String artistName){
        if(artists == null){
            artists = new HashMap<>(500);
        }
        Artist artist =  artists.getOrDefault(artistName, new Artist(-1, "null album!"));
        return artist == null ? Collections.emptyList() :artist.getTracks();
    }


    public ArrayList<String> getMainArtistNames(Context context){
        if(artists == null){
            return new ArrayList<>();
        }
        ArrayList<String> names = new ArrayList<>();
        int minNumberOfTracks = getMinimumNumberOfTracksForMainArtist(context);
        for(String key : artists.keySet()){
            Artist artist = artists.get(key);
            if(artist != null && artist.getTracks().size() > minNumberOfTracks){
                names.add(key);
            }
        }
        Collections.sort(names);
        return new ArrayList<>(names);
    }


    private int getMinimumNumberOfTracksForMainArtist(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(prefs.getString("minimumNumberOfTracksForMainArtist", "1"));
    }


}
