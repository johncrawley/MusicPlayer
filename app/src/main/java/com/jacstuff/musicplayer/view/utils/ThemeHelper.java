package com.jacstuff.musicplayer.view.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;

import java.util.HashMap;
import java.util.Map;

public class ThemeHelper {

    private String currentThemeKey = "green";
    private final Map<String, Integer> themeMap;


    public ThemeHelper(){
        themeMap = new HashMap<>();
        themeMap.put("green", R.style.AppTheme);
        themeMap.put("yellow", R.style.YellowTheme);
        themeMap.put("red", R.style.RedTheme);
        themeMap.put("blue", R.style.BlueTheme);
        themeMap.put("grey", R.style.GreyTheme);
        themeMap.put("magenta", R.style.MagentaTheme);
        themeMap.put("cyan", R.style.CyanTheme);
    }


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


    public void setTheme(Activity activity, String themeKey){
        Integer styleId = themeMap.getOrDefault(themeKey, R.style.AppTheme);
        if(styleId != null){
            activity.setTheme(styleId);
        }
    }


}
