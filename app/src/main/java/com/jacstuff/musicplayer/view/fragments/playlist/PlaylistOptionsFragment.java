package com.jacstuff.musicplayer.view.fragments.playlist;

import static android.view.View.GONE;
import static com.jacstuff.musicplayer.view.fragments.Message.ADD_RANDOM_TRACKS_TO_PLAYLIST;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_PLAYLISTS_FRAGMENT_TO_DELETE;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_PLAYLISTS_FRAGMENT_TO_LOAD;
import static com.jacstuff.musicplayer.view.fragments.MessageKey.IS_USER_PLAYLIST;
import static com.jacstuff.musicplayer.view.fragments.about.Utils.getBoolean;

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
import com.jacstuff.musicplayer.view.fragments.DialogFragmentUtils;
import com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper;
import com.jacstuff.musicplayer.view.fragments.Message;
import com.jacstuff.musicplayer.view.fragments.trackinfo.TrackInfoFragment;


public class PlaylistOptionsFragment extends DialogFragment {


    private Button loadPlaylistButton, deletePlaylistButton, addRandomTracksButton;
    private boolean isUserPlaylist;


    public static PlaylistOptionsFragment newInstance() {
        return new PlaylistOptionsFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_options_playlist, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        assignArgs();
        setupButtons(view);
        DialogFragmentUtils.setTransparentBackground(this);
    }


    private void assignArgs(){
        Bundle bundle = getArguments();
        assert bundle != null;
        isUserPlaylist = getBoolean(bundle, IS_USER_PLAYLIST);
    }


    private void setupButtons(View parentView){
        loadPlaylistButton = setupButton(parentView, R.id.loadPlaylistButton, ()-> sendMessage(NOTIFY_PLAYLISTS_FRAGMENT_TO_LOAD));
        deletePlaylistButton = setupButton(parentView, R.id.removePlaylistButton, ()-> sendMessage(NOTIFY_PLAYLISTS_FRAGMENT_TO_DELETE));
        addRandomTracksButton = setupButton(parentView, R.id.addRandomTracksButton, this::loadAddRandomTracksFragment);
        hideSomeButtonsWhenUserPlaylistIsLoaded();

    }


    private void loadAddRandomTracksFragment(){
        dismiss();
        FragmentManagerHelper.showDialog(this, new AddRandomTracksFragment(), AddRandomTracksFragment.TAG, new Bundle());
    }


    private void hideSomeButtonsWhenUserPlaylistIsLoaded(){
        if(!isUserPlaylist){
            deletePlaylistButton.setVisibility(GONE);
            addRandomTracksButton.setVisibility(GONE);
        }
    }


    private void sendMessage(Message key){
        disableAllButtons();
        getParentFragmentManager().setFragmentResult(key.toString(), new Bundle());
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
