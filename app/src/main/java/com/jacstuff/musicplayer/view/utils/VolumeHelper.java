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
        int progressColor = getColorFromAttribute(androidx.appcompat.R.attr.colorAccent);
        int backgroundColor = getColorFromAttribute(R.attr.seek_bar_background);
        volumeView.setColors(progressColor, backgroundColor);
    }


    private int getColorFromAttribute(int id){
        var typedValue = new TypedValue();
        var theme = mainActivity.getTheme();
        theme.resolveAttribute(id, typedValue, true);

        @ColorInt int color = typedValue.data;
        return color;
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
