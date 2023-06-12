package com.jacstuff.musicplayer.view.fragments.tracks;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.view.utils.ButtonMaker;


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
        ButtonMaker.createButton(parentView, R.id.enqueueTrackButton, this::enqueueCurrentTrack);
        ButtonMaker.createButton(parentView, R.id.loadAlbumButton, this::loadRelatedAlbum);
        setupAddTrackToPlaylistButton(parentView);
        setupRemoveTrackButton(parentView);
    }


    private void setupAddTrackToPlaylistButton(View parentView){
        Button addTrackToPlaylistButton =  ButtonMaker.createButton(parentView, R.id.addTrackToPlaylistButton, this::showAddTrackToPlaylistDialog);
        if(addTrackToPlaylistButton != null) {
            setupVisibilityForUserPlaylistsExist(addTrackToPlaylistButton);
        }
    }


    private void setupRemoveTrackButton(View parentView){
        Button removeTrackButton =  ButtonMaker.createButton(parentView, R.id.removeFromPlaylistButton, this::removeSelectedTrackFromPlaylist);
        if(removeTrackButton != null) {
            setupVisibilityForUserPlaylistLoaded(removeTrackButton);
        }
    }


    private void setupVisibilityForUserPlaylistsExist(View view){
        MainActivity mainActivity = getMainActivity();
        int visibility = mainActivity != null && !mainActivity.getAllUserPlaylists().isEmpty() ?
                View.VISIBLE : View.GONE;
        view.setVisibility(visibility);
    }


    private void setupVisibilityForUserPlaylistLoaded(View view){
        MainActivity mainActivity = getMainActivity();
        int visibility = mainActivity != null && mainActivity.isUserPlaylistLoaded() ?
                View.VISIBLE : View.GONE;
        view.setVisibility(visibility);
    }


    private void enqueueCurrentTrack(){
        MainActivity mainActivity = getMainActivity();
        if(mainActivity != null){
            mainActivity.addSelectedTrackToQueue();
        }
        dismissAfterDelay();
    }


    private void loadRelatedAlbum(){
        MainActivity mainActivity = getMainActivity();
        if(mainActivity != null) {
            mainActivity.loadAlbumOfSelectedTrack();
        }
        dismissAfterDelay();
    }


    private void showAddTrackToPlaylistDialog(){
        MainActivity mainActivity = getMainActivity();
        if(mainActivity != null){
            mainActivity.showAddTrackToPlaylistView();
        }
        dismissAfterDelay();
    }


    private void removeSelectedTrackFromPlaylist(){
        MainActivity mainActivity = getMainActivity();
        if(mainActivity != null){
            mainActivity.removeSelectedTrackFromPlaylist();
        }
        dismissAfterDelay();
    }


    private MainActivity getMainActivity(){
        return (MainActivity) getActivity();
    }


    private void dismissAfterDelay(){
        new Handler(Looper.getMainLooper()).postDelayed(this::dismiss, 200);
    }
}
