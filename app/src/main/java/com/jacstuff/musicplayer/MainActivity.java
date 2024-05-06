package com.jacstuff.musicplayer;

import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_AUDIO;
import static com.jacstuff.musicplayer.view.fragments.Message.*;
import static com.jacstuff.musicplayer.view.fragments.MessageKey.ALBUM_ARTIST;
import static com.jacstuff.musicplayer.view.fragments.MessageKey.ALBUM_UPDATES;
import static com.jacstuff.musicplayer.view.fragments.about.Utils.putBoolean;
import static com.jacstuff.musicplayer.view.fragments.about.Utils.putInt;
import static com.jacstuff.musicplayer.view.fragments.about.Utils.sendFragmentMessage;
import static com.jacstuff.musicplayer.view.utils.FragmentHelper.sendArrayListToFragment;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.jacstuff.musicplayer.service.db.entities.Playlist;
import com.jacstuff.musicplayer.service.db.entities.Track;
import com.jacstuff.musicplayer.service.helpers.PreferencesHelper;
import com.jacstuff.musicplayer.service.playlist.PlaylistManager;
import com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper;
import com.jacstuff.musicplayer.view.fragments.MessageKey;
import com.jacstuff.musicplayer.view.fragments.Message;
import com.jacstuff.musicplayer.view.fragments.about.AboutDialogFragment;
import com.jacstuff.musicplayer.view.fragments.options.AddTrackToPlaylistFragment;
import com.jacstuff.musicplayer.view.fragments.options.TrackOptionsDialog;
import com.jacstuff.musicplayer.view.utils.PlayerViewHelper;
import com.jacstuff.musicplayer.view.utils.AddTrackToPlaylistViewHelper;
import com.jacstuff.musicplayer.view.search.SearchViewHelper;
import com.jacstuff.musicplayer.service.MediaPlayerService;
import com.jacstuff.musicplayer.view.utils.ThemeHelper;
import com.jacstuff.musicplayer.view.art.AlbumArtHelper;
import com.jacstuff.musicplayer.view.tab.TabHelper;
import com.jacstuff.musicplayer.view.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class MainActivity extends AppCompatActivity {

    private MediaPlayerService mediaPlayerService;
    private Track selectedTrack;
    private SearchViewHelper searchViewHelper;
    private MainViewModel viewModel;
    private ThemeHelper themeHelper;
    private AlbumArtHelper albumArtHelper;
    private AddTrackToPlaylistViewHelper addTrackToPlaylistViewHelper;
    private PlayerViewHelper playerViewHelper;
    private TabHelper tabHelper;
    private final AtomicBoolean isServiceConnected = new AtomicBoolean(false);
    private PreferencesHelper preferencesHelper;


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
            setupOptionsMenuForCurrentTrack();
            setupFunctionButtons();
            sendMessage(NOTIFY_PLAYLIST_TAB_TO_RELOAD);
            isServiceConnected.set(true);
        }

        @Override public void onServiceDisconnected(ComponentName arg0) {
            isServiceConnected.set(false);
        }
    };


    private final ActivityResultLauncher<String> requestAudioPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    refreshTrackData();
                    askForNotificationPermission();
                }
            }
    );


    private final ActivityResultLauncher<String> requestNotificationsPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> { });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissions();
        assignTheme();
        setContentView(R.layout.activity_main);
        setupViewModel();
        tabHelper = new TabHelper(viewModel, this);
        preferencesHelper = new PreferencesHelper(getApplicationContext());
        initPlayerViewHelper();
        startMediaPlayerService();
        checkPath();
    }


    private void checkPath(){
        if(isServiceConnected.get()){
            mediaPlayerService.checkPath();
        }
    }


    private void initPlayerViewHelper(){
        if(playerViewHelper == null){
            playerViewHelper = new PlayerViewHelper(this);
        }
        playerViewHelper.setupViews();
    }


    private void requestPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            askPermissionFor(READ_MEDIA_AUDIO, requestAudioPermissionLauncher);
            askPermissionFor(POST_NOTIFICATIONS, requestNotificationsPermissionLauncher);
            return;
        }
        askPermissionFor(READ_EXTERNAL_STORAGE, requestAudioPermissionLauncher);
    }


    private void askPermissionFor(String permission, ActivityResultLauncher<String> resultLauncher){
        if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
            resultLauncher.launch(permission);
        }
    }


    private void refreshTrackData(){
        if(isServiceConnected.get() && mediaPlayerService != null){
            mediaPlayerService.refreshTrackDataFromFilesystem();
        }
    }


    private void askForNotificationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            askPermissionFor(POST_NOTIFICATIONS, requestNotificationsPermissionLauncher);
        }
    }


    private void assignTheme(){
        themeHelper = new ThemeHelper();
        themeHelper.assignTheme(this);
    }


    public void onStart() {
        super.onStart();
        themeHelper.restartActivityIfDifferentThemeSet(this);
        updateArtistsListInCaseMinTracksSettingHasChanged();
        checkPath();
    }


    public MainViewModel getViewModel(){ return viewModel; }


    private void updateArtistsListInCaseMinTracksSettingHasChanged(){
        if (mediaPlayerService != null) {
            mediaPlayerService.updateArtistView();
        }
    }


    public boolean isUserPlaylistLoaded(){
        return !isPlaylistManagerUnavailable() && mediaPlayerService.getPlaylistManager().isUserPlaylistLoaded();
    }


    public List<Playlist> getAllUserPlaylists(){
        return isPlaylistManagerUnavailable() ? Collections.emptyList() : mediaPlayerService.getPlaylistManager().getAllUserPlaylists();
    }


    private List<String> getTracksOrEmptyList(Function<PlaylistManager, List<String>> function){
        if(mediaPlayerService == null){
            return Collections.emptyList();
        }
        PlaylistManager playlistManager = mediaPlayerService.getPlaylistManager();
        return isPlaylistManagerUnavailable() ? Collections.emptyList() : function.apply(playlistManager);
    }


    private boolean isPlaylistManagerUnavailable(){
        return mediaPlayerService == null || mediaPlayerService.getPlaylistManager() == null;
    }


    public void stopTrack(){
        mediaPlayerService.stop();
        resetElapsedTime();
    }


    public MediaPlayerService getMediaPlayerService(){ return this.mediaPlayerService;}

    public SearchViewHelper getSearchViewHelper(){ return searchViewHelper; }

    public AddTrackToPlaylistViewHelper getAddTrackToPlaylistViewHelper(){ return addTrackToPlaylistViewHelper; }

    public List<String> getAlbumNames(){ return getTracksOrEmptyList(PlaylistManager::getAlbumNames); }

    public List<String> getGenreNames(){ return getTracksOrEmptyList(PlaylistManager::getGenreNames); }

    public List<String> getArtistNames(){ return getTracksOrEmptyList(PlaylistManager::getArtistNames); }

    public void showAddTrackToPlaylistView(){
        FragmentManagerHelper.showDialog(this, AddTrackToPlaylistFragment.newInstance(), "add_track_to_playlist");
       // addTrackToPlaylistViewHelper.showAddTrackToPlaylistView();
    }

    public void hidePlayerViews(){ playerViewHelper.setVisibilityOnPlayerViews(View.INVISIBLE);}

    public void showPlayerViews(){playerViewHelper.setVisibilityOnPlayerViews(View.VISIBLE);}

    public void setBlankTrackInfo(){ playerViewHelper.setBlankTrackInfo();}

    public void notifyMediaPlayerStopped(){ playerViewHelper.notifyMediaPlayerStopped();}

    public void hideTrackSeekBar(){ playerViewHelper.hideTrackSeekBar();}

    public void notifyMediaPlayerPaused(){ playerViewHelper.notifyMediaPlayerPaused();}

    public void notifyShuffleEnabled(){ playerViewHelper.notifyShuffleEnabled(); }

    public void notifyShuffleDisabled(){ playerViewHelper.notifyShuffleDisabled(); }

    public void notifyMediaPlayerPlaying(){playerViewHelper.notifyMediaPlayerPlaying(); }

    public void setSelectedTrack(Track track){ this.selectedTrack = track; }

    public void setTrackDetails(final Track track, int elapsedTime){ playerViewHelper.setTrackDetails(track, elapsedTime); }

    public void setAlbumArt(Bitmap coverArtBitmap){ albumArtHelper.changeAlbumArtTo(coverArtBitmap);}

    public void setBlankAlbumArt(){ albumArtHelper.changeAlbumArtToBlank();}

    public Track getSelectedTrack(){return selectedTrack;}

    public void resetElapsedTime(){playerViewHelper.resetElapsedTime();}

    public void setElapsedTime(long elapsedMilliseconds){playerViewHelper.setElapsedTime(elapsedMilliseconds);}

    public void deselectCurrentTrack() { sendMessage(DESELECT_CURRENT_TRACK_ITEM); }

    public void disableViewForAWhile(View view){disableViewForAWhile(view, 700);}

    public void selectTrack(int index) {mediaPlayerService.selectTrack(index); }

    public void addSelectedTrackToQueue(){enqueue(selectedTrack);}

    public void notifyTrackAddedToPlaylist(){ toast(R.string.toast_track_added_to_playlist); }

    public void notifyTrackAlreadyInPlaylist(){toast(R.string.toast_track_already_in_playlist);}


    public void notifyTrackRemovedFromPlaylist(boolean success){
        toast(success ? R.string.toast_track_removed_from_playlist : R.string.toast_track_removed_from_playlist_fail);
    }


    private void setupViewModel(){ viewModel = new ViewModelProvider(this).get(MainViewModel.class); }


    public void disableViewForAWhile(View view, int delayTime) {
        view.setEnabled(false);
        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(()->view.setEnabled(true), delayTime);
    }


    public void enqueue(Track track){
        mediaPlayerService.getPlaylistManager().addTrackToQueue(track);
        toast(R.string.toast_track_added_to_queue);
    }


    public void notifyTracksAddedToPlaylist(int numberOfTracks){
        switch (numberOfTracks) {
            case 0 -> toast(R.string.toast_no_new_tracks_were_added_to_playlist);
            case 1 -> toast(R.string.toast_one_track_added_to_playlist);
            default -> toast(getString(R.string.toast_tracks_added_to_playlist, numberOfTracks));
        }
    }


    public void removeSelectedTrackFromPlaylist(){
        mediaPlayerService.removeTrackFromCurrentPlaylist(selectedTrack);
    }


    public void loadAlbumOfSelectedTrack(){
        if(mediaPlayerService == null){
            return;
        }
        var playlistHelper = mediaPlayerService.getPlaylistHelper();
        if(playlistHelper != null){
            playlistHelper.loadWholeAlbumOf(selectedTrack);
        }

        toastIfTabsNotAutoSwitched(R.string.toast_album_tracks_loaded);
    }


    public void loadArtistOfSelectedTrack(){
        mediaPlayerService.loadArtistOfTrack(selectedTrack);
        toastIfTabsNotAutoSwitched(R.string.toast_artist_tracks_loaded);
    }


    public void toastIfTabsNotAutoSwitched(int strId){
        log("Entered toastIfTabsNotAutoSwitched() result: " + preferencesHelper.isTabSwitchedAfterPlaylistLoaded());
        if(!preferencesHelper.isTabSwitchedAfterPlaylistLoaded()){
            toast(strId);
        }
    }


    private void log(String msg){
        System.out.println("^^^ MainActivity: " + msg);
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


    @Override
    public void onDestroy(){
        super.onDestroy();
        tabHelper.onDestroy();
    }


    public void displayError(Track track){
        String errorMessage = getString(R.string.error_playing_track_toast_message, track.getPathname());
        toast(errorMessage);
    }


    public void toastFileDoesNotExistError(Track track){
        String errorMessage = getString(R.string.error_loading_track_toast_message, track.getPathname());
        toast(errorMessage);
    }


    public void toast(int resId){
        toast(getString(resId));
    }


    private void toast(String msg){
        runOnUiThread(()-> Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show());
    }


    private void toast(int resId, String arg){
        runOnUiThread(()-> Toast.makeText(MainActivity.this, getString(resId, arg), Toast.LENGTH_SHORT).show());
    }


    public void updateTracksList(Playlist playlist, Track currentTrack, int currentTrackIndex){
        Bundle bundle = new Bundle();
        putInt(bundle, MessageKey.TRACK_INDEX, currentTrackIndex);
        sendMessage(NOTIFY_TO_REQUEST_UPDATED_PLAYLIST, bundle);
        runOnUiThread(()-> updateViews(playlist.getTracks(), currentTrack));
    }


    public void notifyTracksFragmentReady(){
        startMediaPlayerService();
    }


    public void saveTrackIndexToScrollTo(int index){
        viewModel.tracksFragmentSavedIndex = index;
        viewModel.isTracksFragmentIndexSaved = true;
    }


    public void cancelSavedScrollIndex(){viewModel.isTracksFragmentIndexSaved = false;}


    public void notifyAlbumNotLoaded(String albumName){
        toast(R.string.unable_to_load_album_toast_message, albumName);
    }


    public void notifyGenreNotLoaded(String genreName){
        toast(R.string.unable_to_load_genre_toast_message, genreName);
    }


    public void deselectItemsInPlaylistAndArtistTabs(){
        sendMessage(NOTIFY_TO_DESELECT_PLAYLIST_ITEMS);
        sendMessage(NOTIFY_TO_DESELECT_ARTIST_ITEMS);
        sendMessage(NOTIFY_TO_DESELECT_GENRE_ITEMS);
    }


    public void deselectItemsInNonArtistTabs(){
        sendMessage(NOTIFY_TO_DESELECT_PLAYLIST_ITEMS);
        // sendMessage(NOTIFY_TO_DESELECT_ALBUM_ITEMS); // FYI Not required because the album list is reloaded anyway
        sendMessage(NOTIFY_TO_DESELECT_GENRE_ITEMS);
    }


    public void deselectItemsInTabsOtherThanGenre(){
        sendMessage(NOTIFY_TO_DESELECT_PLAYLIST_ITEMS);
        sendMessage(NOTIFY_TO_DESELECT_ARTIST_ITEMS);
        sendMessage(NOTIFY_TO_DESELECT_ALBUM_ITEMS);
    }


    private void setupOptionsMenuForCurrentTrack(){
        List<View> views = List.of(findViewById(R.id.trackTitle), findViewById(R.id.albumArtImageView), findViewById(R.id.trackDetailsLayout));
        views.forEach(v -> v.setOnLongClickListener(c -> {createTrackOptionsFragment(); return false;}));
    }


    private void createTrackOptionsFragment() {
        setSelectedTrack(mediaPlayerService.getCurrentTrack());
        FragmentManagerHelper.showDialog(this, TrackOptionsDialog.newInstance(), "track_options_dialog");
    }


    public void updateAlbumsList(ArrayList<String> albums, String artist){
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(ALBUM_UPDATES.toString(), albums);
        bundle.putString(ALBUM_ARTIST.toString(), artist);
        runOnUiThread(()-> getSupportFragmentManager().setFragmentResult(LOAD_ALBUMS.toString(), bundle));
    }


    public void updateArtistsList(ArrayList<String> artists){
        sendArrayListToFragment(this, LOAD_ARTISTS, MessageKey.ARTIST_UPDATES, artists);
    }


    public void updateGenresList(ArrayList<String> genres){
        sendArrayListToFragment(this, Message.LOAD_GENRES, MessageKey.GENRE_UPDATES, genres);
    }


    public void loadTracksFromPlaylist(Playlist playlist){
        mediaPlayerService.loadPlaylist(playlist);
        tabHelper.switchToTracksTab();
    }


    public void loadTracksFromArtist(String artistName){
        mediaPlayerService.loadTracksFromArtist(artistName);
        tabHelper.switchToAlbumsTab();
    }


    public void loadTracksFromAlbum(String albumName){
        mediaPlayerService.loadTracksFromAlbum(albumName);
        tabHelper.switchToTracksTab();
    }


    public void loadTracksFromGenre(String genreName){
        if(mediaPlayerService != null) {
            mediaPlayerService.loadTracksFromGenre(genreName);
            tabHelper.switchToTracksTab();
        }
    }


    private void updateViews(List<Track> updatedTracks, Track currentTrack){
        playerViewHelper.updateViews(updatedTracks.size(), currentTrack == null);
    }


    public void displayPlaylistRefreshedMessage(){
        if(viewModel.isFirstPlaylistLoad){
            viewModel.isFirstPlaylistLoad = false;
            return;
        }
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


    private void setupFunctionButtons(){
        setupImageButton(R.id.searchButton, ()-> searchViewHelper.toggleSearch());
        setupImageButton(R.id.refreshButton, ()-> mediaPlayerService.refreshTrackDataFromFilesystem());
        setupImageButton(R.id.configButton, this::startSettingsActivity);
        setupImageButton(R.id.aboutButton, this::loadAboutDialog);
    }


    private void setupImageButton(int buttonId, Runnable runnable){
        ImageButton button = findViewById(buttonId);
        button.setOnClickListener(v -> runnable.run());
    }


    private void startSettingsActivity(){
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }


    private void loadAboutDialog(){
        FragmentManagerHelper.showDialog(this, new AboutDialogFragment(), "aboutDialogFragment");
    }


    public void scrollToAndSelectPosition(int index, boolean isSearchResult){
        Bundle bundle = new Bundle();
        putInt(bundle, MessageKey.TRACK_INDEX, index);
        putBoolean(bundle, MessageKey.IS_SEARCH_RESULT, isSearchResult);
        sendMessage(SCROLL_TO_CURRENT_TRACK, bundle);
    }


    public void ensureSelectedTrackIsVisible(){
        if(mediaPlayerService == null){
            return;
        }
        Bundle bundle = new Bundle();
        putInt(bundle, MessageKey.TRACK_INDEX, mediaPlayerService.getCurrentTrackIndex());
        sendMessage(ENSURE_SELECTED_TRACK_IS_VISIBLE, bundle);
    }


    private void sendMessage(Message message, Bundle bundle){
        runOnUiThread(()-> sendFragmentMessage(this, message, bundle));
    }


    private void sendMessage(Message message){
        runOnUiThread(()-> sendFragmentMessage(this, message));
    }


    public void addTrackToPlaylist(Playlist playlist, int position){
        mediaPlayerService.addTrackToPlaylist(selectedTrack, playlist);
    }

}
