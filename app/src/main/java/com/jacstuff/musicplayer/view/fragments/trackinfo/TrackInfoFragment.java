package com.jacstuff.musicplayer.view.fragments.trackinfo;

import static com.jacstuff.musicplayer.view.fragments.DialogFragmentUtils.setScrollViewHeight;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.db.entities.Track;
import com.jacstuff.musicplayer.view.fragments.DialogFragmentUtils;
import com.jacstuff.musicplayer.view.utils.ButtonMaker;
import com.jacstuff.musicplayer.view.utils.TimeConverter;

public class TrackInfoFragment extends DialogFragment {

    public static final String TAG = "track_info_fragment";

    public static TrackInfoFragment newInstance() {
        return new TrackInfoFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_track_info, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupTextViews(view);
        setupButtons(view);
        setScrollViewHeight(this, view, R.id.trackInfoScrollView, R.id.trackInfoLayout);
        DialogFragmentUtils.setTransparentBackground(this);
    }


    private void setupTextViews(View parentView){
        MainActivity mainActivity = getMainActivity();
        if(mainActivity == null){
            return;
        }
        Track track = mainActivity.getSelectedTrack();
        if(track == null){
            dismiss();
            return;
        }
        assignTrackInfoToTextViews(parentView, track);
    }


    private void assignTrackInfoToTextViews(View parentView, Track track){
        setValue(parentView, R.id.track, track.getTitle());
        setValue(parentView, R.id.album, track.getAlbum());
        setValue(parentView, R.id.artist, track.getArtist());
        setValue(parentView, R.id.disc, track.getDisc());
        setValue(parentView, R.id.trackNumber, track.getTrackNumberStr());
        setValue(parentView, R.id.genre, track.getGenre());
        setValue(parentView, R.id.year, track.getYear());
        setValue(parentView, R.id.length, TimeConverter.convert(track.getDuration()));
        setValue(parentView, R.id.path, track.getPathname());
        setValue(parentView, R.id.bitrate, track.getBitrate());
    }


    private void setValue(View parentView, int viewId, String input){
        TextView textView = parentView.findViewById(viewId);
        textView.setText(getFormattedStrFor(input));
    }


    private String getFormattedStrFor(String value){
        String nullPlaceholder = getString(R.string.null_val);
        if(value == null || value.isBlank()){
            return nullPlaceholder;
        }
        return value;
    }


    private void setupButtons(View parentView){
        ButtonMaker.setupButton(parentView, R.id.dismissTrackInfoDialogButton, this::dismiss);
    }


    private MainActivity getMainActivity(){
        Activity activity = getActivity();
        return activity == null ? null : (MainActivity) activity;
    }


}