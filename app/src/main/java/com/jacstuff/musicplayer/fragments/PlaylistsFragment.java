package com.jacstuff.musicplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.viewmodel.MainViewModel;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class PlaylistsFragment extends Fragment {

    private Context context;
    private MainViewModel viewModel;

    public PlaylistsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getContext();
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        // setupKeyAction(view.findViewById(R.id.wholeWordCheckEditText));
        return view;
    }
}