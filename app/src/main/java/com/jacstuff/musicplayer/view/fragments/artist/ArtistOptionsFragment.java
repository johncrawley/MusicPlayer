package com.jacstuff.musicplayer.view.fragments.artist;

import static android.view.View.VISIBLE;
import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.sendMessage;

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
import com.jacstuff.musicplayer.view.fragments.Message;
import com.jacstuff.musicplayer.view.utils.ButtonMaker;

public class ArtistOptionsFragment extends DialogFragment {

    private String artistName;
    public final static String ARTIST_NAME_BUNDLE_KEY = "artist_name_key";
    private Button addTracksToPlaylistButton, loadTracksButton;

    public static ArtistOptionsFragment newInstance() {
        return new ArtistOptionsFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_options_artist, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        assignArtistNameFromBundle();
        setupButtons(view);
        DialogFragmentUtils.setTransparentBackground(this);
    }


    private void assignArtistNameFromBundle(){
        Bundle bundle = getArguments();
        assert bundle != null;
        artistName = bundle.getString(ARTIST_NAME_BUNDLE_KEY);
    }


    private void setupButtons(View parentView){
        loadTracksButton = ButtonMaker.setupButton(parentView, R.id.loadArtistTracksButton, this::loadArtistTracks);
        setupUserPlaylistButtons(parentView);
    }


    private void loadArtistTracks(){
        disableAllButtons();
        notifyArtistFragmentToLoadArtist();
        dismissAfterPause();
    }


    private void notifyArtistFragmentToLoadArtist(){
        sendMessage(this, Message.NOTIFY_TO_LOAD_ARTIST);
    }


    private void setupUserPlaylistButtons(View parentView){
        if(getMainActivity().isUserPlaylistLoaded()){
            addTracksToPlaylistButton = ButtonMaker.setupButton(parentView,
                    R.id.addArtistTracksToCurrentPlaylistButton,
                    this::addArtistTracksToCurrentPlaylist);

            if(addTracksToPlaylistButton != null) {
                addTracksToPlaylistButton.setVisibility(VISIBLE);
            }

            setupButtonAndMakeVisible(parentView,
                    R.id.addArtistTracksToCurrentPlaylistButton,
                    this::addArtistTracksToCurrentPlaylist);


            setupButtonAndMakeVisible(parentView,
                    R.id.addRandomTracksToCurrentPlaylistButton,
                    this::addArtistTracksToCurrentPlaylist);
        }
    }


    private Button setupButtonAndMakeVisible(View parentView, int buttonId, Runnable runnable){
        var button = ButtonMaker.setupButton(parentView,
                buttonId,
                runnable);

        if(button != null){
            button.setVisibility(VISIBLE);
        }


        return button;
    }


    private void disableAllButtons(){
        loadTracksButton.setEnabled(false);
        if(addTracksToPlaylistButton != null) {
            addTracksToPlaylistButton.setEnabled(false);
        }
    }


    private void log(String msg){
        System.out.println("^^ ArtistOptionsFragment: " + msg);
    }


    private void addArtistTracksToCurrentPlaylist(){
        disableAllButtons();
        log("addArtistTracksToCurrentPlaylist() artistName: " + artistName);
        getService().addTracksFromAristToCurrentPlaylist(artistName);
        dismissAfterPause();
    }


    private void addRandomTracksToCurrentPlaylist(){
        disableAllButtons();
        getService().addTracksFromAristToCurrentPlaylist(artistName);
        dismissAfterPause();
    }


    private MediaPlayerService getService(){
        return getMainActivity().getMediaPlayerService();
    }


    private MainActivity getMainActivity(){
        return (MainActivity) getActivity();
    }


    private void dismissAfterPause(){
        new Handler(Looper.getMainLooper()).postDelayed(this::dismiss, 150);
    }


}