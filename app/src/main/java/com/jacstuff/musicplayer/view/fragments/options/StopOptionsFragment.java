package com.jacstuff.musicplayer.view.fragments.options;

import android.os.Bundle;
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

import java.util.function.Consumer;


public class StopOptionsFragment extends DialogFragment {


    public static StopOptionsFragment newInstance() {
        return new StopOptionsFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_dialog_options_stop, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupButtons(view);
    }


    private void setupButtons(View parentView){
        setupButton(parentView, R.id.stopNowButton, this::stopNow);
        setupButton(parentView, R.id.stopAfterCurrentTrackButton, this::stopWhenCurrentTrackEnds);
        setupButton(parentView, R.id.stopInThreeMinutesButton, this::stopInThreeMinutes);
    }


    private void setupButton(View parentView, int id, Runnable runnable){
        Button button = parentView.findViewById(id);
        button.setOnClickListener((View v)-> runnable.run());
    }


    private void stopNow(){
        MainActivity mainActivity = getMainActivity();
        if(mainActivity != null){
            mainActivity.stopTrack();
        }
        dismiss();
    }


    private void stopWhenCurrentTrackEnds(){
        runMediaPlayerServiceCall(MediaPlayerService::enableStopAfterTrackFinishes);
        dismiss();
    }


    private void stopInThreeMinutes(){
        runMediaPlayerServiceCall(MediaPlayerService::stopPlayingInThreeMinutes);
        dismiss();
    }


    private void runMediaPlayerServiceCall(Consumer<MediaPlayerService> consumer){
        MainActivity mainActivity = getMainActivity();
        if(mainActivity != null){
            MediaPlayerService mediaPlayerService = mainActivity.getMediaPlayerService();
            if(mediaPlayerService != null){
                consumer.accept(mediaPlayerService);
            }
        }
    }


    private MainActivity getMainActivity(){
        return (MainActivity) getActivity();
    }

}