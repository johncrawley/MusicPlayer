package com.jacstuff.musicplayer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.jacstuff.musicplayer.service.PlayTrackService;
import com.jacstuff.musicplayer.service.playtrack.TrackPlayerHelper;
import com.jacstuff.musicplayer.view.art.AlbumArtHelper;
import com.jacstuff.musicplayer.view.trackplayer.TrackPlayerViewHelper;
import com.jacstuff.musicplayer.view.utils.ThemeHelper;
import com.jacstuff.musicplayer.view.viewmodel.MainViewModel;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class OpenTrackActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private ThemeHelper themeHelper;
    private AlbumArtHelper albumArtHelper;
    private TrackPlayerHelper trackPlayerHelper;
    TrackPlayerViewHelper trackPlayerViewHelper;
    private PlayTrackService playTrackService;

    private Uri savedUri;
    private AtomicBoolean isServiceConnected = new AtomicBoolean(false);

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            PlayTrackService.LocalBinder binder = (PlayTrackService.LocalBinder) service;
            playTrackService = binder.getService();

           // albumArtHelper = new AlbumArtHelper(MainActivity.this);
            playTrackService.setActivity(OpenTrackActivity.this);
            trackPlayerViewHelper.setService(playTrackService);
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
        //setupViewModel();
        initPlayerViewHelper();
        startMediaPlayerService();
        openUri();
    }



    private void playSavedUri(){
        if(savedUri != null){
            log("entered playSavedUri() going to play the track from the service");
            playTrackService.playUri(savedUri);
            savedUri = null;
        }
    }


    private void startMediaPlayerService(){
        Intent mediaPlayerServiceIntent = new Intent(this, PlayTrackService.class);
        getApplicationContext().startForegroundService(mediaPlayerServiceIntent);
        getApplicationContext().bindService(mediaPlayerServiceIntent, serviceConnection, 0);
    }


    private void initPlayerViewHelper(){
        if(trackPlayerViewHelper == null){
            trackPlayerViewHelper = new TrackPlayerViewHelper(this);
        }
        trackPlayerViewHelper.setupViews();
    }


    private void openUri(){
        Intent intent = getIntent();
        if (intent != null) {
            Uri uri = intent.getData();
            if(isServiceConnected.get()){
                log("openUri() going to play the track from the URI immediately");
                playTrackService.playUri(uri);
            }
            else{
                log("saving uri");
                savedUri = uri;
            }
        }
    }


    public void openFile() {

        Intent intent = getIntent();
        if (intent != null) {
            String path = intent.getDataString();
            Uri uri = intent.getData();
            if (path == null) {
                log("openFile() intent data string is null");
            } else {
                log("openFile() intent data string: " + path);
                log("openFile() intent data: " + intent.getStringExtra(Intent.ACTION_VIEW));
                if(uri != null) {
                    System.out.println("^^^ uri: " + uri.getPath());
                    playOpenedFile(uri);
                }
            }
        }
    }


    private void playOpenedFile(Uri uri){
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            new Handler(Looper.getMainLooper()).postDelayed(()->{
                mediaPlayer.stop();
                mediaPlayer.release();
            }, 5000);
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    private void log(String msg){
        System.out.println("^^^ OpenTrackActivity: " + msg);
    }


}
