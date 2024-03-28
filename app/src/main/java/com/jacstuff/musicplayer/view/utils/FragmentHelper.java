package com.jacstuff.musicplayer.view.utils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


public class FragmentHelper {

    public static FragmentTransaction createTransaction(AppCompatActivity mainActivity, String tag){
        FragmentTransaction fragmentTransaction = mainActivity.getSupportFragmentManager().beginTransaction();
        removePreviousFragmentTransaction(mainActivity, tag, fragmentTransaction);
        return fragmentTransaction;
    }

    private static void removePreviousFragmentTransaction(AppCompatActivity mainActivity, String tag, FragmentTransaction fragmentTransaction){
        Fragment prev = mainActivity.getSupportFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
    }

}
