package com.jacstuff.musicplayer;

import static com.jacstuff.musicplayer.search.AnimatorHelper.createShowAnimatorFor;

import android.Manifest;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.jacstuff.musicplayer.db.playlist.Playlist;
import com.jacstuff.musicplayer.db.playlist.PlaylistRepository;
import com.jacstuff.musicplayer.db.playlist.PlaylistRepositoryImpl;
import com.jacstuff.musicplayer.db.track.Track;
import com.jacstuff.musicplayer.fragments.options.StopOptionsFragment;
import com.jacstuff.musicplayer.fragments.playlist.PlaylistRecyclerAdapter;
import com.jacstuff.musicplayer.fragments.tracks.TracksFragment;
import com.jacstuff.musicplayer.fragments.playlist.PlaylistsFragment;
import com.jacstuff.musicplayer.fragments.TabsViewStateAdapter;
import com.jacstuff.musicplayer.list.SearchResultsListAdapter;
import com.jacstuff.musicplayer.search.AnimatorHelper;
import com.jacstuff.musicplayer.search.KeyListenerHelper;
import com.jacstuff.musicplayer.service.MediaPlayerService;
import com.jacstuff.musicplayer.theme.ThemeHelper;
import com.jacstuff.musicplayer.utils.KeyboardHelper;
import com.jacstuff.musicplayer.view.tab.TabHelper;
import com.jacstuff.musicplayer.viewmodel.MainViewModel;

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
    private Button addSearchResultButton, enqueueSearchResultButton, playSearchResultButton;
    private ImageView albumArtImageView;
    private SeekBar trackTimeSeekBar;
    private boolean isTrackTimeSeekBarHeld = false;
    private EditText searchEditText;
    private String totalTrackTime = "0:00";
    private TracksFragment tracksFragment;
    private ViewGroup playerButtonPanel;
    private RecyclerView searchResultsRecyclerView;
    private SearchResultsListAdapter searchResultsListAdapter;
    private TabLayout tabLayout;
    private View searchView, addTrackToPlaylistView;
    private OnBackPressedCallback dismissSearchViewOnBackPressedCallback;
    private OnBackPressedCallback dismissAddTrackToPlaylistViewOnBackPressedCallback;
    private Track selectedTrack;
    private Track selectedSearchResultTrack;
    private KeyboardHelper keyboardHelper;
    private MainViewModel viewModel;
    private ThemeHelper themeHelper;
    private boolean hasSearchResultBeenPlayed = false;

    private PlaylistRecyclerAdapter playlistRecyclerAdapter;
    private RecyclerView addTrackToPlaylistRecyclerView;
    private PlaylistRepository playlistRepository;


    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mediaPlayerService = binder.getService();
            mediaPlayerService.setActivity(MainActivity.this);
        }
        @Override public void onServiceDisconnected(ComponentName arg0) {}
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeHelper = new ThemeHelper();
        themeHelper.assignTheme(this);
        setContentView(R.layout.activity_main);
        setupViewModel();
        keyboardHelper = new KeyboardHelper(this);
        setupViews();
        setupTabLayout();
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        startMediaPlayerService();
        setupSearchView();
        setupAddTrackToPlaylistView();
        setupDismissSearchOnBackPressed();
        setupDismissAddTrackToPlaylistViewOnBackPressed();
    }


    public void onStart() {
        super.onStart();
        themeHelper.restartActivityIfDifferentThemeSet(this);
        updateArtistsListInCaseMinTracksSettingHasChanged();
    }


    private void updateArtistsListInCaseMinTracksSettingHasChanged(){
        if (mediaPlayerService != null) {
            mediaPlayerService.updateArtistView();
        }
    }


    private void toggleSearch(){
        if(searchView.getVisibility() == View.VISIBLE){
            hideSearch();
            return;
        }
        showSearch();
    }


    private void setupDismissSearchOnBackPressed(){
        dismissSearchViewOnBackPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                hideSearch();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, dismissSearchViewOnBackPressedCallback);
    }


    private void setupDismissAddTrackToPlaylistViewOnBackPressed(){
        dismissAddTrackToPlaylistViewOnBackPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                hideAddTrackToPlaylistView();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, dismissAddTrackToPlaylistViewOnBackPressedCallback);
    }


    public boolean isUserPlaylistLoaded(){
        return mediaPlayerService.getPlaylistManager().isUserPlaylistLoaded();
    }


    private void showSearch(){
        hideAllSearchResultsButtons();
        Animator animator = createShowAnimatorFor(searchView, ()-> keyboardHelper.showKeyboardAndFocusOn(searchEditText));
        searchView.setVisibility(View.VISIBLE);
        dismissSearchViewOnBackPressedCallback.setEnabled(true);
        animator.start();
    }


    public void showAddTrackToPlaylistView(){
        hideAllSearchResultsButtons();
        Animator animator = createShowAnimatorFor(addTrackToPlaylistView, ()-> {});
        addTrackToPlaylistView.setVisibility(View.VISIBLE);
        dismissAddTrackToPlaylistViewOnBackPressedCallback.setEnabled(true);
        animator.start();
    }

    private void hideAddTrackToPlaylistView(){
        if(addTrackToPlaylistView.getVisibility() != View.VISIBLE){
            return;
        }
        Animator animator = AnimatorHelper.createHideAnimatorFor(addTrackToPlaylistView, ()->
            addTrackToPlaylistView.setVisibility(View.GONE));
        dismissSearchViewOnBackPressedCallback.setEnabled(false);
        animator.start();
    }


    private void hideSearch(){
        if(searchView.getVisibility() != View.VISIBLE){
            return;
        }
        Animator animator = AnimatorHelper.createHideAnimatorFor(searchView, ()->{
            searchView.setVisibility(View.GONE);
            searchEditText.setText("");
            clearSearchResults();
            scrollToPositionIfSearchResultHasBeenPlayed();
        });
        keyboardHelper.hideKeyboard(searchView);
        dismissSearchViewOnBackPressedCallback.setEnabled(false);
        animator.start();
    }


    private void scrollToPositionIfSearchResultHasBeenPlayed(){
        if(hasSearchResultBeenPlayed && tracksFragment != null){
            if (mediaPlayerService.getPlaylistManager().isUserPlaylistLoaded()) {
                deselectCurrentTrackAfterDelay();
            }
            else{
                tracksFragment.scrollToListPosition(selectedSearchResultTrack.getIndex());
            }
        }
        hasSearchResultBeenPlayed = false;
    }


    private void deselectCurrentTrackAfterDelay(){
        // we need to give the recycler view in tracks fragment time to recreate its layout
        new Handler(Looper.getMainLooper())
                .postDelayed(()->tracksFragment.deselectCurrentItemAndNotify(),
                        300);
    }


    public List<String> getAlbumNames(){
        if(mediaPlayerService == null || mediaPlayerService.getPlaylistManager() == null){
            return Collections.emptyList();
        }
        return mediaPlayerService.getPlaylistManager().getAlbumNames();
    }


    public List<String> getArtistNames(){
        if(mediaPlayerService == null || mediaPlayerService.getPlaylistManager() == null){
            return Collections.emptyList();
        }
        return mediaPlayerService.getPlaylistManager().getArtistNames();
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


    public void setAlbumArt(Bitmap coverArtBitmap){
        runOnUiThread(()-> {
            if (coverArtBitmap != null) {
                albumArtImageView.setImageDrawable(new BitmapDrawable(getApplicationContext().getResources(), coverArtBitmap));
                return;
            }
            albumArtImageView.setImageResource(R.drawable.album_art_empty);
        });
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


    private void enqueue(Track track){
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
        updateViews(tracks);
       return tracks;
    }


    private void startMediaPlayerService(){
        Intent mediaPlayerServiceIntent = new Intent(this, MediaPlayerService.class);
        getApplicationContext().startForegroundService(mediaPlayerServiceIntent);
        getApplicationContext().bindService(mediaPlayerServiceIntent, serviceConnection, 0);
    }


    private void setupViews(){
        setupPlayerButtonPanelViews();
        assignSearchViews();
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


    private void assignSearchViews(){
        searchView = findViewById(R.id.searchView);
        searchEditText = findViewById(R.id.trackSearchEditText);
    }


    private void assignTrackInfoViews(){
        trackTime = findViewById(R.id.trackTime);
        trackTitle = findViewById(R.id.trackTitle);
        trackAlbum = findViewById(R.id.albumTextView);
        trackArtist = findViewById(R.id.artistTextView);
        albumArtImageView = findViewById(R.id.albumArtImageView);
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
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        removePreviousFragmentTransaction(tag, fragmentTransaction);
        StopOptionsFragment stopOptionsFragment = StopOptionsFragment.newInstance();
        stopOptionsFragment.show(fragmentTransaction, tag);
    }


    private void removePreviousFragmentTransaction(String tag, FragmentTransaction fragmentTransaction){
        Fragment prev = getSupportFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
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
                trackTimeSeekBar.setProgress((int)elapsedTime);
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


    public void scrollToPosition(int index){
       runOnUiThread(()-> {
           if(tracksFragment != null){
               tracksFragment.scrollToListPosition(index);
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


    public void updateTracksList(List<Track> updatedTracks, int currentTrackIndex){
        runOnUiThread(()-> {
            if(tracksFragment != null) {
                tracksFragment.updateTracksList(updatedTracks, currentTrackIndex);
                updateViews(updatedTracks);
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
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(BUNDLE_KEY_ALBUM_UPDATES, albums);
        runOnUiThread(()-> getSupportFragmentManager().setFragmentResult(SEND_ALBUMS_TO_FRAGMENT, bundle));
    }


    public void updateArtistsList(ArrayList<String> artists){
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(BUNDLE_KEY_ARTIST_UPDATES, artists);
        runOnUiThread(()-> getSupportFragmentManager().setFragmentResult(SEND_ARTISTS_TO_FRAGMENT, bundle));
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


    private void updateViews(List<Track> updatedTracks){
        if(updatedTracks.isEmpty()){
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
            toggleSearch();
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


    public void setupSearchView() {
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        setupSearchRecyclerView(Collections.emptyList());
        setupSearchKeyListener();
        setupSearchViewButtons();
    }


    public void setupAddTrackToPlaylistView() {
        addTrackToPlaylistView = findViewById(R.id.addTrackToPlaylistView);
        playlistRepository = new PlaylistRepositoryImpl(MainActivity.this);
        addTrackToPlaylistRecyclerView = findViewById(R.id.addTrackToPlaylistRecyclerView);
        playlistRecyclerAdapter = new PlaylistRecyclerAdapter(playlistRepository.getAllPlaylists(), this::addTrackToPlaylist);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        addTrackToPlaylistRecyclerView.setLayoutManager(layoutManager);
        addTrackToPlaylistRecyclerView.setItemAnimator(new DefaultItemAnimator());
        addTrackToPlaylistRecyclerView.setAdapter(playlistRecyclerAdapter);
    }


    private void addTrackToPlaylist(Playlist playlist){
        mediaPlayerService.addTrackToPlaylist(selectedTrack, playlist);
    }


    private void setupSearchRecyclerView(List<Track> tracks){
        if(tracks == null){
            return;
        }
        searchResultsListAdapter = new SearchResultsListAdapter(tracks, this::onSearchResultSelect);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        searchResultsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        searchResultsRecyclerView.setAdapter(searchResultsListAdapter);
    }


    private void onSearchResultSelect(Track track){
        selectedSearchResultTrack = track;
        showSearchResultsButtons();
    }


    @SuppressLint("NotifyDataSetChanged")
    private void setSearchResults(List<Track> tracks){
        searchResultsListAdapter.setTracks(tracks);
        searchResultsListAdapter.notifyDataSetChanged();
    }


    private void clearSearchResults(){
        setSearchResults(Collections.emptyList());
    }


    private void setupSearchKeyListener(){
        KeyListenerHelper.setListener(searchEditText, () ->{
            List<Track> tracks = getTracksForSearch(searchEditText.getText().toString());
            setSearchResults(tracks);
        });
    }


    private List<Track> getTracksForSearch(String str){
        if(mediaPlayerService == null){
            return Collections.emptyList();
        }
        return mediaPlayerService.getTracksForSearch(str);
    }


    private void setupSearchViewButtons(){
        addSearchResultButton       = setupButton(R.id.addSelectedButton, this::addSelectedSearchResultToPlaylist);
        playSearchResultButton      = setupButton(R.id.playSelectedButton, this::playSelectedSearchResult);
        enqueueSearchResultButton   = setupButton(R.id.playNextButton, this::addSearchResultToQueue);
    }


    public void hideAllSearchResultsButtons(){
        addSearchResultButton.setVisibility(View.GONE);
        playSearchResultButton.setVisibility(View.GONE);
        enqueueSearchResultButton.setVisibility(View.GONE);
    }


    public void showSearchResultsButtons(){
        playSearchResultButton.setVisibility(View.VISIBLE);
        enqueueSearchResultButton.setVisibility(View.VISIBLE);
        if(isUserPlaylistLoaded()){
            addSearchResultButton.setVisibility(View.VISIBLE);
        }
    }


    private void addSelectedSearchResultToPlaylist(){
       if(selectedSearchResultTrack != null){
           mediaPlayerService.addTrackToCurrentPlaylist(selectedSearchResultTrack);
       }
    }


    public void loadPlaylist(Playlist playlist, boolean shouldSwitchToTracksTab){
        mediaPlayerService.loadPlaylist(playlist);
        if(shouldSwitchToTracksTab){
            switchToTracksTab();
        }
    }


    private void playSelectedSearchResult(){
        disableViewForAWhile(playSearchResultButton);
        if(selectedSearchResultTrack != null) {
            mediaPlayerService.selectAndPlayTrack(selectedSearchResultTrack);
            hasSearchResultBeenPlayed = true;
        }
    }


    private void addSearchResultToQueue(){
        if(selectedSearchResultTrack != null){
            enqueue(selectedSearchResultTrack);
        }
    }


    private Button setupButton(int buttonId, Runnable runnable){
        Button button = findViewById(buttonId);
        button.setOnClickListener((View v)-> runnable.run());
        return button;
    }


    private ImageButton setupImageButton(int buttonId, Runnable runnable){
        ImageButton button = findViewById(buttonId);
        button.setOnClickListener((View v)-> runnable.run());
        return button;
    }
}
