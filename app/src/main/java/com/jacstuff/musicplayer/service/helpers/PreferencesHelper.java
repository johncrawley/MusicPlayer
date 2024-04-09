package com.jacstuff.musicplayer.service.helpers;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.jacstuff.musicplayer.service.MediaPlayerService;

public class PreferencesHelper {

    private final Context context;
    private final String SHUFFLE_ENABLED_PREF = "isShuffleEnabled";
    private String path;

    public PreferencesHelper(Context context){
        this.context = context;
        path = getPathsStr();
    }


    public void assignPreferences(MediaPlayerService mediaPlayerService){
        assignShuffleState(mediaPlayerService);
    }


    public void saveShuffleState(boolean isShuffleEnabled){
        getPrefs().edit()
                .putBoolean(SHUFFLE_ENABLED_PREF, isShuffleEnabled)
                .apply();
    }


    public boolean areOnlyAlbumTracksFromSelectedArtistShown(){
       return getPrefs().getBoolean("onlyShowAlbumTracksFromSelectedArtist", true);
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


    public boolean hasPathChanged(){
        boolean hasChanged = !path.equals(getPathsStr());
        path = getPathsStr();
        return hasChanged;
    }


    private String getPathsStr(){
        return getPrefs().getString("tracksPathnameString", "/Music");
    }


    private SharedPreferences getPrefs(){
        return getDefaultSharedPreferences(context);
    }


}
