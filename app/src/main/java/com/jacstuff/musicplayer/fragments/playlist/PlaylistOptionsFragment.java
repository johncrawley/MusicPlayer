package com.jacstuff.musicplayer.fragments.playlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;

public class PlaylistOptionsFragment extends DialogFragment {


    public static PlaylistOptionsFragment newInstance() {
        return new PlaylistOptionsFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist_options, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupButtons(view);
    }


    private void setupButtons(View parentView){
        setupButton(parentView, R.id.loadPlaylistButton, this::enqueueCurrentTrack);
        setupButton(parentView, R.id.createNewPlaylistButton, this::showAddTrackToPlaylistDialog);
        Button removeTrackButton = setupButton(parentView, R.id.removePlaylistButton, this::removeSelectedTrackFromPlaylist);

    }


    private Button setupButton(View parentView, int id, Runnable runnable){
        Button button = parentView.findViewById(id);
        button.setOnClickListener((View v)-> runnable.run());
        return button;
    }


    private void enqueueCurrentTrack(){
        MainActivity mainActivity = getMainActivity();
        if(mainActivity != null){
            mainActivity.addSelectedTrackToQueue();
        }
        dismiss();
    }


    private void showAddTrackToPlaylistDialog(){
        MainActivity mainActivity = getMainActivity();
        if(mainActivity != null){
            mainActivity.showAddTrackToPlaylistView();
        }
        dismiss();
    }


    private void removeSelectedTrackFromPlaylist(){
        MainActivity mainActivity = getMainActivity();
        if(mainActivity != null){
            mainActivity.removeSelectedTrackFromPlaylist();
        }
        dismiss();
    }


    private MainActivity getMainActivity(){
        return (MainActivity) getActivity();
    }


}
