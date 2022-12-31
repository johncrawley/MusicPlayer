package com.jacstuff.musicplayer.fragments;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewStateAdapter extends FragmentStateAdapter {

//    private List<Fragment> fragments;

    public ViewStateAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        log("Entered ViewStateAdapter");
        //fragments = new ArrayList<>();
        //fragments.add(new PlaylistsFragment());    // auto-assigned tag: 'f0'
        //fragments.add(new PlayerFragment()); // auto-assigned tag: 'f1'
    }


    private void log(String msg){
        System.out.println("^^^ View State Adapter: " + msg);
    }


    public void onDestroy(){
      //  fragments = null;
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position == 0){
            return new PlayerFragment();
        }
        else if (position == 1){
            return new PlaylistsFragment();
        }
        return new ArtistsFragment();
    }


    @Override
    public int getItemCount() {
        return 3;
    }

}