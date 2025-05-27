package com.jacstuff.musicplayer.view.tab;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.jacstuff.musicplayer.view.fragments.album.AlbumsFragment;
import com.jacstuff.musicplayer.view.fragments.artist.ArtistsFragment;
import com.jacstuff.musicplayer.view.fragments.tracks.TracksFragment;
import com.jacstuff.musicplayer.view.fragments.playlist.PlaylistsFragment;

public class TabsViewStateAdapter extends FragmentStateAdapter {

    private final int NUMBER_OF_FRAGMENTS = 4;

    public TabsViewStateAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return switch (position) {
            case 0 -> new TracksFragment();
            case 1 -> new PlaylistsFragment();
            case 2 -> new ArtistsFragment();
            case 3 -> new AlbumsFragment();
            // case 4 -> new GenresFragment();
            default -> throw new RuntimeException("createFragment() : position exceeded number of fragments (" + NUMBER_OF_FRAGMENTS + ") position: " + position);
        };
    }


    @Override
    public int getItemCount() {
        return NUMBER_OF_FRAGMENTS;
    }


}