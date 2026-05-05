package com.jacstuff.musicplayer.view.fragments.album;

import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.sendMessage;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TO_LOAD_ALBUM;
import static com.jacstuff.musicplayer.view.fragments.Utils.disableButton;
import static com.jacstuff.musicplayer.view.fragments.Utils.setupButtonAndMakeVisible;

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
import com.jacstuff.musicplayer.service.MediaPlayerService;
import com.jacstuff.musicplayer.view.fragments.DialogFragmentUtils;
import com.jacstuff.musicplayer.view.utils.ButtonMaker;

public class AlbumOptionsFragment extends DialogFragment {

    private String albumName;
    public final static String ALBUM_NAME_BUNDLE_KEY = "album_name_key";
    private Button addTracksToPlaylistButton, addRandomTracksToPlaylistButton, loadTracksButton;

    public static AlbumOptionsFragment newInstance() {
        return new AlbumOptionsFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_options_album, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        assignAlbumNameFromBundle();
        setupButtons(view);
        DialogFragmentUtils.setTransparentBackground(this);
    }


    private void assignAlbumNameFromBundle(){
        var bundle = getArguments();
        assert bundle != null;
        albumName = bundle.getString(ALBUM_NAME_BUNDLE_KEY);
    }


    private void setupButtons(View parentView){
        loadTracksButton = ButtonMaker.setupButton(parentView, R.id.loadAlbumTracksButton, this::loadAlbumTracks);
        addTracksToPlaylistButton =  setupButtonAndMakeVisible(parentView, R.id.addAlbumTracksToCurrentPlaylistButton, this::addAlbumTracksToCurrentPlaylist);
        addRandomTracksToPlaylistButton = setupButtonAndMakeVisible(parentView, R.id.addRandomAlbumTracksToCurrentPlaylistButton, this::addRandomAlbumTracksToCurrentPlaylist);
    }


    private void loadAlbumTracks(){
        disableAllButtons();
        notifyAlbumFragmentToLoadAlbum();
        dismissAfterPause();
    }


    private void notifyAlbumFragmentToLoadAlbum(){
        sendMessage(this, NOTIFY_TO_LOAD_ALBUM);
    }


    private void addAlbumTracksToCurrentPlaylist(){
        disableAllButtons();
        var service = getService();
        if(service != null){
            service.getPlaylistHelper().addTracksFromAlbumToCurrentPlaylist(albumName);
        }
        dismissAfterPause();
    }


    private void addRandomAlbumTracksToCurrentPlaylist(){
        var service = getService();
        if(service != null){
            service.getPlaylistHelper().addRandomTracksFromAlbumToCurrentPlaylist(albumName);
        }
    }


    private void disableAllButtons(){
        disableButton(loadTracksButton);
        disableButton(addTracksToPlaylistButton);
        disableButton(addRandomTracksToPlaylistButton);
    }


    private MediaPlayerService getService(){
        var activity = getMainActivity();
        if(activity == null) {
            return null;
        }
        return activity.getMediaPlayerService();
    }


    private MainActivity getMainActivity(){
        return (MainActivity) getActivity();
    }


    private void dismissAfterPause(){
        new Handler(Looper.getMainLooper()).postDelayed(this::dismiss, 150);
    }


}