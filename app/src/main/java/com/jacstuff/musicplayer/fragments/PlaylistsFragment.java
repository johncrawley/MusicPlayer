package com.jacstuff.musicplayer.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.viewmodel.MainViewModel;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

public class PlaylistsFragment extends Fragment {

    private Context context;
    private MainViewModel viewModel;
    private Button addPlaylistButton;

    public PlaylistsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println("Entered onCreateView");
        context = getContext();
        View view = inflater.inflate(R.layout.fragment_playlists, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        // setupKeyAction(view.findViewById(R.id.wholeWordCheckEditText));
        setupButtons(view);
        hasClicked = false;
        return view;
    }

    private void setupButtons(View parentView){
        setupAddPlaylistButton(parentView);
    }


    private void setupAddPlaylistButton(View parentView){
        addPlaylistButton = parentView.findViewById(R.id.addPlaylistButton);
        addPlaylistButton.setOnClickListener((View v)->{
            startAddPlaylistFragment();
        });
    }

    private boolean hasClicked;

    @Override
    public void onResume(){
        super.onResume();
        System.out.println("Entered onResume()");
        hasClicked = false;
    }

    private void startAddPlaylistFragment(){
        if(hasClicked){
            return;
        }
        hasClicked = true;
        String tag = "add_playlist";
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(fragmentManager == null){
            return;
        }
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        removePreviousFragmentTransaction(fragmentManager,tag, fragmentTransaction);
        AddPlaylistFragment addPlaylistFragment = AddPlaylistFragment.newInstance();
        addPlaylistFragment.show(fragmentTransaction, tag);
    }

    private FragmentManager getSupportFragmentManager(){
        FragmentActivity activity = getActivity();
        if(activity == null){
            return null;
        }
        return activity.getSupportFragmentManager();
    }


    private void removePreviousFragmentTransaction(FragmentManager fragmentManager, String tag, FragmentTransaction fragmentTransaction){
        Fragment prev = fragmentManager.findFragmentByTag(tag);
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
    }

}