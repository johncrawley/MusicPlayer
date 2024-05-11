package com.jacstuff.musicplayer.view.fragments.playlist;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.db.entities.Playlist;
import com.jacstuff.musicplayer.service.db.playlist.PlaylistRepository;
import com.jacstuff.musicplayer.service.db.playlist.PlaylistRepositoryImpl;
import com.jacstuff.musicplayer.view.fragments.DialogFragmentUtils;
import com.jacstuff.musicplayer.view.utils.KeyboardHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class CreatePlaylistFragment extends DialogFragment {

    private EditText addPlaylistNameEditText;
    private Button createPlaylistButton;
    private PlaylistRepository playlistRepository;
    private Set<String> playlistNames;
    private TextView playlistAlreadyExistsTextView;
    private Animation fadeIn, fadeOut;


    public static CreatePlaylistFragment newInstance() {
        return new CreatePlaylistFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_add_playlist, container, false);
        playlistRepository = new PlaylistRepositoryImpl(getContext());
        setupKeyboardHelper();
        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        assignPlaylistNames();
        setupKeyboardHelper();
        DialogFragmentUtils.setTransparentBackground(this);
    }


    private void setupKeyboardHelper(){
        Activity activity = getActivity();
        if(activity == null){
            return;
        }
        KeyboardHelper keyboardHelper = new KeyboardHelper(activity);
        keyboardHelper.showKeyboardAndFocusOn(addPlaylistNameEditText);
    }


    private void setupViews(View rootView){
        playlistAlreadyExistsTextView = rootView.findViewById(R.id.playlistAlreadyExistsTextView);
        createPlaylistButton = rootView.findViewById(R.id.createPlaylistButton);
        addPlaylistNameEditText = rootView.findViewById(R.id.addPlaylistNameEditText);
        initAnimations();
        setupTextChangedListener();
        setupCreateButton();
        setupCancelButton(rootView);
    }


    private void setupTextChangedListener(){
        addPlaylistNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                disableCreateButtonIfInputsAreEmpty();
            }

            @Override public void afterTextChanged(Editable editable) { }
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
        });


        addPlaylistNameEditText.setOnEditorActionListener((view, i, keyEvent) -> {
            if(i == IME_ACTION_DONE){
                createPlaylistAndDismiss();
            }
            return false;
        });
    }


    private void setupCreateButton(){
        disableCreateButtonIfInputsAreEmpty();
        createPlaylistButton.setOnClickListener((View v) -> createPlaylistAndDismiss());
    }


    private void createPlaylistAndDismiss(){
        if(isInputValid()){
            playlistRepository.createPlaylist(getEditText());
            updatePlaylistsOnParentFragment();
            dismiss();
        }
    }


    private void setupCancelButton(View parentView){
        Button cancelButton = parentView.findViewById(R.id.cancelDialogButton);
        cancelButton.setOnClickListener((View v)->dismiss());
    }


    private void updatePlaylistsOnParentFragment(){
        PlaylistsFragment playlistsFragment = getPlaylistsFragment();
        if(playlistsFragment != null){
            playlistsFragment.onAddNewPlaylist();
        }
    }


    private void assignPlaylistNames(){
        var mainActivity = (MainActivity)getActivity();
        if(mainActivity == null || mainActivity.getMediaPlayerService() == null){
            playlistNames = Collections.emptySet();
            new Handler(Looper.getMainLooper()).postDelayed(this::assignPlaylistNames, 200);
        }
        else {
            playlistNames = mainActivity.getMediaPlayerService()
                    .getPlaylistManager()
                    .getAllPlaylists()
                    .stream()
                    .map(Playlist::getName)
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
        }
        disableCreateButtonIfInputsAreEmpty();
    }


    @Override
    @SuppressWarnings("@NonNull")
    public void onDismiss(@NonNull DialogInterface dialogInterface){
        super.onDismiss(dialogInterface);
        notifyDismissOnParentFragment();
    }


    @Override
    public void dismiss(){
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


    private void disableCreateButtonIfInputsAreEmpty(){
        createPlaylistButton.setEnabled(isInputValid());
        if(!isInputValid()){
            fadeOutCreateButton();
        }
        else{
            fadeInCreateButtonIfInvisible();
        }
    }


    private boolean isInputValid(){
        return isNameValid() && isNameUnique();
    }


    private void fadeInCreateButtonIfInvisible(){
        if(createPlaylistButton.getVisibility() == View.INVISIBLE){
            createPlaylistButton.setVisibility(View.VISIBLE);
            createPlaylistButton.startAnimation(fadeIn);
        }
    }


    private void fadeOutCreateButton(){
        createPlaylistButton.startAnimation(fadeOut);
    }


    private void initAnimations(){
        initFadeInAnimation();
        initFadeOutAnimation();
    }


    private void initFadeInAnimation(){
        fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation) {
            }
        });
    }


    private void initFadeOutAnimation(){
        fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation) {
                createPlaylistButton.setVisibility(View.INVISIBLE);
            }
        });
    }



    private boolean isNameValid(){
        return !getEditText().isEmpty();
    }


    private boolean isNameUnique(){
        if(playlistNames.isEmpty()){
            return false;
        }
        boolean isNameUnique =  !playlistNames.contains(getEditText().toLowerCase());
        playlistAlreadyExistsTextView.setVisibility(isNameUnique ? View.INVISIBLE : View.VISIBLE);
        return isNameUnique;
    }


    private String getEditText(){
        return addPlaylistNameEditText.getText().toString().trim();
    }




}