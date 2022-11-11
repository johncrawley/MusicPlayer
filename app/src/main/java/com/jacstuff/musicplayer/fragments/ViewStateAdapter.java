package com.jacstuff.musicplayer.fragments;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewStateAdapter extends FragmentStateAdapter {

    private final List<Fragment> fragments;
    private final PlaylistsFragment playlistsFragment;

    public ViewStateAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        fragments = new ArrayList<>();
        playlistsFragment = new PlaylistsFragment();
        fragments.add(new PlayerFragment());    // auto-assigned tag: 'f0'
        fragments.add(playlistsFragment); // auto-assigned tag: 'f1'
    }


    public PlaylistsFragment getPlaylistsFragment(){
        return playlistsFragment;
    }



    @NonNull
    @Override
    public Fragment createFragment(int position) {
        int index = Math.min(fragments.size()-1, position);
        return fragments.get(index);
    }


    @Override
    public int getItemCount() {
        return fragments.size();
    }
}