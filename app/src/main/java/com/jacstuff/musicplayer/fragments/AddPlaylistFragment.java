package com.jacstuff.musicplayer.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.db.playlist.Playlist;
import com.jacstuff.musicplayer.db.playlist.PlaylistRepository;
import com.jacstuff.musicplayer.db.playlist.PlaylistRepositoryImpl;
import com.jacstuff.musicplayer.fragments.playlist.PlaylistsFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import java.util.Collections;
import java.util.Set;

public class AddPlaylistFragment extends DialogFragment {

    private MainActivity activity;
    private EditText addPlaylistNameEditText;
    private Button createPlaylistButton;
    private PlaylistRepository playlistRepository;
    private Set<String> playlistNames;
    private TextView playlistAlreadyExistsTextView;

    public static AddPlaylistFragment newInstance() {
        return new AddPlaylistFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_playlist, container, false);
        playlistRepository = new PlaylistRepositoryImpl(getContext());
        assignPlaylistNames();
        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Dialog dialog =  getDialog();
        activity = (MainActivity)getActivity();
        if(activity == null){
            return;
        }
        if(dialog != null){
        //    dialog.setTitle(activity.getString(R.string.update_station_dialog_title));
        }

        //setupTitle(activity, view, R.string.update_station_dialog_title);
        setupViews(view);
       // FragmentUtils.setupDimensions(view, activity);
    }


    private void setupViews(View rootView){
        playlistAlreadyExistsTextView = rootView.findViewById(R.id.playlistAlreadyExistsTextView);
        createPlaylistButton = rootView.findViewById(R.id.createPlaylistButton);
        addPlaylistNameEditText = rootView.findViewById(R.id.addPlaylistNameEditText);
        addPlaylistNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                disableButtonIfInputsAreEmpty();
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        setupCreateButton();
    }


    private void setupCreateButton(){
        disableButtonIfInputsAreEmpty();
        createPlaylistButton.setOnClickListener((View v) -> {
            playlistRepository.createPlaylist(getEditText());
            updatePlaylistsOnParentFragment();
            dismiss();
        });
    }


    private void updatePlaylistsOnParentFragment(){
        PlaylistsFragment playlistsFragment = getPlaylistsFragment();
        if(playlistsFragment != null){
            playlistsFragment.onAddNewPlaylist();
        }
    }

    private void assignPlaylistNames(){
        PlaylistsFragment playlistsFragment = getPlaylistsFragment();
        if(playlistsFragment == null){
            playlistNames = Collections.emptySet();
            return;
        }
        playlistNames = playlistsFragment.getPlaylistNames();
    }


    @Override
    public void onDismiss(DialogInterface dialogInterface){
        super.onDismiss(dialogInterface);
        notifyDismissOnParentFragment();
    }


    @Override
    public void dismiss(){
        activity.updatePlaylistList();
        super.dismiss();
    }


    private void notifyDismissOnParentFragment(){
        PlaylistsFragment playlistsFragment = getPlaylistsFragment();
        if(playlistsFragment != null){
            playlistsFragment.onAddDialogDismissed();
        }
    }



    private PlaylistsFragment getPlaylistsFragment(){
        Fragment fragment = getParentFragmentManager().findFragmentByTag("f1");
        if(fragment == null || !fragment.getClass().equals(PlaylistsFragment.class)){
            return null;
        }
        return (PlaylistsFragment) fragment;
    }


    private void log(String msg){
        System.out.println("^^^ AddPlaylistFragment: " + msg);
    }


    private void disableButtonIfInputsAreEmpty(){
        createPlaylistButton.setEnabled(isNameValid() && isNameUnique());
    }


    private boolean isNameValid(){
        return !getEditText().isEmpty();
    }


    private boolean isNameUnique(){
        boolean isNameUnique =  !playlistNames.contains(getEditText().toLowerCase());
        playlistAlreadyExistsTextView.setVisibility(isNameUnique ? View.INVISIBLE : View.VISIBLE);
        return isNameUnique;
    }


    private String getEditText(){
        return addPlaylistNameEditText.getText().toString().trim();
    }


}