package com.jacstuff.musicplayer.service.playlist;

import com.jacstuff.musicplayer.service.db.entities.Track;
import com.jacstuff.musicplayer.service.helpers.preferences.PrefKey;
import com.jacstuff.musicplayer.service.helpers.preferences.PreferencesHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomTrackAppender {

    private final PreferencesHelper preferencesHelper;
    private final Random random = new Random(System.currentTimeMillis());

    public RandomTrackAppender(PreferencesHelper preferencesHelper){
        this.preferencesHelper = preferencesHelper;
    }


    public int addTracksFrom(List<Track> sourceTracks, List<Track> destinationTracks){
        var source = new ArrayList<>(sourceTracks);
        var copiedTracksCount = 0;

        for(int i = 0; i < getNumberOfTracksToCopy(); i++){
            if(source.isEmpty()){
                break;
            }
            int randomIndex = random.nextInt(source.size());
            var randomTrack = source.remove(randomIndex);
            if(!destinationTracks.contains(randomTrack)){
                destinationTracks.add(randomTrack);
                copiedTracksCount++;
            }
        }
        return copiedTracksCount;
    }


    private int getNumberOfTracksToCopy(){
       return Math.max(1, preferencesHelper.getInt(PrefKey.NUMBER_OF_RANDOM_TRACKS_TO_ADD));
    }

}
