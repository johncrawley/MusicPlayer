package com.jacstuff.musicplayer.fragments;

import android.app.AlertDialog;
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

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class AddPlaylistFragment extends DialogFragment {

    private MainActivity activity;
    private EditText addPlaylistNameEditText;
    public long stationId;
    private Button createPlaylistButton;
    private AlertDialog.Builder deleteConfirmationDialog;


    public static AddPlaylistFragment newInstance() {
        return new AddPlaylistFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_playlist, container, false);
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

       // disableButtonWhenAnyEmptyInputs(updateButton, stationNameEditText, stationUrlEditText);
        setupCreateButton();
        setupCancelButton(rootView);
    }


    private void setupCreateButton(){
        disableButtonIfInputsAreEmpty();
        createPlaylistButton.setOnClickListener((View v) -> {
           //TODO: add repository save here
            dismiss();
        });
    }


    private void disableButtonIfInputsAreEmpty(){
        createPlaylistButton.setEnabled(isNameValid());
    }

    private boolean isNameValid(){
        return addPlaylistNameEditText.getText().toString().trim().isEmpty();
    }


    private void setupCancelButton(View parentView){
        Button cancelButton = parentView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener((View v)-> dismiss());
    }

}