package com.jacstuff.musicplayer;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;

import static com.jacstuff.musicplayer.HandlerCode.UPDATE_TIME;

public class TrackTimeUpdater implements Runnable{

    private final MediaPlayer mediaPlayer;
    private final Handler handler;


    public TrackTimeUpdater(MediaPlayer mediaPlayer, Handler handler) {
        this.mediaPlayer = mediaPlayer;
        this.handler = handler;
    }


    @Override
    public void run() {
        if(!mediaPlayer.isPlaying()) {
            return;
        }
        Message message = handler.obtainMessage(UPDATE_TIME, TimeConverter.convert(mediaPlayer.getCurrentPosition()));
        message.sendToTarget();
    }

}
