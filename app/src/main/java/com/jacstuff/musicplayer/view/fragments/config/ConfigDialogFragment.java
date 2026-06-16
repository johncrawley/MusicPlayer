package com.jacstuff.musicplayer.view.fragments.config;

import static com.jacstuff.musicplayer.view.fragments.dialog.DialogFragmentUtils.runThenDismissAfterDelay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.view.fragments.dialog.DialogFragmentUtils;
import com.jacstuff.musicplayer.view.fragments.FragmentHelper;
import com.jacstuff.musicplayer.view.utils.ButtonMaker;

import java.util.Optional;

public class ConfigDialogFragment extends DialogFragment {

    public static ConfigDialogFragment newInstance() {
        return new ConfigDialogFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_config, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupButtons(view);
        DialogFragmentUtils.setTransparentBackground(this);
    }


    private void setupButtons(View parentView){
        ButtonMaker.setupButton(parentView, R.id.openSettingsButton, this::openSettings);
        ButtonMaker.setupButton(parentView, R.id.refreshTracksButton, this::refreshTracks);
        ButtonMaker.setupButton(parentView, R.id.loadGenreButton, this::openLoadGenreDialog);
        ButtonMaker.setupButton(parentView, R.id.aboutAppButton, this::openAboutApp);
    }


    private void openSettings(){
        runThenDismissAfterDelay(this, (MainActivity::startSettingsActivity));
    }


    private void openAboutApp(){
        dismiss();
        getMain().ifPresent(FragmentHelper::showAboutDialog);
    }


    private void openLoadGenreDialog(){
        dismiss();
        getMain().ifPresent(FragmentHelper::showGenreDialog);
    }


    private void refreshTracks(){
        runThenDismissAfterDelay(this, (ma) -> {
            var mediaPlayerService = ma.getMediaPlayerService();
            if(mediaPlayerService != null){
                mediaPlayerService.refreshTrackDataFromFilesystem();
            }
        });
    }


    private Optional<MainActivity> getMain(){
        return Optional.ofNullable((MainActivity) getActivity());
    }



}