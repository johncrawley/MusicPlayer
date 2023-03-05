package com.jacstuff.musicplayer.theme;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;

public class ThemeHelper {

    private String currentThemeKey = "green";


    public void restartActivityIfDifferentThemeSet(MainActivity mainActivity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        String themeKey = prefs.getString("theme_color", "green");
        if(!themeKey.equals(currentThemeKey)){
            restartActivity(mainActivity);
        }
    }


    public void assignTheme(MainActivity mainActivity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        String themeKey = prefs.getString("theme_color", "green");
        if(themeKey.equals(currentThemeKey)){
            return;
        }
        setTheme(mainActivity, themeKey);
        currentThemeKey = themeKey;
    }


    public void restartActivity(Activity activity){
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
    }


    public static void setTheme(Activity activity, String themeKey){
        int styleId = R.style.AppTheme;
        switch (themeKey){
            case "green":
                styleId = R.style.AppTheme;
            case "yellow":
                styleId = R.style.YellowTheme;
                break;
            case "red":
                styleId = R.style.RedTheme;

        }
        activity.setTheme(styleId);
    }


}
