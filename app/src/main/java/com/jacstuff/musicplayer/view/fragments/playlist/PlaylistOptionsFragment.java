package com.jacstuff.musicplayer.view.fragments.playlist;

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

import com.jacstuff.musicplayer.R;


public class PlaylistOptionsFragment extends DialogFragment {


    public final static String NOTIFY_PLAYLISTS_FRAGMENT_TO_DELETE= "Notify_Playlists_Fragment_To_Delete";
    public final static String NOTIFY_PLAYLISTS_FRAGMENT_TO_CREATE= "Notify_Playlists_Fragment_To_Create";
    public final static String NOTIFY_PLAYLISTS_FRAGMENT_TO_LOAD= "Notify_Playlists_Fragment_To_Load";

    private Button loadPlaylistButton, deletePlaylistButton;
    private boolean isUserPlaylist;


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
        assignArgs();
        setupButtons(view);
    }


    private void assignArgs(){
        Bundle bundle = getArguments();
        assert bundle != null;
        isUserPlaylist = bundle.getBoolean(PlaylistsFragment.BUNDLE_KEY_IS_USER_PLAYLIST, false);
    }


    private void setupButtons(View parentView){
        loadPlaylistButton = setupButton(parentView, R.id.loadPlaylistButton, ()-> sendMessage(NOTIFY_PLAYLISTS_FRAGMENT_TO_LOAD));
        deletePlaylistButton = setupButton(parentView, R.id.removePlaylistButton, ()-> sendMessage(NOTIFY_PLAYLISTS_FRAGMENT_TO_DELETE));
        if(!isUserPlaylist){
            deletePlaylistButton.setVisibility(View.GONE);
        }
    }


    private void sendMessage(String key){
        disableAllButtons();
        getParentFragmentManager().setFragmentResult(key, new Bundle());
        dismissAfterPause();
    }


    private void disableAllButtons(){
        loadPlaylistButton.setEnabled(false);
        deletePlaylistButton.setEnabled(false);
    }


    private void dismissAfterPause(){
        new Handler(Looper.getMainLooper()).postDelayed(this::dismiss, 150);
    }


    private Button setupButton(View parentView, int id, Runnable runnable){
        Button button = parentView.findViewById(id);
        button.setOnClickListener((View v)-> runnable.run());
        return button;
    }



}
