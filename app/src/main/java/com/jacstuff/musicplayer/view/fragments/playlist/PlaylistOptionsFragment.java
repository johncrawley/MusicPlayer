package com.jacstuff.musicplayer.view.fragments.playlist;

import static android.view.View.GONE;
import static com.jacstuff.musicplayer.view.fragments.DialogFragmentUtils.addStrTo;
import static com.jacstuff.musicplayer.view.fragments.DialogFragmentUtils.getBundleStr;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_PLAYLISTS_FRAGMENT_TO_DELETE;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_PLAYLISTS_FRAGMENT_TO_LOAD;
import static com.jacstuff.musicplayer.view.fragments.MessageKey.IS_USER_PLAYLIST;
import static com.jacstuff.musicplayer.view.fragments.Utils.getBoolean;
import static com.jacstuff.musicplayer.view.fragments.Utils.getLong;
import static com.jacstuff.musicplayer.view.fragments.Utils.putLong;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.MediaPlayerService;
import com.jacstuff.musicplayer.service.db.entities.PlaylistType;
import com.jacstuff.musicplayer.view.fragments.AlertHelper;
import com.jacstuff.musicplayer.view.fragments.DialogFragmentUtils;
import com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper;
import com.jacstuff.musicplayer.view.fragments.Message;
import com.jacstuff.musicplayer.view.fragments.MessageKey;

import java.util.Optional;


public class PlaylistOptionsFragment extends DialogFragment {


    private Button loadPlaylistButton, deletePlaylistButton, addRandomTracksButton, clearTracksButton;
    private boolean isUserPlaylist;
    private String selectedPlaylistName;
    private long selectedPlaylistId;


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
        selectedPlaylistName =  getBundleStr(bundle, MessageKey.PLAYLIST_NAME);
        selectedPlaylistId = getLong(bundle, MessageKey.PLAYLIST_ID);
    }


    private void setupButtons(View parentView){
        loadPlaylistButton = setupButton(parentView, R.id.loadPlaylistButton, ()-> sendMessage(NOTIFY_PLAYLISTS_FRAGMENT_TO_LOAD));
        deletePlaylistButton = setupButton(parentView, R.id.removePlaylistButton, ()-> sendMessage(NOTIFY_PLAYLISTS_FRAGMENT_TO_DELETE));
        addRandomTracksButton = setupButton(parentView, R.id.addRandomTracksButton, this::loadAddRandomTracksFragment);
        clearTracksButton = setupButton(parentView, R.id.clearTracksButton, this::showClearTracksConfirmationDialog);
        hideSomeButtonsIfPlaylistIsReadOnly();
        hideClearTracksButtonIfPlaylistIsEmpty();
    }


    private void showClearTracksConfirmationDialog(){
        if(selectedPlaylistName == null){
            return;
        }
        dismissAfterPause();
        AlertHelper.showDialogForPlaylist(getContext(),
                R.string.clear_tracks_confirm_dialog_title,
                R.string.clear_tracks_confirm_dialog_text,
                selectedPlaylistName,
                this::clearTracksFromPlaylist);
    }


    private void clearTracksFromPlaylist(){
        getMediaPlayerService().ifPresent( mps -> mps.getPlaylistHelper().clearTracksFromPlaylist(selectedPlaylistId));
    }


    private void loadAddRandomTracksFragment(){
        dismiss();
        var bundle = new Bundle();
        addStrTo(bundle, MessageKey.PLAYLIST_NAME, selectedPlaylistName);
        addStrTo(bundle, MessageKey.PLAYLIST_TYPE, PlaylistType.GENRE.name());
        putLong(bundle, MessageKey.PLAYLIST_ID, selectedPlaylistId);
        FragmentManagerHelper.showDialog(this, new AddRandomTracksFragment(), AddRandomTracksFragment.TAG, bundle);
    }


    private void hideSomeButtonsIfPlaylistIsReadOnly(){
        if(!isUserPlaylist){
            deletePlaylistButton.setVisibility(GONE);
            addRandomTracksButton.setVisibility(GONE);
            clearTracksButton.setVisibility(GONE);
        }
    }


    private void hideClearTracksButtonIfPlaylistIsEmpty(){
        getMediaPlayerService().ifPresent(mps->{
            if(mps.getPlaylistManager().isPlaylistEmpty(selectedPlaylistId)){
                clearTracksButton.setVisibility(GONE);
            }
        });
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


    private Optional<MediaPlayerService> getMediaPlayerService(){
        MainActivity mainActivity = (MainActivity) getActivity();
        if(mainActivity == null){
            return Optional.empty();
        }
        var mps = mainActivity.getMediaPlayerService();

        return mps == null ? Optional.empty() : Optional.of(mainActivity.getMediaPlayerService());
    }



}
