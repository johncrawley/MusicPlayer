package com.jacstuff.musicplayer.trackplayer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.trackplayer.service.PlayTrackService;
import com.jacstuff.musicplayer.trackplayer.view.OpenTrackViewImpl;

import java.util.concurrent.atomic.AtomicBoolean;

public class OpenTrackActivity extends AppCompatActivity {


    private OpenTrackViewImpl view;
    private PlayTrackService playTrackService;
    private Uri savedUri;
    private final AtomicBoolean isServiceConnected = new AtomicBoolean(false);

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            PlayTrackService.LocalBinder binder = (PlayTrackService.LocalBinder) service;
            playTrackService = binder.getService();
            view.setService(playTrackService);
            playTrackService.setActivity(OpenTrackActivity.this);
            isServiceConnected.set(true);
            playSavedUri();
        }
        @Override public void onServiceDisconnected(ComponentName arg0) {
            isServiceConnected.set(false);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestPermissions();
        //assignTheme();
        setContentView(R.layout.activity_open_track);
        openUri();
        initPlayerViewHelper();
        startMediaPlayerService();
        handleBackButton();
    }


    private void playSavedUri(){
        if(savedUri != null){
            playTrackService.playUri(savedUri);
            savedUri = null;
        }
    }


    public void handleBackButton(){
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                playTrackService.stopSelf();
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }


    private void startMediaPlayerService(){
        Intent mediaPlayerServiceIntent = new Intent(this, PlayTrackService.class);
        getApplicationContext().startService(mediaPlayerServiceIntent);
        getApplicationContext().bindService(mediaPlayerServiceIntent, serviceConnection, 0);
    }


    private void initPlayerViewHelper(){
        if(view == null){
            view = new OpenTrackViewImpl(this);
        }
        view.setupViews();
    }


    private void openUri(){
        Intent intent = getIntent();
        if (intent != null) {
            Uri uri = intent.getData();
            log("saving uri");
            savedUri = uri;
        }
    }


    private void log(String msg){
        System.out.println("^^^ OpenTrackActivity: " + msg);
    }
}
