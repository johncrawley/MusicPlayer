package com.jacstuff.musicplayer.service.helpers.preferences;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesHelperImpl implements PreferencesHelper{

    private final Context context;
    private String path = "";
    private String excludeStr = "";

    public PreferencesHelperImpl(Context context){
        this.context = context;
        path = getStr(PrefKey.TRACKS_PATH_STR);
    }


    public void saveShuffleState(boolean isShuffleEnabled){
        getPrefs().edit()
                .putBoolean(PrefKey.IS_SHUFFLE_ENABLED.getKeyStr(), isShuffleEnabled)
                .apply();
    }


    public boolean getBoolean(PrefKey prefKey){
        return getPrefs().getBoolean(prefKey.getKeyStr(), prefKey.getDefault());
    }


    public int getInt(PrefKey prefKey){
        return getPrefs().getInt(prefKey.getKeyStr(), 5);
    }


    public String getStr(PrefKey prefKey){
        return getPrefs().getString(prefKey.getKeyStr(), "");
    }


    public boolean hasPathChanged(){
        var savedPath = getStr(PrefKey.TRACKS_PATH_STR);
        var savedExcludeStr = getStr(PrefKey.EXCLUDED_TRACKS_PATH_STR);
        boolean hasChanged = !path.equals(savedPath) || !excludeStr.equals(savedExcludeStr);
        path = savedPath;
        excludeStr = savedExcludeStr;
        return hasChanged;
    }


    private SharedPreferences getPrefs(){
        return getDefaultSharedPreferences(context);
    }
}
