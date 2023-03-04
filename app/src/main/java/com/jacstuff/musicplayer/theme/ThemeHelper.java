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
        switch (themeKey){
            case "green":
                activity.setTheme(R.style.AppTheme);
                break;
            case "yellow":
                activity.setTheme(R.style.YellowTheme);
                break;
            default:
        }
    }


}
