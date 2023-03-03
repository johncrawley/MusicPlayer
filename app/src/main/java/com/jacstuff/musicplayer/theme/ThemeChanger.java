package com.jacstuff.musicplayer.theme;

import android.app.Activity;
import android.content.Intent;

import com.jacstuff.musicplayer.R;

public class ThemeChanger {

    private static String themeKey = "green";

    public static void changeToTheme(Activity activity, String theme){
        themeKey = theme;
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
    }


    public static void onActivityCreateSetTheme(Activity activity){
        System.out.println("^^^ theme changer sTheme: " + themeKey);
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
