package com.jacstuff.musicplayer;

import android.Manifest;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.jacstuff.musicplayer.db.track.Track;
import com.jacstuff.musicplayer.fragments.PlayerFragment;
import com.jacstuff.musicplayer.fragments.PlaylistsFragment;
import com.jacstuff.musicplayer.fragments.ViewStateAdapter;
import com.jacstuff.musicplayer.service.MediaPlayerService;
import com.jacstuff.musicplayer.viewmodel.MainViewModel;

import java.util.List;


public class MainActivity extends AppCompatActivity{


    private ViewStateAdapter viewStateAdapter;
    private MediaPlayerService mediaPlayerService;
    private TextView trackTime;
    private TextView trackTitle, trackAlbum, trackArtist;
    private ImageButton playButton, pauseButton, stopButton;
    private ImageButton nextTrackButton, previousTrackButton;
    private String totalTrackTime = "0:00";
  //  private ListNotifier listNotifier;
    private PlayerFragment playerFragment;
    private ViewGroup playerButtonPanel;


    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            log("Entered onServiceConnected!!");
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mediaPlayerService = binder.getService();
            mediaPlayerService.setActivity(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };


    public void onServiceReady(List<Track> tracks){
        log("Entered onServiceReady(), about to initPlaylist and refresh on mediaPlayerService");
        //getPlayerFragment().onServiceReady(tracks);
        mediaPlayerService.initPlaylist();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("Entered onCreate() *********************************");
        setContentView(R.layout.activity_main);
        setupViews();
        setupTabLayout();
        setupViewModel();
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        startMediaPlayerService();
    }


    public void playTrack() {
        mediaPlayerService.playTrack();
    }


    public void setPlayerFragment(PlayerFragment playerFragment){
        this.playerFragment = playerFragment;
        onQueueFragmentReady();
    }


    public void stopTrack(){
        mediaPlayerService.stop();
        resetElapsedTime();
    }


    public void nextTrack(){
        mediaPlayerService.loadNextTrack();
    }


    public void previousTrack(){
        mediaPlayerService.loadPreviousTrack();
    }


    public void selectTrack(int index) {
        mediaPlayerService.selectTrack(index);
    }


    public List<Track> getTrackList(){
       return mediaPlayerService.getTrackList();
    }


    public void pauseMediaPlayer() {
        mediaPlayerService.pause();
    }


    private void startMediaPlayerService(){
        Intent mediaPlayerServiceIntent = new Intent(this, MediaPlayerService.class);
        getApplicationContext().startForegroundService(mediaPlayerServiceIntent);
        getApplicationContext().bindService(mediaPlayerServiceIntent, serviceConnection, 0);
    }


    private void log(String msg){
        System.out.println("^^^ MainActivity: " + msg);
    }


    private PlayerFragment getPlayerFragment(){
        return (PlayerFragment) getSupportFragmentManager().findFragmentByTag("f1");
    }


    private void setupViews(){
        assignViews();
        assignClickListeners();
        resetElapsedTime();
        playButton.setEnabled(false);
        nextTrackButton.setEnabled(false);
    }


    public void resetElapsedTime(){
        setElapsedTime("0:00");
    }


    public void setElapsedTime(String elapsedTime){
        this.setTrackTime(elapsedTime);
    }


    private void setTrackTime(String elapsedTime){
        runOnUiThread(()->{
            if(trackTime != null){
                String time = elapsedTime + " / " + this.totalTrackTime;
                trackTime.setText(time);
            }
        });
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        if(viewStateAdapter != null) {
            viewStateAdapter.onDestroy();
            viewStateAdapter = null;
        }
    }


    public void setVisibilityOnDetailsAndNavViews(int visibility){
        trackTitle.setVisibility(visibility);
        trackAlbum.setVisibility(visibility);
        trackArtist.setVisibility(visibility);
        trackTime.setVisibility(visibility);
        playerButtonPanel.setVisibility(visibility);
    }


    private void setTrackTimeInfo(int elapsedTime, long trackDuration){
        this.totalTrackTime = TimeConverter.convert(trackDuration);
        setElapsedTime(TimeConverter.convert(elapsedTime));
    }


    private void assignViews(){
        trackTime = findViewById(R.id.trackTime);
        trackTitle = findViewById(R.id.trackTitle);
        trackAlbum = findViewById(R.id.albumTextView);
        trackArtist = findViewById(R.id.artistTextView);
        playerButtonPanel = findViewById(R.id.buttonLayout);
        playButton = findViewById(R.id.playButton);
        pauseButton = findViewById(R.id.pauseButton);
        stopButton = findViewById(R.id.stopButton);
        nextTrackButton = findViewById(R.id.nextTrackButton);
        previousTrackButton = findViewById(R.id.previousTrackButton);
    }


    private void assignClickListeners(){
        playButton.setOnClickListener((View v) -> playTrack());
        pauseButton.setOnClickListener((View v) -> pauseMediaPlayer());
        nextTrackButton.setOnClickListener((View v) -> nextTrack());
        previousTrackButton.setOnClickListener((View v) -> previousTrack());
        stopButton.setOnClickListener((View v) -> stopTrack());
    }


    public void setBlankTrackInfo(){
        runOnUiThread(()-> setTrackInfo(""));
    }


    public void notifyMediaPlayerStopped(){
        playButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
    }


    public void notifyMediaPlayerPaused(){
        playButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
    }


    public void notifyMediaPlayerPlaying(){
        runOnUiThread(()->{
            log("entered notifyMediaPlayerPlaying, setting play Button to Gone, pause button to visible");
            playButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        });
    }


    public void setTrackInfoOnView(final Track track, int elapsedTime){
        runOnUiThread(()-> {
                setTrackInfo(track.getName());
                setAlbumInfo(track.getAlbum());
                setArtistInfo(track.getArtist());
                setTrackTimeInfo(elapsedTime, track.getDuration());
        });
    }


    public void enableControls(){
        runOnUiThread(()->{
            playButton.setEnabled(true);
            nextTrackButton.setEnabled(true);
            previousTrackButton.setEnabled(true);
        });
    }


    public void hideSeekButtonsIfOnlyOneTrack(int numberOfTracks){
        if(numberOfTracks == 1){
            nextTrackButton.setVisibility(View.INVISIBLE);
            previousTrackButton.setVisibility(View.INVISIBLE);
        }
    }


    public void scrollToPosition(int index){
       runOnUiThread(()-> {
           if(playerFragment!= null){
               playerFragment.scrollToListPosition(index);
           }
       });
    }


    public void setTrackInfo(String trackInfo){
        if(trackInfo.isEmpty()){
            trackInfo = getResources().getString(R.string.no_tracks_found);
        }
        this.trackTitle.setText(trackInfo);
    }


    public void setAlbumInfo(String albumInfo){
        this.trackAlbum.setText(albumInfo);
    }


    public void setArtistInfo(String artistInfo){
        this.trackArtist.setText(artistInfo);
    }


    private void setupViewModel(){
        MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);
    }


    private void setupTabLayout(){
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        if(tabLayout == null){
            return;
        }
        viewStateAdapter = new ViewStateAdapter(getSupportFragmentManager(), getLifecycle());

        final ViewPager2 pager = findViewById(R.id.pager);
        pager.setAdapter(viewStateAdapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }


    public void updatePlaylistList(){
        PlaylistsFragment fragment = (PlaylistsFragment)getSupportFragmentManager().findFragmentByTag("f1");
        fragment.onAddNewPlaylist();
    }


    public void updateTracksList(List<Track> updatedTracks, int currentTrackIndex){
        runOnUiThread(()-> {
            log("Entered updateTracksList, number of tracks: " + updatedTracks.size());
           // listNotifier.setTracks(updatedTracks);
            if(playerFragment!= null){
                log("updateTracksList() playerFragment is not null, so updating tracks list to it, current index: " + currentTrackIndex);
                playerFragment.updateTracksList(updatedTracks, currentTrackIndex);
            }
            updateViews(updatedTracks);
        });
    }


    public void onQueueFragmentReady(){
        startMediaPlayerService();
    }


    private void updateViews(List<Track> updatedTracks){
        if(updatedTracks.isEmpty()){
            setVisibilityOnDetailsAndNavViews(View.INVISIBLE);
            return;
        }
        setVisibilityOnDetailsAndNavViews(View.VISIBLE);
        hideSeekButtonsIfOnlyOneTrack(updatedTracks.size());
    }


    public void displayPlaylistRefreshedMessage(){
        String msg = getResources().getString(R.string.playlist_refreshed_message);
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }


    public void displayPlaylistRefreshedMessage(int newTrackCount) {
        runOnUiThread(() -> displayPlaylistMessage(newTrackCount));
    }


    public void displayPlaylistMessage(int newTrackCount) {
        if(newTrackCount == 0){
            displayPlaylistRefreshedMessage();
            return;
        }
        String msg = newTrackCount > 1 ?
                getResources().getString(R.string.playlist_refreshed_message_new_tracks_count, newTrackCount)
                : getResources().getString(R.string.playlist_refreshed_one_new_track);
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
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
          mediaPlayerService.scanForTracks();
        }
        else if(id == R.id.test_stop_after_current){
            mediaPlayerService.enableStopAfterTrackFinishes();
        }
        return super.onOptionsItemSelected(item);
    }


}
