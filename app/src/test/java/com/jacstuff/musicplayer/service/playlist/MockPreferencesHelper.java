package com.jacstuff.musicplayer.service.playlist;

import com.jacstuff.musicplayer.service.helpers.preferences.PrefKey;
import com.jacstuff.musicplayer.service.helpers.preferences.PreferencesHelper;

public class MockPreferencesHelper implements PreferencesHelper {

    int numberOfRandomTracksToAdd;

    public MockPreferencesHelper(int numberOfRandomTracksToAdd){
        this.numberOfRandomTracksToAdd = numberOfRandomTracksToAdd;
    }

    @Override
    public void saveShuffleState(boolean isShuffleEnabled) {

    }

    @Override
    public boolean getBoolean(PrefKey prefKey) {
        return false;
    }

    @Override
    public int getInt(PrefKey prefKey) {
        if(prefKey == PrefKey.NUMBER_OF_RANDOM_TRACKS_TO_ADD){
            return numberOfRandomTracksToAdd;
        }
        return 0;
    }

    @Override
    public String getStr(PrefKey prefKey) {
        return "";
    }

    @Override
    public boolean hasPathChanged() {
        return false;
    }


    public void setNumberOfRandomTracksToAdd(int value){
        this.numberOfRandomTracksToAdd = value;
    }
}
