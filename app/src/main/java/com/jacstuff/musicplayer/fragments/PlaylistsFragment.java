package com.jacstuff.musicplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.viewmodel.MainViewModel;

import androidx.fragment.app.Fragment;
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
        context = getContext();
        View view = inflater.inflate(R.layout.fragment_playlists, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        // setupKeyAction(view.findViewById(R.id.wholeWordCheckEditText));
        setupButtons(view);
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


    private void startAddPlaylistFragment(){
        String tag = "add_playlist";
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        //TODO : implement removePreviousFragmentTransaction(tag, fragmentTransaction);
        AddPlaylistFragment addPlaylistFragment = AddPlaylistFragment.newInstance();
        addPlaylistFragment.show(fragmentTransaction, tag);

    }
}