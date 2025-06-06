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

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.MediaPlayerService;
import com.jacstuff.musicplayer.view.fragments.DialogFragmentUtils;
import com.jacstuff.musicplayer.view.fragments.Message;

import java.util.Optional;


public class AddRandomTracksFragment extends DialogFragment {


    private Button okButton, cancelButton;
    public static String TAG = "ADD_RANDOM_TRACKS_FRAGMENT";


    public static PlaylistOptionsFragment newInstance() {
        return new PlaylistOptionsFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_playlist_add_random_tracks, container, false);
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
    }


    private void setupButtons(View parentView){
        okButton = setupButton(parentView, R.id.okButton, this::addRandomTracks);
        cancelButton = setupButton(parentView, R.id.cancelDialogButton, this::dismissDialog);

    }


    private void dismissDialog(){
        disableAllButtons();
        dismissAfterPause();
    }


    private void addRandomTracks(){
        disableAllButtons();
        getService().ifPresent( service -> service.getPlaylistHelper().addRandomTracksFromAristToCurrentPlaylist(""));
        dismissAfterPause();
    }


    private Optional<MediaPlayerService> getService(){
        MainActivity mainActivity = (MainActivity) getActivity();
        return mainActivity == null ? Optional.empty() : Optional.of(mainActivity.getMediaPlayerService());
    }


    private void disableAllButtons(){
        okButton.setEnabled(false);
        cancelButton.setEnabled(false);
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
