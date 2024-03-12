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

import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.jacstuff.musicplayer.service.db.playlist.Playlist;
import com.jacstuff.musicplayer.service.db.track.Track;
import com.jacstuff.musicplayer.view.fragments.MessageKey;
import com.jacstuff.musicplayer.view.fragments.Message;
import com.jacstuff.musicplayer.view.fragments.album.AlbumsFragment;
import com.jacstuff.musicplayer.view.fragments.artist.ArtistsFragment;
import com.jacstuff.musicplayer.view.fragments.tracks.TracksFragment;
import com.jacstuff.musicplayer.view.fragments.playlist.PlaylistsFragment;
import com.jacstuff.musicplayer.view.player.PlayerViewHelper;
import com.jacstuff.musicplayer.view.playlist.AddTrackToPlaylistViewHelper;
import com.jacstuff.musicplayer.view.search.SearchViewHelper;
import com.jacstuff.musicplayer.view.tab.TabsViewStateAdapter;
import com.jacstuff.musicplayer.service.MediaPlayerService;
import com.jacstuff.musicplayer.view.utils.ThemeHelper;
import com.jacstuff.musicplayer.view.art.AlbumArtHelper;
import com.jacstuff.musicplayer.view.tab.TabHelper;
import com.jacstuff.musicplayer.view.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final  String BUNDLE_KEY_ALBUM_UPDATES = "bundle_key_album_updates";
    public static final  String BUNDLE_KEY_ARTIST_UPDATES = "bundle_key_artist_updates";
    public static final String SEND_ALBUMS_TO_FRAGMENT = "send_albums_to_fragment";
    public static final String SEND_ARTISTS_TO_FRAGMENT = "send_artists_to_fragment";
    private TabsViewStateAdapter tabsViewStateAdapter;
    private MediaPlayerService mediaPlayerService;
    private TracksFragment tracksFragment;
    private TabLayout tabLayout;
    private Track selectedTrack;
    private SearchViewHelper searchViewHelper;
    private MainViewModel viewModel;
    private ThemeHelper themeHelper;
    private AlbumArtHelper albumArtHelper;
    private AddTrackToPlaylistViewHelper addTrackToPlaylistViewHelper;
    private PlayerViewHelper playerViewHelper;


    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mediaPlayerService = binder.getService();
            playerViewHelper.setMediaPlayerService(mediaPlayerService);
            albumArtHelper = new AlbumArtHelper(MainActivity.this);
            mediaPlayerService.setActivity(MainActivity.this);
            addTrackToPlaylistViewHelper = new AddTrackToPlaylistViewHelper(MainActivity.this);
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
        setupTabLayout();
        requestPermissions();
        initPlayerViewHelper();
        startMediaPlayerService();
    }


    private void initPlayerViewHelper(){
        if(playerViewHelper == null){
            playerViewHelper = new PlayerViewHelper(this);
        }
        playerViewHelper.setupViews();
    }


    private void requestPermissions(){
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPostNotificationPermission();
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_AUDIO}, 3);
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
        return !isPlaylistManagerUnavailable() && mediaPlayerService.getPlaylistManager().isUserPlaylistLoaded();
    }


    public SearchViewHelper getSearchViewHelper(){
        return searchViewHelper;
    }

    public AddTrackToPlaylistViewHelper getAddTrackToPlaylistViewHelper(){ return addTrackToPlaylistViewHelper; }


    public List<String> getAlbumNames(){
        return isPlaylistManagerUnavailable() ? Collections.emptyList() : mediaPlayerService.getPlaylistManager().getAlbumNames();
    }


    public List<String> getGenreNames(){
        return isPlaylistManagerUnavailable() ? Collections.emptyList() : mediaPlayerService.getPlaylistManager().getGenreNames();
    }


    public List<String> getArtistNames(){
        return isPlaylistManagerUnavailable() ? Collections.emptyList() : mediaPlayerService.getPlaylistManager().getArtistNames();
    }


    public List<Playlist> getAllUserPlaylists(){
        return isPlaylistManagerUnavailable() ? Collections.emptyList() : mediaPlayerService.getPlaylistManager().getAllUserPlaylists();
    }


    private boolean isPlaylistManagerUnavailable(){
        return mediaPlayerService == null || mediaPlayerService.getPlaylistManager() == null;
    }


    public void setPlayerFragment(TracksFragment tracksFragment){
        this.tracksFragment = tracksFragment;
        onQueueFragmentReady();
    }


    public void stopTrack(){
        mediaPlayerService.stop();
        resetElapsedTime();
    }


    public void hidePlayerViews(){ playerViewHelper.setVisibilityOnPlayerViews(View.INVISIBLE);}


    public void showPlayerViews(){
        playerViewHelper.setVisibilityOnPlayerViews(View.VISIBLE);
    }


    public void setBlankTrackInfo(){ playerViewHelper.setBlankTrackInfo();}


    public void notifyMediaPlayerStopped(){ playerViewHelper.notifyMediaPlayerStopped();}


    public void hideTrackSeekBar(){ playerViewHelper.hideTrackSeekBar();}


    public void notifyMediaPlayerPaused(){ playerViewHelper.notifyMediaPlayerPaused();}


    public void notifyShuffleEnabled(){ playerViewHelper.notifyShuffleEnabled(); }


    public void notifyShuffleDisabled(){ playerViewHelper.notifyShuffleDisabled(); }


    public void notifyMediaPlayerPlaying(){playerViewHelper.notifyMediaPlayerPlaying(); }


    public void setTrackDetails(final Track track, int elapsedTime){ playerViewHelper.setTrackDetails(track, elapsedTime); }


    public void setAlbumArt(Bitmap coverArtBitmap){ albumArtHelper.changeAlbumArtTo(coverArtBitmap);}


    public void setBlankAlbumArt(){ albumArtHelper.changeAlbumArtToBlank();}


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


    public void loadAlbumOfSelectedTrack(){
        mediaPlayerService.loadAlbumOTrack(selectedTrack);
    }


    public List<Track> getTrackList(){
        if(mediaPlayerService == null){
            return Collections.emptyList();
        }
        List<Track> tracks = mediaPlayerService.getTrackList();
        updateViews(tracks, mediaPlayerService.getCurrentTrack());
        return tracks;
    }


    public Playlist getCurrentPlaylist(){
        return mediaPlayerService == null ? new Playlist(-50L, "Empty Playlist", false):
                mediaPlayerService.getPlaylistManager().getCurrentPlaylist();
    }


    private void startMediaPlayerService(){
        Intent mediaPlayerServiceIntent = new Intent(this, MediaPlayerService.class);
        getApplicationContext().startForegroundService(mediaPlayerServiceIntent);
        getApplicationContext().bindService(mediaPlayerServiceIntent, serviceConnection, 0);
    }


    public void resetElapsedTime(){
        playerViewHelper.resetElapsedTime();
    }


    public void setElapsedTime(long elapsedMilliseconds){
        playerViewHelper.setElapsedTime(elapsedMilliseconds);
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        if(tabsViewStateAdapter != null) {
            tabsViewStateAdapter = null;
        }
    }


    public void displayError(Track track){
        String errorMessage = getString(R.string.error_playing_track_toast_message, track.getPathname());
        toast(errorMessage);
    }


    public void toastFileDoesNotExistError(Track track){
        String errorMessage = getString(R.string.error_loading_track_toast_message, track.getPathname());
        toast(errorMessage);
    }


    private void toast(String msg){
        runOnUiThread(()-> Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show());
    }


    private void toast(int resId, String arg){
        runOnUiThread(()-> Toast.makeText(MainActivity.this, getString(resId, arg), Toast.LENGTH_SHORT).show());
    }


    private void toast(int resId){
        toast(getString(resId));
    }

    private void log(String msg){
        System.out.println("^^^ MainActivity: " + msg);
    }


    public void deselectCurrentTrack(){
        log("Entered deselectCurrentTrack");
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


    public void updateTracksList(Playlist playlist, Track currentTrack, int currentTrackIndex){
        runOnUiThread(()-> {
            if(tracksFragment != null && tracksFragment.getContext() != null) {
                tracksFragment.updateTracksList(playlist, currentTrackIndex);
                updateViews(playlist.getTracks(), currentTrack);
            }
        });
    }


    public void notifyAlbumNotLoaded(String albumName){
        toast(R.string.unable_to_load_album_toast_message, albumName);
    }


    public void deselectItemsInPlaylistAndArtistTabs(){
        sendFragmentMessage(PlaylistsFragment.NOTIFY_TO_DESELECT_ITEMS);
        sendFragmentMessage(ArtistsFragment.NOTIFY_TO_DESELECT_ITEMS);
        sendFragmentMessage(Message.NOTIFY_TO_DESELECT_GENRE_ITEMS);
    }


    public void deselectItemsInTabsOtherThanGenre(){
        sendFragmentMessage(PlaylistsFragment.NOTIFY_TO_DESELECT_ITEMS);
        sendFragmentMessage(ArtistsFragment.NOTIFY_TO_DESELECT_ITEMS);
        sendFragmentMessage(AlbumsFragment.NOTIFY_TO_DESELECT_ITEMS);
    }


    private void sendFragmentMessage(String messageKey){
        getSupportFragmentManager().setFragmentResult(messageKey, new Bundle());
    }


    private void sendFragmentMessage(Message message){
        getSupportFragmentManager().setFragmentResult(message.toString(), new Bundle());
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


    public void updateGenresList(ArrayList<String> genres){
        sendArrayListToFragment(Message.SEND_GENRES_TO_FRAGMENT, MessageKey.GENRE_UPDATES, genres);
    }


    private void sendArrayListToFragment(String requestKey, String itemKey, ArrayList<String> arrayList){
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(itemKey, arrayList);
        runOnUiThread(()-> getSupportFragmentManager().setFragmentResult(requestKey, bundle));
    }


    private void sendArrayListToFragment(Message requestKey, MessageKey itemKey, ArrayList<String> arrayList){
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(itemKey.toString(), arrayList);
        runOnUiThread(()-> getSupportFragmentManager().setFragmentResult(requestKey.toString(), bundle));
    }


    public void loadTracksFromArtist(String artistName){
        mediaPlayerService.loadTracksFromArtist(artistName);
    }


    public void loadTracksFromAlbum(String albumName){
        mediaPlayerService.loadTracksFromAlbum(albumName);
    }


    public void loadTracksFromGenre(String genreName){
        mediaPlayerService.loadTracksFromGenre(genreName);
    }


    public void onQueueFragmentReady(){
        startMediaPlayerService();
    }


    private void updateViews(List<Track> updatedTracks, Track currentTrack){
        playerViewHelper.updateViews(updatedTracks.size(), currentTrack == null);
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


    public void scrollToAndSelectPosition(int index, boolean isSearchResult){
        runOnUiThread(()-> {
            if(tracksFragment != null){
                tracksFragment.scrollToAndSelectListPosition(index, isSearchResult);
            }
        });
    }


    public void ensureSelectedTrackIsVisible(){
        runOnUiThread(()-> {
            if(tracksFragment != null){
                if(mediaPlayerService == null){
                    return;
                }
                int currentTrackIndex = mediaPlayerService.getCurrentTrackIndex();
                tracksFragment.ensureSelectedTrackIsVisible(currentTrackIndex);
            }
        });
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
}
