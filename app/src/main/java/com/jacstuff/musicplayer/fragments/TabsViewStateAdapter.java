package com.jacstuff.musicplayer.fragments;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class TabsViewStateAdapter  {

    private static int NUM_ITEMS = 2;

    public TabsViewStateAdapter(FragmentManager fm) {

    }

    public int getCount() {
        return NUM_ITEMS;
    }


}