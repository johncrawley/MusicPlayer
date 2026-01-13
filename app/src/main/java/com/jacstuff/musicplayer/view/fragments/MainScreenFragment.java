package com.jacstuff.musicplayer.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.jacstuff.musicplayer.R;

public class MainScreenFragment  extends Fragment {

    public MainScreenFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.fragment_main_screen, container, false);
        setupViews(parentView);
        return parentView;
    }


    private void setupViews(View parentView){

    }

    private void setupButton(View parentView, int buttonId, Runnable action){
        Button button = parentView.findViewById(buttonId);
        button.setOnClickListener( v-> action.run());
    }


}
