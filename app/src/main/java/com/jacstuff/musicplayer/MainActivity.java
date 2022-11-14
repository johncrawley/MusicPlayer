package com.jacstuff.musicplayer;

import static com.jacstuff.musicplayer.service.MediaPlayerService.ACTION_NOTIFY_VIEW_OF_CONNECTING;
import static com.jacstuff.musicplayer.service.MediaPlayerService.ACTION_NOTIFY_VIEW_OF_ERROR;
import static com.jacstuff.musicplayer.service.MediaPlayerService.ACTION_NOTIFY_VIEW_OF_PLAYING;
import static com.jacstuff.musicplayer.service.MediaPlayerService.ACTION_NOTIFY_VIEW_OF_STOP;
import static com.jacstuff.musicplayer.service.MediaPlayerService.ACTION_SELECT_NEXT_STATION;
import static com.jacstuff.musicplayer.service.MediaPlayerService.ACTION_SELECT_PREVIOUS_STATION;

import android.Manifest;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.google.android.material.tabs.TabLayout;
import com.jacstuff.musicplayer.fragments.ViewStateAdapter;
import com.jacstuff.musicplayer.viewmodel.MainViewModel;


public class MainActivity extends AppCompatActivity{


    private ViewStateAdapter viewStateAdapter;

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
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        setupTabLayout();
        //listAudioFiles();
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
        register(serviceReceiverForPreviousTrack, ACTION_SELECT_PREVIOUS_STATION);
        register(serviceReceiverForNextTrack, ACTION_SELECT_NEXT_STATION);
        register(serviceReceiverForNotifyStop, ACTION_NOTIFY_VIEW_OF_STOP);
        register(serviceReceiverForNotifyConnecting, ACTION_NOTIFY_VIEW_OF_CONNECTING);
        register(serviceReceiverForNotifyPlaying, ACTION_NOTIFY_VIEW_OF_PLAYING);
        register(serviceReceiverForNotifyError, ACTION_NOTIFY_VIEW_OF_ERROR);
    }


    private void register(BroadcastReceiver serviceReceiverForNotifyStop, String actionNotifyViewOfStop) {


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



    public void onDestroy(){
      //  mediaController.finish();
        super.onDestroy();
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
          //  mediaController.scanForTracks();
        }
        return super.onOptionsItemSelected(item);
    }


}
