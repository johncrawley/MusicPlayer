package com.jacstuff.musicplayer.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;

public class SearchFragment extends DialogFragment {


    public static SearchFragment newInstance() {
        return new SearchFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) getActivity();
        if(activity == null){
            return;
        }
        setupButtons(view);
        setupDimensions(view, activity);
    }

    void setupDimensions(View rootView, Activity activity){
        DisplayMetrics metrics = getDisplayMetrics(activity);
        int width = (int)(metrics.widthPixels /1.5f);
        rootView.setLayoutParams(new FrameLayout.LayoutParams(width, rootView.getLayoutParams().height));
    }


    public DisplayMetrics getDisplayMetrics(Activity activity){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }



    private void setupButtons(View parentView){
        setupButton(parentView, R.id.addSelectedButton, this::dismiss);
        setupButton(parentView, R.id.addAllButton, this::dismiss);
        setupButton(parentView, R.id.playSelectedButton, this::dismiss);
        setupButton(parentView, R.id.cancelButton, this::dismiss);
    }

    private void setupButton(View parentView, int buttonId, Runnable runnable){
        Button button = parentView.findViewById(buttonId);
        button.setOnClickListener((View v)-> runnable.run());
    }



}