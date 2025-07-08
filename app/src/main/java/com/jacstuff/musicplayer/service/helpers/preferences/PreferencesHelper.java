package com.jacstuff.musicplayer.service.helpers.preferences;

public interface PreferencesHelper {

    void saveShuffleState(boolean isShuffleEnabled);
    boolean getBoolean(PrefKey prefKey);
    int getInt(PrefKey prefKey);
    String getStr(PrefKey prefKey);
    boolean hasPathChanged();
    void set(PrefKey prefKey, boolean value);


}
