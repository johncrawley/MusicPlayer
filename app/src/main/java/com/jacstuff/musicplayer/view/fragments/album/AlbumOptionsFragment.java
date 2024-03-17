package com.jacstuff.musicplayer.view.fragments.album;

import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.sendMessage;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TO_LOAD_ALBUM;

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

public class AlbumOptionsFragment extends DialogFragment {

    private String albumName;
    public final static String ALBUM_NAME_BUNDLE_KEY = "album_name_key";
    Button addTracksToPlaylistButton, loadTracksButton;

    public static AlbumOptionsFragment newInstance() {
        return new AlbumOptionsFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_album_options, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        assignAlbumNameFromBundle();
        setupButtons(view);
    }


    private void assignAlbumNameFromBundle(){
        Bundle bundle = getArguments();
        assert bundle != null;
        albumName = bundle.getString(ALBUM_NAME_BUNDLE_KEY);
    }


    private void setupButtons(View parentView){
        loadTracksButton = ButtonMaker.createButton(parentView, R.id.loadAlbumTracksButton, this::loadAlbumTracks);
        setupAddTracksToPlaylistButton(parentView);
    }


    private void loadAlbumTracks(){
        disableAllButtons();
        notifyAlbumFragmentToLoadAlbum();
        dismissAfterPause();
    }


    private void notifyAlbumFragmentToLoadAlbum(){
        sendMessage(this, NOTIFY_TO_LOAD_ALBUM);
    }


    private void setupAddTracksToPlaylistButton(View parentView){
        if(getMainActivity().isUserPlaylistLoaded()){
            addTracksToPlaylistButton = ButtonMaker.createButton(parentView,
                    R.id.addAlbumTracksToCurrentPlaylistButton,
                    this::addAlbumTracksToCurrentPlaylist);

            if(addTracksToPlaylistButton != null) {
                addTracksToPlaylistButton.setVisibility(View.VISIBLE);
            }
        }
    }


    private void disableAllButtons(){
        loadTracksButton.setEnabled(false);
        if(addTracksToPlaylistButton != null) {
            addTracksToPlaylistButton.setEnabled(false);
        }
    }


    private void addAlbumTracksToCurrentPlaylist(){
        disableAllButtons();
        getMainActivity().getMediaPlayerService().addTracksFromAlbumToCurrentPlaylist(albumName);
        dismissAfterPause();
    }


    private MainActivity getMainActivity(){
        return (MainActivity) getActivity();
    }


    private void dismissAfterPause(){
        new Handler(Looper.getMainLooper()).postDelayed(this::dismiss, 150);
    }


}