package com.jacstuff.musicplayer.fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.jacstuff.musicplayer.fragments.albums.AlbumsFragment;
import com.jacstuff.musicplayer.fragments.artist.ArtistsFragment;
import com.jacstuff.musicplayer.fragments.tracks.TracksFragment;
import com.jacstuff.musicplayer.fragments.playlist.PlaylistsFragment;

public class ViewStateAdapter extends FragmentStateAdapter {


    public ViewStateAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position == 0){
            return new TracksFragment(); // auto-assigned tag: 'f0'
        }
        else if (position == 1){
            return new PlaylistsFragment(); // auto-assigned tag: 'f1', and so on.
        }
        else if (position == 2){
            return new ArtistsFragment();
        }
        return new AlbumsFragment();
    }


    @Override
    public int getItemCount() {
        return 4;
    }

}