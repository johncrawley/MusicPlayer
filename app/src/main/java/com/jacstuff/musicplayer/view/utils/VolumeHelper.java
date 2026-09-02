package com.jacstuff.musicplayer.view.utils;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.util.TypedValue;

import androidx.annotation.ColorInt;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.helpers.MediaPlayerHelper;
import com.jacstuff.musicplayer.service.helpers.preferences.PrefKey;
import com.jacstuff.musicplayer.view.fragments.volume.CustomVolumeView;

public class VolumeHelper {

    private MediaPlayerHelper mediaPlayerHelper;
    private final MainActivity mainActivity;

    public VolumeHelper(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }


    public void setMediaPlayerHelper(MediaPlayerHelper mediaPlayerHelper){
        this.mediaPlayerHelper = mediaPlayerHelper;
        setupVolume();
    }


    private void setupVolume(){
        CustomVolumeView volumeView = mainActivity.findViewById(R.id.volumeControl);
        setProgressColorOf(volumeView);
        setVisibility();
    }


    public void onStart(){
        if(mediaPlayerHelper != null){
            setVisibility();
        }
    }


    private void setProgressColorOf(CustomVolumeView volumeView){
        var typedValue = new TypedValue();
        var theme = mainActivity.getTheme();
        theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true);

        @ColorInt int color = typedValue.data;
        volumeView.setProgressColor(color);
    }


    private void setVisibility(){
        if(mediaPlayerHelper == null){
            return;
        }
        CustomVolumeView volumeView = mainActivity.findViewById(R.id.volumeControl);
        if(! mainActivity.getPreferencesHelper().getBoolean(PrefKey.IS_VOLUME_CONTROL_SHOWN)){
            volumeView.setVisibility(INVISIBLE);
            mediaPlayerHelper.setMaxVolume();
        }
        else{
            volumeView.setVisibility(VISIBLE);
            volumeView.setVolume((int)mediaPlayerHelper.getVolume());
            volumeView.setOnVolumeChangeListener(mediaPlayerHelper::setVolume);
        }
    }

}
