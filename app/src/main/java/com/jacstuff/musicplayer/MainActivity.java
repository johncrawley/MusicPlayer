package com.jacstuff.musicplayer;

import android.Manifest;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.jacstuff.musicplayer.db.track.Track;
import com.jacstuff.musicplayer.fragments.ViewStateAdapter;
import com.jacstuff.musicplayer.service.MediaPlayerService;
import com.jacstuff.musicplayer.viewmodel.MainViewModel;

import java.util.List;


public class MainActivity extends AppCompatActivity{


    private ViewStateAdapter viewStateAdapter;
    private boolean isServiceBound;
    private Intent mediaPlayerServiceIntent;
    private MediaPlayerService mediaPlayerService;


    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mediaPlayerService = binder.getService();
            mediaPlayerService.setActivity(MainActivity.this);
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isServiceBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context context = MainActivity.this;
        setupViewModel();
        startMediaPlayerService();
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        setupTabLayout();
        //listAudioFiles();
    }


    public void playTrack(String trackUrl, String trackName) {
        mediaPlayerService.playTrack(trackUrl, trackName);
    }


    public void selectTrack(String trackUrl, String trackName) {
        mediaPlayerService.selectTrack(trackUrl, trackName);
    }


    public void pauseMediaPlayer() {
        mediaPlayerService.pause();
    }


    public void updateTracksOnMediaPlayer(List<Track> tracks){
        mediaPlayerService.updateTracks(tracks);
    }


    private void startMediaPlayerService(){
        mediaPlayerServiceIntent = new Intent(this, MediaPlayerService.class);
        log("entered startMediaPlayerService(), about to start foreground media player service");
        getApplicationContext().startForegroundService(mediaPlayerServiceIntent);
        getApplicationContext().bindService(mediaPlayerServiceIntent, serviceConnection, 0);
    }

    private void log(String msg){
        System.out.println("^^^ MainActivity: " + msg);
    }


    private void notifyPlayerStopped(){
        log("Entered notifyPlayerStopped()");
        viewStateAdapter.getPlayerFragment().notifyTrackPaused();
    }


    public void notifyMediaPlayerPaused(){
        log("Entered notifyPlayerPaused()");
        viewStateAdapter.getPlayerFragment().notifyTrackPaused();
    }


    private void notifyPlayerConnecting(){

    }


    public void notifyPlayerPlaying(){
        viewStateAdapter.getPlayerFragment().notifyTrackPlaying();
    }



    private void notifyPlayError(){

    }

    private void selectNextTrack(){

    }

    private void selectPreviousTrack(){

    }


    private void setupViewModel(){
        MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);
    }


    private void setupTabLayout(){
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        viewStateAdapter = new ViewStateAdapter(getSupportFragmentManager(), getLifecycle());
        final ViewPager2 pager = findViewById(R.id.pager);
        pager.setAdapter(viewStateAdapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public void updatePlaylistList(){
        viewStateAdapter.getPlaylistsFragment().onAddNewPlaylist();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.refresh_button, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.refresh_button) {
          viewStateAdapter.getPlayerFragment().scanForTracks();
        }
        return super.onOptionsItemSelected(item);
    }


}
