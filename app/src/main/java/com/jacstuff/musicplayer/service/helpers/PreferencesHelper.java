package com.jacstuff.musicplayer.service.helpers;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.jacstuff.musicplayer.service.MediaPlayerService;

public class PreferencesHelper {


    private final MediaPlayerService mediaPlayerService;
    private final Context context;

    private final String SHUFFLE_ENABLED_PREF = "isShuffleEnabled";

    public PreferencesHelper(MediaPlayerService mediaPlayerService, Context context){
        this.context = context;
        this.mediaPlayerService = mediaPlayerService;
    }


    public void assignPreferences(){
        assignShuffleState();
    }


    public void saveShuffleState(boolean isShuffleEnabled){
        getPrefs().edit()
                .putBoolean(SHUFFLE_ENABLED_PREF, isShuffleEnabled)
                .apply();
    }


    private void assignShuffleState(){
        boolean isShuffleEnabled = getPrefs().getBoolean(SHUFFLE_ENABLED_PREF, false);
        if(isShuffleEnabled){
            mediaPlayerService.enableShuffle();
        }
        else{
            mediaPlayerService.disableShuffle();
        }
    }


    public void loadNextTrackAutomatically(){
        if(getPrefs().getBoolean("autoNextTrackOnPlaylistLoaded", true)){
            mediaPlayerService.loadNextTrack();
        }
    }


    private SharedPreferences getPrefs(){
        return getDefaultSharedPreferences(context);
    }


}
