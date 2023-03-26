package com.jacstuff.musicplayer.fragments.artist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.view.utils.ButtonMaker;

public class ArtistOptionsFragment extends DialogFragment {


    public static ArtistOptionsFragment newInstance() {
        return new ArtistOptionsFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artist_options, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupButtons(view);
    }


    private void setupButtons(View parentView){
        ButtonMaker.createButton(parentView, R.id.loadArtistTracksButton, this::loadArtistTracks);
        ButtonMaker.createButton(parentView, R.id.addArtistTracksToCurrentPlaylistButton, this::addArtistTracksToCurrentPlaylist);

    }

    private void loadArtistTracks(){

    }


    private void addArtistTracksToCurrentPlaylist(){

    }


    private MainActivity getMainActivity(){
        return (MainActivity) getActivity();
    }


}