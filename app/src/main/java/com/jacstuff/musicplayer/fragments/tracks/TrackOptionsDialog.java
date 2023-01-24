package com.jacstuff.musicplayer.fragments.tracks;


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


public class TrackOptionsDialog extends DialogFragment {


    public static TrackOptionsDialog newInstance() {
        return new TrackOptionsDialog();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_dialog_track_options, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupButtons(view);
    }


    private void setupButtons(View parentView){
        setupButton(parentView, R.id.enqueueTrackButton, this::enqueueCurrentTrack);
        Button removeTrackButton = setupButton(parentView, R.id.removeFromPlaylistButton, this::removeCurrentTrack);
        setupVisibilityFor(removeTrackButton);
    }


    private void setupVisibilityFor(View view){
        MainActivity mainActivity = getMainActivity();
        int visibility = mainActivity != null && mainActivity.isUserPlaylistLoaded() ?
                View.VISIBLE : View.GONE;
        view.setVisibility(visibility);
    }


    private Button setupButton(View parentView, int id, Runnable runnable){
        Button button = parentView.findViewById(id);
        button.setOnClickListener((View v)-> runnable.run());
        return button;
    }


    private void enqueueCurrentTrack(){
        MainActivity mainActivity = getMainActivity();
        if(mainActivity != null){
            mainActivity.enqueueTrack();
        }
        dismiss();
    }


    private void removeCurrentTrack(){
        MainActivity mainActivity = getMainActivity();
        if(mainActivity != null){
            mainActivity.removeTrack();
        }
        dismiss();
    }


    private MainActivity getMainActivity(){
        return (MainActivity) getActivity();
    }


}
