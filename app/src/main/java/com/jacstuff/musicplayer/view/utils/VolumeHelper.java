package com.jacstuff.musicplayer.view.utils;

import static android.view.View.INVISIBLE;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.helpers.MediaPlayerHelper;
import com.jacstuff.musicplayer.service.helpers.preferences.PrefKey;
import com.jacstuff.musicplayer.view.fragments.volume.CustomVolumeView;

public class VolumeHelper {

    private final MediaPlayerHelper mediaPlayerHelper;
    private final MainActivity mainActivity;

    public VolumeHelper(MainActivity mainActivity, MediaPlayerHelper mediaPlayerHelper ){
        this.mainActivity = mainActivity;
        this.mediaPlayerHelper = mediaPlayerHelper;
        setupVolume();
    }


    private void setupVolume(){
        CustomVolumeView volumeView = mainActivity.findViewById(R.id.volumeControl);
        if(! mainActivity.getPreferencesHelper().getBoolean(PrefKey.IS_VOLUME_CONTROL_SHOWN)){
            volumeView.setVisibility(INVISIBLE);
        }
        else{
            volumeView.setVolume((int)mediaPlayerHelper.getVolume());
            volumeView.setOnVolumeChangeListener(mediaPlayerHelper::setVolume);
        }
    }

}
