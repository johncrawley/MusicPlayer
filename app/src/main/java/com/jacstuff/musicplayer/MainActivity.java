package com.jacstuff.musicplayer;

import static com.jacstuff.musicplayer.service.MediaPlayerService.ACTION_NOTIFY_VIEW_OF_CONNECTING;
import static com.jacstuff.musicplayer.service.MediaPlayerService.ACTION_NOTIFY_VIEW_OF_ERROR;
import static com.jacstuff.musicplayer.service.MediaPlayerService.ACTION_NOTIFY_VIEW_OF_PLAYING;
import static com.jacstuff.musicplayer.service.MediaPlayerService.ACTION_NOTIFY_VIEW_OF_STOP;
import static com.jacstuff.musicplayer.service.MediaPlayerService.ACTION_SELECT_NEXT_TRACK;
import static com.jacstuff.musicplayer.service.MediaPlayerService.ACTION_SELECT_PREVIOUS_TRACK;

import android.Manifest;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.jacstuff.musicplayer.fragments.ViewStateAdapter;
import com.jacstuff.musicplayer.service.MediaPlayerService;
import com.jacstuff.musicplayer.viewmodel.MainViewModel;


public class MainActivity extends AppCompatActivity{


    private ViewStateAdapter viewStateAdapter;
    private boolean isServiceBound;
    private Intent mediaPlayerServiceIntent;


    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName className, IBinder service) { isServiceBound = true; }
        @Override public void onServiceDisconnected(ComponentName arg0) {
            isServiceBound = false;
        }
    };


    private final BroadcastReceiver serviceReceiverForPreviousTrack = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            selectPreviousTrack();
        }
    };

    private final BroadcastReceiver serviceReceiverForNextTrack = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            selectNextTrack();
        }
    };

    private final BroadcastReceiver serviceReceiverForNotifyStop = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            notifyPlayerStopped();
        }
    };

    private final BroadcastReceiver serviceReceiverForNotifyConnecting = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            notifyPlayerConnecting();
        }
    };

    private final BroadcastReceiver serviceReceiverForNotifyPlaying = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            notifyPlayerPlaying();
        }
    };

    private final BroadcastReceiver serviceReceiverForNotifyError = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            notifyPlayError();
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
        setupBroadcastReceivers();
        //listAudioFiles();
    }


    @Override
    protected  void onDestroy(){
        super.onDestroy();
        unregisterReceiver(serviceReceiverForPreviousTrack);
        unregisterReceiver(serviceReceiverForNextTrack);
        unregisterReceiver(serviceReceiverForNotifyStop);
        unregisterReceiver(serviceReceiverForNotifyConnecting);
        unregisterReceiver(serviceReceiverForNotifyPlaying);
        unregisterReceiver(serviceReceiverForNotifyError);

        //mediaController.finish();
    }


    public void sendPlayBroadcast(String trackUrl, String trackName) {
        Intent intent = new Intent(MediaPlayerService.ACTION_START_PLAYER);
        intent.putExtra(MediaPlayerService.TAG_TRACK_URL, trackUrl);
        intent.putExtra(MediaPlayerService.TAG_TRACK_NAME, trackName);
        sendBroadcast(intent);
    }


    public void sendPauseBroadcast() {
        Intent intent = new Intent(MediaPlayerService.ACTION_PAUSE_PLAYER);
        sendBroadcast(intent);
    }


    @Override
    protected void onStart(){
        super.onStart();
        bindService();
    }


    @Override
    protected void onStop() {
        super.onStop();
        unbindService();
    }


    private void bindService() {
        bindService(mediaPlayerServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }


    private void unbindService(){
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
    }


    private void startMediaPlayerService(){
        mediaPlayerServiceIntent = new Intent(this, MediaPlayerService.class);
        log("entered startMediaPlayerService(), about to start foreground media player service");
        getApplicationContext().startForegroundService(mediaPlayerServiceIntent);
    }

    private void log(String msg){
        System.out.println("^^^ MainActivity: " + msg);
    }

    private void notifyPlayerStopped(){

    }


    private void notifyPlayerConnecting(){

    }


    private void notifyPlayerPlaying(){

    }



    private void notifyPlayError(){

    }

    private void selectNextTrack(){

    }

    private void selectPreviousTrack(){

    }


    private void setupBroadcastReceivers(){
        register(serviceReceiverForPreviousTrack, ACTION_SELECT_PREVIOUS_TRACK);
        register(serviceReceiverForNextTrack, ACTION_SELECT_NEXT_TRACK);
        register(serviceReceiverForNotifyStop, ACTION_NOTIFY_VIEW_OF_STOP);
        register(serviceReceiverForNotifyConnecting, ACTION_NOTIFY_VIEW_OF_CONNECTING);
        register(serviceReceiverForNotifyPlaying, ACTION_NOTIFY_VIEW_OF_PLAYING);
        register(serviceReceiverForNotifyError, ACTION_NOTIFY_VIEW_OF_ERROR);
    }


    private void register(BroadcastReceiver receiver, String action) {
        registerReceiver(receiver, new IntentFilter(action));
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
