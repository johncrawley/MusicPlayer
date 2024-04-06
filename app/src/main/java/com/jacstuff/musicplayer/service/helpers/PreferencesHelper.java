package com.jacstuff.musicplayer.service.helpers;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.jacstuff.musicplayer.service.MediaPlayerService;

public class PreferencesHelper {

    private final Context context;
    private final String SHUFFLE_ENABLED_PREF = "isShuffleEnabled";

    public PreferencesHelper(Context context){
        this.context = context;
    }


    public void assignPreferences(MediaPlayerService mediaPlayerService){
        assignShuffleState(mediaPlayerService);
    }


    public void saveShuffleState(boolean isShuffleEnabled){
        getPrefs().edit()
                .putBoolean(SHUFFLE_ENABLED_PREF, isShuffleEnabled)
                .apply();
    }


    private void assignShuffleState(MediaPlayerService mediaPlayerService){
        boolean isShuffleEnabled = getPrefs().getBoolean(SHUFFLE_ENABLED_PREF, false);
        if(isShuffleEnabled){
            mediaPlayerService.enableShuffle();
        }
        else{
            mediaPlayerService.disableShuffle();
        }
    }


    public boolean isNextTrackLoadedAutomatically() {
        return getPrefs().getBoolean("autoNextTrackOnPlaylistLoaded", true);
    }


    public boolean isTrackNumberDisplayed() {
        return getPrefs().getBoolean("isTrackNumberDisplayed", true);
    }


    public boolean isArtistDisplayed() {
        return getPrefs().getBoolean("isArtistDisplayed", false);
    }


    public boolean areDuplicateTracksIgnored(){
        return getPrefs().getBoolean("ignoreDuplicateTracks", true);
    }


    private SharedPreferences getPrefs(){
        return getDefaultSharedPreferences(context);
    }


}
