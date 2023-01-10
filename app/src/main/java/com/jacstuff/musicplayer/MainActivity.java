package com.jacstuff.musicplayer;

import static com.jacstuff.musicplayer.search.AnimatorHelper.createShowAnimatorFor;

import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.jacstuff.musicplayer.db.album.Album;
import com.jacstuff.musicplayer.db.artist.Artist;
import com.jacstuff.musicplayer.db.track.Track;
import com.jacstuff.musicplayer.fragments.playlist.PlayerFragment;
import com.jacstuff.musicplayer.fragments.PlaylistsFragment;
import com.jacstuff.musicplayer.fragments.ViewStateAdapter;
import com.jacstuff.musicplayer.list.SearchResultsListAdapter;
import com.jacstuff.musicplayer.search.AnimatorHelper;
import com.jacstuff.musicplayer.search.KeyListenerHelper;
import com.jacstuff.musicplayer.service.MediaPlayerService;
import com.jacstuff.musicplayer.view.tab.TabHelper;
import com.jacstuff.musicplayer.viewmodel.MainViewModel;

import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private ViewStateAdapter viewStateAdapter;
    private MediaPlayerService mediaPlayerService;
    private TextView trackTime;
    private TextView trackTitle, trackAlbum, trackArtist;
    private ImageButton playButton, pauseButton, stopButton;
    private EditText searchEditText;
    private ImageButton nextTrackButton, previousTrackButton;
    private ImageButton turnShuffleOnButton, turnShuffleOffButton;
    private String totalTrackTime = "0:00";
    private PlayerFragment playerFragment;
    private ViewGroup playerButtonPanel;
    private RecyclerView searchResultsRecyclerView;
    private SearchResultsListAdapter searchResultsListAdapter;
    private Button addSelectedSearchResultButton, addAllSearchResultsButton;

    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mediaPlayerService = binder.getService();
            mediaPlayerService.setActivity(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {}
    };


    public void onServiceReady(List<Track> tracks){
        log("Entered onServiceReady(), about to initPlaylist and refresh on mediaPlayerService");
        //getPlayerFragment().onServiceReady(tracks);
        mediaPlayerService.initPlaylist();
    }

    private View searchView;

    private void toggleSearch(){
        if(searchView.getVisibility() == View.VISIBLE){
            hideSearch();
            return;
        }
        showSearch();
    }


    private void showSearch(){
        Animator animator = createShowAnimatorFor(searchView, ()->{
            searchEditText.requestFocus();
            searchEditText.postDelayed(this::showKeyboard, 200);
        });
        searchView.setVisibility(View.VISIBLE);
        animator.start();

    }


    private void hideSearch(){
        Animator animator = AnimatorHelper.createHideAnimatorFor(searchView, ()->{
                searchView.setVisibility(View.GONE);
                searchEditText.setText("");
                clearSearchResults();
        });
        hideKeyboard();
        animator.start();
    }


    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
    }


    private void showKeyboard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchEditText, InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViews();
        setupTabLayout();
        setupViewModel();
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        startMediaPlayerService();
        setupSearchView();
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
        if(mediaPlayerService == null){
            return Collections.emptyList();
        }
        List<Track> tracks = mediaPlayerService.getTrackList();
        updateViews(tracks);
       return tracks;
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
        turnShuffleOnButton = findViewById(R.id.turnShuffleOnButton);
        turnShuffleOffButton = findViewById(R.id.turnShuffleOffButton);
        searchView = findViewById(R.id.searchView);
        searchEditText = findViewById(R.id.trackSearchEditText);
    }


    private void assignClickListeners(){
        playButton.setOnClickListener((View v) -> playTrack());
        pauseButton.setOnClickListener((View v) -> pauseMediaPlayer());
        nextTrackButton.setOnClickListener((View v) -> nextTrack());
        previousTrackButton.setOnClickListener((View v) -> previousTrack());
        stopButton.setOnClickListener((View v) -> stopTrack());
        turnShuffleOnButton.setOnClickListener((View v) -> mediaPlayerService.enableShuffle());
        turnShuffleOffButton.setOnClickListener((View v) -> mediaPlayerService.disableShuffle());
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


    public void notifyShuffleEnabled(){
        turnShuffleOnButton.setVisibility(View.GONE);
        turnShuffleOffButton.setVisibility(View.VISIBLE);
    }


    public void notifyShuffleDisabled(){
        turnShuffleOnButton.setVisibility(View.VISIBLE);
        turnShuffleOffButton.setVisibility(View.GONE);
    }



    public void notifyMediaPlayerPlaying(){
        runOnUiThread(()->{
            playButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        });
    }


    public void setTrackInfoOnView(final Track track, int elapsedTime){
        runOnUiThread(()-> {
                setTrackInfo(track.getTitle());
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
        if(numberOfTracks < 2){
            nextTrackButton.setVisibility(View.INVISIBLE);
            previousTrackButton.setVisibility(View.INVISIBLE);
        }
        else{
            nextTrackButton.setVisibility(View.VISIBLE);
            previousTrackButton.setVisibility(View.VISIBLE);
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
        final ViewPager2 pager = findViewById(R.id.pager);
        if(tabLayout == null){
            return;
        }
        viewStateAdapter = new ViewStateAdapter(getSupportFragmentManager(), getLifecycle());
        pager.setAdapter(viewStateAdapter);
        new TabHelper().setupTabLayout(tabLayout, pager);
    }


    public void updatePlaylistList(){
        PlaylistsFragment fragment = (PlaylistsFragment)getSupportFragmentManager().findFragmentByTag("f1");
        if(fragment != null) {
            fragment.onAddNewPlaylist();
        }
    }


    public void updateTracksList(List<Track> updatedTracks, int currentTrackIndex){
        runOnUiThread(()-> {
            if(playerFragment!= null){
                playerFragment.updateTracksList(updatedTracks, currentTrackIndex);
            }
            updateViews(updatedTracks);
        });
    }


    public void loadTracksFromArtist(Artist artist){
        mediaPlayerService.loadTracksFromArtist(artist);
    }


    public void loadTracksFromAlbum(Album album){
        mediaPlayerService.loadTracksFromAlbum(album);
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


    public List<Track> getTracksForSearch(String str){
      return mediaPlayerService.getTracksForSearch(str);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.refresh_button) {
          mediaPlayerService.scanForTracks();
        }
        else if(id == R.id.test_stop_after_current){
            toggleSearch();
            //mediaPlayerService.enableStopAfterTrackFinishes();
        }
        return super.onOptionsItemSelected(item);
    }


    public void setupSearchView() {
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        setupRecyclerView(Collections.emptyList());
        setupSearchKeyListener();
        setupSearchViewButtons();
    }


    private void setupRecyclerView(List<Track> tracks){
        if(tracks == null){
            return;
        }
        searchResultsListAdapter = new SearchResultsListAdapter(tracks);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        searchResultsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        searchResultsRecyclerView.setAdapter(searchResultsListAdapter);
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


    private void setupSearchViewButtons(){
        addSelectedSearchResultButton = setupButton(R.id.addSelectedButton, this::addSelectedSearchResultToPlaylist);
        addAllSearchResultsButton = setupButton(R.id.addAllButton, this::addAllSearchResultsToPlaylist);
        setupButton(R.id.playSelectedButton, this::playSelectedSearchResult);
        setupButton(R.id.cancelButton, this::toggleSearch);
    }



    public void hideSearchAddButtons(){
        addSelectedSearchResultButton.setVisibility(View.GONE);
        addAllSearchResultsButton.setVisibility(View.GONE);
    }


    public void showSearchAddButtons(){
        addSelectedSearchResultButton.setVisibility(View.VISIBLE);
        addAllSearchResultsButton.setVisibility(View.VISIBLE);
    }


    private void addSelectedSearchResultToPlaylist(){

    }


    private void addAllSearchResultsToPlaylist(){

    }


    private void playSelectedSearchResult(){
        Track track = searchResultsListAdapter.getSelectedTrack();
        if(track != null){
            mediaPlayerService.selectAndPlayTrack(track);
        }
    }


    private Button setupButton(int buttonId, Runnable runnable){
        Button button = findViewById(buttonId);
        button.setOnClickListener((View v)-> runnable.run());
        return button;
    }




}
