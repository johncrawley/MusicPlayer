package com.jacstuff.musicplayer.service.loader.store;

import com.jacstuff.musicplayer.service.db.entities.Track;
import com.jacstuff.musicplayer.service.helpers.preferences.PrefKey;
import com.jacstuff.musicplayer.service.helpers.preferences.PreferencesHelperImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TrackStore {

    private List<Track> tracks;
    private final Set<String> existingAllTracksIdentifiers = new HashSet<>();
    private PreferencesHelperImpl preferencesHelper;

    public void init(){
        tracks = new ArrayList<>(10_000);
    }


    public void setPreferencesHelper(PreferencesHelperImpl preferencesHelper){
        this.preferencesHelper = preferencesHelper;
    }


    public List<Track> getTracks(){
        return tracks;
    }


    public void clear(){
        existingAllTracksIdentifiers.clear();
    }


    public List<Track> getAllTracksContaining(String searchTerm){
        return tracks.parallelStream()
                .filter(track -> track.getSearchString().contains(searchTerm))
                .collect(Collectors.toList());
    }


    public boolean add(Track track){
        if(shouldTrackBeAdded(track)){
            tracks.add(track);
            return true;
        }
        return false;
    }


    boolean shouldTrackBeAdded(Track track){
        String identifier = track.getDuplicateIdentifier();
        var areDuplicatesIgnored = preferencesHelper.getBoolean(PrefKey.ARE_DUPLICATE_TRACKS_IGNORED);
        if(areDuplicatesIgnored && existingAllTracksIdentifiers.contains(identifier)){
            return false;
        }
        existingAllTracksIdentifiers.add(identifier);
        return true;
    }

}
