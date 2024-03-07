package com.jacstuff.musicplayer.view.tab;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.jacstuff.musicplayer.view.fragments.album.AlbumsFragment;
import com.jacstuff.musicplayer.view.fragments.artist.ArtistsFragment;
import com.jacstuff.musicplayer.view.fragments.genre.GenresFragment;
import com.jacstuff.musicplayer.view.fragments.tracks.TracksFragment;
import com.jacstuff.musicplayer.view.fragments.playlist.PlaylistsFragment;

public class TabsViewStateAdapter extends FragmentStateAdapter {


    public TabsViewStateAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }


    @NonNull
    //@Override
    public Fragment createFragment2(int position) {
        if(position == 0){
            return new TracksFragment(); // auto-assigned tag: 'f0'
        }
        else if (position == 1){
            return new PlaylistsFragment(); // auto-assigned tag: 'f1', and so on.
        }
        else if (position == 2){
            return new ArtistsFragment();
        }
        else if (position == 3){
            return new AlbumsFragment();
        }
        return new GenresFragment();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return switch (position) {
            case 0 -> new TracksFragment();
            case 1 -> new PlaylistsFragment();
            case 2 -> new ArtistsFragment();
            case 3 -> new AlbumsFragment();
            default -> new GenresFragment();
        };
    }

    @Override
    public int getItemCount() {
        return 5;
    }

}