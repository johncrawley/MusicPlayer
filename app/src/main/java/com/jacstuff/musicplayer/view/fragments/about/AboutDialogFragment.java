package com.jacstuff.musicplayer.view.fragments.about;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.view.utils.ButtonMaker;

public class AboutDialogFragment extends DialogFragment {

    public static AboutDialogFragment newInstance() {
        return new AboutDialogFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupButtons(view);
    }


    private void setupButtons(View parentView){
        ButtonMaker.createButton(parentView, R.id.dismissAboutDialogButton, this::dismiss);
    }


}