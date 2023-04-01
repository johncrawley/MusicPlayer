package com.jacstuff.musicplayer;

import android.Manifest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.jacstuff.musicplayer.service.db.playlist.Playlist;
import com.jacstuff.musicplayer.service.db.track.Track;
import com.jacstuff.musicplayer.service.playlist.PlaylistManager;
import com.jacstuff.musicplayer.view.fragments.options.StopOptionsFragment;
import com.jacstuff.musicplayer.view.fragments.tracks.TracksFragment;
import com.jacstuff.musicplayer.view.fragments.playlist.PlaylistsFragment;
import com.jacstuff.musicplayer.view.playlist.AddTrackToPlaylistViewHelper;
import com.jacstuff.musicplayer.view.search.SearchViewHelper;
import com.jacstuff.musicplayer.view.tab.TabsViewStateAdapter;
import com.jacstuff.musicplayer.service.MediaPlayerService;
import com.jacstuff.musicplayer.view.utils.ThemeHelper;
import com.jacstuff.musicplayer.view.art.AlbumArtHelper;
import com.jacstuff.musicplayer.view.utils.FragmentHelper;
import com.jacstuff.musicplayer.view.utils.TimeConverter;
import com.jacstuff.musicplayer.view.tab.TabHelper;
import com.jacstuff.musicplayer.view.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final  String BUNDLE_KEY_ALBUM_UPDATES = "bundle_key_album_updates";
    public static final  String BUNDLE_KEY_ARTIST_UPDATES = "bundle_key_artist_updates";
    public static final String SEND_ALBUMS_TO_FRAGMENT = "send_albums_to_fragment";
    public static final String SEND_ARTISTS_TO_FRAGMENT = "send_artists_to_fragment";
    private TabsViewStateAdapter tabsViewStateAdapter;
    private MediaPlayerService mediaPlayerService;
    private TextView trackTime, trackTitle, trackAlbum, trackArtist;
    private ImageButton playButton, pauseButton, stopButton, nextTrackButton, previousTrackButton, turnShuffleOnButton, turnShuffleOffButton;
    private SeekBar trackTimeSeekBar;
    private boolean isTrackTimeSeekBarHeld = false;
    private String totalTrackTime = "0:00";
    private TracksFragment tracksFragment;
    private ViewGroup playerButtonPanel;
    private TabLayout tabLayout;
    private Track selectedTrack;
    private SearchViewHelper searchViewHelper;
    private MainViewModel viewModel;
    private ThemeHelper themeHelper;
    private AlbumArtHelper albumArtHelper;
    private AddTrackToPlaylistViewHelper addTrackToPlaylistViewHelper;


    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mediaPlayerService = binder.getService();
            mediaPlayerService.setActivity(MainActivity.this);
            searchViewHelper = new SearchViewHelper(MainActivity.this);
            searchViewHelper.setMediaPlayerService(mediaPlayerService);
        }
        @Override public void onServiceDisconnected(ComponentName arg0) {}
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assignTheme();
        setContentView(R.layout.activity_main);
        setupViewModel();
        setupViews();
        setupTabLayout();
        requestPermissions();
        startMediaPlayerService();
        addTrackToPlaylistViewHelper = new AddTrackToPlaylistViewHelper(this);
    }


    private void requestPermissions(){
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPostNotificationPermission();
        }
    }


    public void showAddTrackToPlaylistView(){
        addTrackToPlaylistViewHelper.showAddTrackToPlaylistView();
    }


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requestPostNotificationPermission(){
        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 3);
    }


    private void assignTheme(){
        themeHelper = new ThemeHelper();
        themeHelper.assignTheme(this);
    }


    public void onStart() {
        super.onStart();
        themeHelper.restartActivityIfDifferentThemeSet(this);
        updateArtistsListInCaseMinTracksSettingHasChanged();
    }


    public MainViewModel getViewModel(){
        return viewModel;
    }


    private void updateArtistsListInCaseMinTracksSettingHasChanged(){
        if (mediaPlayerService != null) {
            mediaPlayerService.updateArtistView();
        }
    }


    public boolean isUserPlaylistLoaded(){
        return mediaPlayerService.getPlaylistManager().isUserPlaylistLoaded();
    }



    public void deselectCurrentTrackAfterDelay(){
        // we need to give the recycler view in tracks fragment time to recreate its layout
        new Handler(Looper.getMainLooper())
                .postDelayed(()->tracksFragment.deselectCurrentItemAndNotify(),
                        300);
    }


    public SearchViewHelper getSearchViewHelper(){
        return searchViewHelper;
    }


    public List<String> getAlbumNames(){
        if(isPlaylistManagerUnavailable()){
            return Collections.emptyList();
        }
        return mediaPlayerService.getPlaylistManager().getAlbumNames();
    }


    public List<String> getArtistNames(){
        if(isPlaylistManagerUnavailable()){
            return Collections.emptyList();
        }
        return mediaPlayerService.getPlaylistManager().getArtistNames();
    }


    private boolean isPlaylistManagerUnavailable(){
        return mediaPlayerService == null || mediaPlayerService.getPlaylistManager() == null;
    }


    public void setPlayerFragment(TracksFragment tracksFragment){
        this.tracksFragment = tracksFragment;
        onQueueFragmentReady();
    }


    private void setupTrackTimeSeekBar(){
        trackTimeSeekBar = findViewById(R.id.trackTimeSeekBar);
        trackTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(isTrackTimeSeekBarHeld) {
                    setElapsedTimeOnView(TimeConverter.convert(seekBar.getProgress()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTrackTimeSeekBarHeld = true;
                setPlayerControlsEnabled(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = Math.min(seekBar.getMax() - 500, seekBar.getProgress());
                mediaPlayerService.seek(progress);
                isTrackTimeSeekBarHeld = false;
                setPlayerControlsEnabled(true);
            }
        });
    }


    private void setPlayerControlsEnabled(boolean isEnabled){
        List<View> controls = Arrays.asList(stopButton, playButton, pauseButton, nextTrackButton, previousTrackButton, turnShuffleOnButton, turnShuffleOffButton);
        for(View control : controls){
            control.setEnabled(isEnabled);
        }
    }


    public void nextTrack(){
        disableViewForAWhile(nextTrackButton);
        mediaPlayerService.loadNextTrack();
    }


    public void previousTrack(){
        disableViewForAWhile(previousTrackButton);
        mediaPlayerService.loadPreviousTrack();
    }


    public void playTrack() {
        mediaPlayerService.playTrack();
    }


    public void pauseTrack() {
        disableViewForAWhile(playButton, 300);
        mediaPlayerService.pause();
    }


    public void stopTrack(){
        mediaPlayerService.stop();
        resetElapsedTime();
    }


    public void initAlbumArt(){
        albumArtHelper = new AlbumArtHelper(this);
    }

    public void setAlbumArt(Bitmap coverArtBitmap){
        albumArtHelper.changeAlbumArtTo(coverArtBitmap);
    }


    public void disableViewForAWhile(View view){
        disableViewForAWhile(view, 700);
    }


    public void disableViewForAWhile(View view, int delayTime) {
        view.setEnabled(false);
        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(()->view.setEnabled(true), delayTime);
    }


    public void selectTrack(int index) {
        mediaPlayerService.selectTrack(index);
    }


    public void addSelectedTrackToQueue(){
        enqueue(selectedTrack);
    }


    public void enqueue(Track track){
        mediaPlayerService.getPlaylistManager().addTrackToQueue(track);
        toast(R.string.toast_track_added_to_queue);
    }


    public void notifyTrackAddedToPlaylist(){
        toast(R.string.toast_track_added_to_playlist);
    }


    public void notifyTrackAlreadyInPlaylist(){
        toast(R.string.toast_track_already_in_playlist);
    }


    public void notifyTrackRemovedFromPlaylist(boolean success){
        toast(success ? R.string.toast_track_removed_from_playlist : R.string.toast_track_removed_from_playlist_fail);
    }


    public void notifyTracksAddedToPlaylist(int numberOfTracks){
        switch(numberOfTracks){
            case 0 : toast(R.string.toast_no_new_tracks_were_added_to_playlist); break;
            case 1 : toast(R.string.toast_one_track_added_to_playlist); break;
            default : toast(getString(R.string.toast_tracks_added_to_playlist, numberOfTracks));
        }
    }


    public void setSelectedTrack(Track track){
        this.selectedTrack = track;
    }


    public void removeSelectedTrackFromPlaylist(){
        mediaPlayerService.removeTrackFromCurrentPlaylist(selectedTrack);
    }


    public List<Track> getTrackList(){
        if(mediaPlayerService == null){
            return Collections.emptyList();
        }
        List<Track> tracks = mediaPlayerService.getTrackList();
        updateViews(tracks, mediaPlayerService.getCurrentTrack());
       return tracks;
    }


    private void startMediaPlayerService(){
        Intent mediaPlayerServiceIntent = new Intent(this, MediaPlayerService.class);
        getApplicationContext().startForegroundService(mediaPlayerServiceIntent);
        getApplicationContext().bindService(mediaPlayerServiceIntent, serviceConnection, 0);
    }


    private void setupViews(){
        setupPlayerButtonPanelViews();
        assignTrackInfoViews();
        setupTrackTimeSeekBar();
        resetElapsedTime();
    }


    public void resetElapsedTime(){
        setElapsedTime("0:00");
    }


    public void setElapsedTime(long elapsedMilliseconds){
        setElapsedTime(TimeConverter.convert(elapsedMilliseconds));
        runOnUiThread(()->{
            if(!isTrackTimeSeekBarHeld){
                trackTimeSeekBar.setProgress((int)elapsedMilliseconds);
            }
        });
    }


    public void setElapsedTime(String elapsedTime){
        if(!isTrackTimeSeekBarHeld){
            setElapsedTimeOnView(elapsedTime);
        }
    }


    private void setElapsedTimeOnView(String elapsedTime){
        runOnUiThread(()->{
            if(trackTime != null){
                String time = elapsedTime + " / " + totalTrackTime;
                trackTime.setText(time);
            }
        });
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        if(tabsViewStateAdapter != null) {
            tabsViewStateAdapter = null;
        }
    }


    public void displayError(Track track){
        runOnUiThread(()->{
            String errorMessage = getString(R.string.error_playing_track_toast_message, track.getPathname());
            toast(errorMessage);
        });
    }


    private void toast(String msg){
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }


    private void toast(int resId){
        toast(getString(resId));
    }


    public void hidePlayerViews(){
        setVisibilityOnPlayerViews(View.INVISIBLE);
    }


    public void showPlayerViews(){
        setVisibilityOnPlayerViews(View.VISIBLE);
    }


    public void setVisibilityOnPlayerViews(int visibility){
        trackTitle.setVisibility(visibility);
        trackAlbum.setVisibility(visibility);
        trackArtist.setVisibility(visibility);
        trackTime.setVisibility(visibility);
        playerButtonPanel.setVisibility(visibility);
    }


    private void setTrackTimeInfo(int elapsedTime, long trackDuration){
        this.totalTrackTime = TimeConverter.convert(trackDuration);
        trackTimeSeekBar.setMax((int)trackDuration);
        setElapsedTime(TimeConverter.convert(elapsedTime));
    }


    private void assignTrackInfoViews(){
        trackTime = findViewById(R.id.trackTime);
        trackTitle = findViewById(R.id.trackTitle);
        trackAlbum = findViewById(R.id.albumTextView);
        trackArtist = findViewById(R.id.artistTextView);
    }


    private void setupPlayerButtonPanelViews(){
        playerButtonPanel = findViewById(R.id.buttonLayout);
        previousTrackButton = setupImageButton(R.id.previousTrackButton, this::previousTrack);
        nextTrackButton     = setupImageButton(R.id.nextTrackButton, this::nextTrack);
        playButton  = setupImageButton(R.id.playButton, this::playTrack);
        pauseButton = setupImageButton(R.id.pauseButton, this::pauseTrack);
        stopButton  = setupImageButton(R.id.stopButton, this:: stopTrack);
        setupStopLongClick();
        turnShuffleOnButton =  setupImageButton(R.id.turnShuffleOnButton,  ()-> mediaPlayerService.enableShuffle());
        turnShuffleOffButton = setupImageButton(R.id.turnShuffleOffButton, ()-> mediaPlayerService.disableShuffle());
    }


    private void setupStopLongClick(){
        stopButton.setOnLongClickListener((View v)->{
            if(mediaPlayerService.isPlaying()){
                createStopOptionsFragment();
            }
            return true;
        });
    }


    private void createStopOptionsFragment(){
        String tag = "stop_options_dialog";
        FragmentTransaction fragmentTransaction = FragmentHelper.createTransaction(this, tag);
        StopOptionsFragment.newInstance().show(fragmentTransaction, tag);
    }


    public void setBlankTrackInfo(){
        runOnUiThread(()-> trackTitle.setText(""));
    }


    public void notifyMediaPlayerStopped(){
        runOnUiThread(()->{
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
            trackTimeSeekBar.setProgress(0);
            trackTimeSeekBar.setVisibility(View.INVISIBLE);
        });
    }


    public void hideTrackSeekBar(){
        trackTimeSeekBar.setVisibility(View.INVISIBLE);
    }


    public void notifyMediaPlayerPaused(){
        runOnUiThread(()->{
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
        });
    }


    public void notifyShuffleEnabled(){
        turnShuffleOnButton.setVisibility(View.GONE);
        turnShuffleOffButton.setVisibility(View.VISIBLE);
    }


    public void notifyShuffleDisabled(){
        turnShuffleOnButton.setVisibility(View.VISIBLE);
        turnShuffleOffButton.setVisibility(View.GONE);
    }


    private void setShuffleButtonsVisibility(){
        if(mediaPlayerService.isShuffleEnabled()){
            notifyShuffleEnabled();
            return;
        }
        notifyShuffleDisabled();
    }


    public void notifyMediaPlayerPlaying(){
        runOnUiThread(()->{
            playButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
            trackTimeSeekBar.setVisibility(View.VISIBLE);
        });
    }


    public void setTrackInfoOnView(final Track track, int elapsedTime){
        runOnUiThread(()-> {
                playerButtonPanel.setVisibility(View.VISIBLE);
                String titleText = track.getTitle();
                trackTitle.setText(titleText.isEmpty()? getString(R.string.no_tracks_found) : titleText);
                trackAlbum.setText(track.getAlbum());
                trackArtist.setText(track.getArtist());
                setTrackTimeInfo(elapsedTime, track.getDuration());
                trackTimeSeekBar.setProgress(elapsedTime);
        });
    }


    public void setSeekAndShuffleButtonsVisibility(int numberOfTracks){
        if(numberOfTracks < 2){
            nextTrackButton.setVisibility(View.INVISIBLE);
            previousTrackButton.setVisibility(View.INVISIBLE);
            turnShuffleOffButton.setVisibility(View.GONE);
            turnShuffleOnButton.setVisibility(View.GONE);
        }
        else{
            nextTrackButton.setVisibility(View.VISIBLE);
            previousTrackButton.setVisibility(View.VISIBLE);
            setShuffleButtonsVisibility();
        }
    }


    public void scrollToAndSelectPosition(int index){
       runOnUiThread(()-> {
           if(tracksFragment != null){
               tracksFragment.scrollToAndSelectListPosition(index);
           }
       });
    }


    public void deselectCurrentTrack(){
        tracksFragment.deselectCurrentItem();
    }


    private void setupViewModel(){
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
    }


    private void setupTabLayout(){
        tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 tabViewPager = findViewById(R.id.pager);
        if(tabLayout == null){
            return;
        }
        tabsViewStateAdapter = new TabsViewStateAdapter(getSupportFragmentManager(), getLifecycle());
        tabViewPager.setAdapter(tabsViewStateAdapter);
        new TabHelper(viewModel).setupTabLayout(tabLayout, tabViewPager);
    }


    public void updatePlaylistList(){
        PlaylistsFragment fragment = (PlaylistsFragment)getSupportFragmentManager().findFragmentByTag("f1");
        if(fragment != null) {
            fragment.onAddNewPlaylist();
        }
    }


    public void updateTracksList(List<Track> updatedTracks, Track currentTrack, int currentTrackIndex){
        runOnUiThread(()-> {
            if(tracksFragment != null) {
                tracksFragment.updateTracksList(updatedTracks, currentTrackIndex);
                updateViews(updatedTracks, currentTrack);
            }
        });
    }


    public void switchToTracksTab(){
        TabLayout.Tab tab = tabLayout.getTabAt(0);
        if(tab != null){
            tab.select();
        }
    }


    public void updateAlbumsList(ArrayList<String> albums){
        sendArrayListToFragment(SEND_ALBUMS_TO_FRAGMENT, BUNDLE_KEY_ALBUM_UPDATES, albums);
    }


    public void updateArtistsList(ArrayList<String> artists){
        sendArrayListToFragment(SEND_ARTISTS_TO_FRAGMENT, BUNDLE_KEY_ARTIST_UPDATES, artists);
    }


    private void sendArrayListToFragment(String requestKey, String itemKey, ArrayList<String> arrayList){
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(itemKey, arrayList);
        runOnUiThread(()-> getSupportFragmentManager().setFragmentResult(requestKey, bundle));
    }


    public void loadTracksFromArtist(String artistName){
        mediaPlayerService.loadTracksFromArtist(artistName);
    }


    public void loadTracksFromAlbum(String albumName){
        mediaPlayerService.loadTracksFromAlbum(albumName);
    }


    public void onQueueFragmentReady(){
        startMediaPlayerService();
    }


    private void updateViews(List<Track> updatedTracks, Track currentTrack){
        if(updatedTracks.isEmpty() && currentTrack == null){
            setVisibilityOnPlayerViews(View.INVISIBLE);
            return;
        }
        setVisibilityOnPlayerViews(View.VISIBLE);
        setSeekAndShuffleButtonsVisibility(updatedTracks.size());
        setPlayPauseAndTrackSeekBarVisibility();
    }


    private void setPlayPauseAndTrackSeekBarVisibility(){
        if(mediaPlayerService.isPlaying()){
            playButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
            trackTimeSeekBar.setVisibility(View.VISIBLE);
            return;
        }
        playButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
        trackTimeSeekBar.setVisibility(View.INVISIBLE);
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
        inflater.inflate(R.menu.menu_buttons, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.refresh_button) {
          mediaPlayerService.refreshTrackDataFromFilesystem();
        }
        else if(id == R.id.search){
           searchViewHelper.toggleSearch();
        }
        else if(id == R.id.options){
            startSettingsActivity();
        }
        return super.onOptionsItemSelected(item);
    }


    private void startSettingsActivity(){
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }


    public MediaPlayerService getMediaPlayerService(){
        return this.mediaPlayerService;
    }


    public void scrollToTrack(Track track){
        PlaylistManager playlistManager = mediaPlayerService.getPlaylistManager();
        if(playlistManager == null || tracksFragment == null){
            return;
        }
       if (playlistManager.isUserPlaylistLoaded()) {
           int index = playlistManager.getCurrentIndexOf(track);
           if (index != -1) {
               tracksFragment.scrollToAndSelectListPosition(index);
           } else {
               deselectCurrentTrackAfterDelay();
           }
       } else {
           tracksFragment.scrollToAndSelectListPosition(track.getIndex());
       }
    }

    public void addTrackToPlaylist(Playlist playlist){
        mediaPlayerService.addTrackToPlaylist(selectedTrack, playlist);
    }


    public void loadPlaylist(Playlist playlist, boolean shouldSwitchToTracksTab){
        mediaPlayerService.loadPlaylist(playlist);
        if(shouldSwitchToTracksTab){
            switchToTracksTab();
        }
    }


    private ImageButton setupImageButton(int buttonId, Runnable runnable){
        ImageButton button = findViewById(buttonId);
        button.setOnClickListener((View v)-> runnable.run());
        return button;
    }
}
