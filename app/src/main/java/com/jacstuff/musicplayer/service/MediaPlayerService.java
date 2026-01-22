package com.jacstuff.musicplayer.service;

import static android.view.View.INVISIBLE;
import static com.jacstuff.musicplayer.service.helpers.preferences.PrefKey.IS_SHUFFLE_ENABLED;
import static com.jacstuff.musicplayer.service.notifications.MediaNotificationManager.NOTIFICATION_ID;
import static com.jacstuff.musicplayer.view.utils.PlayerViewHelper.MediaPlayerNotification.MEDIA_PLAYER_PAUSED;
import static com.jacstuff.musicplayer.view.utils.PlayerViewHelper.MediaPlayerNotification.MEDIA_PLAYER_PLAYING;
import static com.jacstuff.musicplayer.view.utils.PlayerViewHelper.MediaPlayerNotification.MEDIA_PLAYER_STOPPED;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.db.entities.Playlist;
import com.jacstuff.musicplayer.service.db.entities.Track;
import com.jacstuff.musicplayer.service.helpers.art.AlbumArtConsumer;
import com.jacstuff.musicplayer.service.helpers.art.AlbumArtRetriever;
import com.jacstuff.musicplayer.service.helpers.BroadcastHelper;
import com.jacstuff.musicplayer.service.helpers.MediaPlayerHelper;
import com.jacstuff.musicplayer.service.helpers.PlaylistHelper;
import com.jacstuff.musicplayer.service.helpers.preferences.PreferencesHelperImpl;
import com.jacstuff.musicplayer.service.notifications.MediaNotificationManager;
import com.jacstuff.musicplayer.service.playlist.PlaylistManager;
import com.jacstuff.musicplayer.view.utils.PlayerViewHelper;

public class MediaPlayerService extends Service implements AlbumArtConsumer {

    private MediaNotificationManager mediaNotificationManager;
    private MainActivity mainActivity;
    private final IBinder binder = new LocalBinder();
    private PlaylistHelper playlistHelper;
    private MediaPlayerHelper mediaPlayerHelper;
    private BroadcastHelper broadcastHelper;
    private AlbumArtRetriever albumArtRetriever;
    private PreferencesHelperImpl preferencesHelper;
    private final ListIndexManager listIndexManager;
    private PlayerViewHelper playerViewHelper;


    public MediaPlayerService() {
        listIndexManager = new ListIndexManager();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayerHelper = new MediaPlayerHelper(this);
        mediaPlayerHelper.createMediaPlayer();
        playlistHelper = new PlaylistHelper(this);
        broadcastHelper = new BroadcastHelper(this);
        preferencesHelper = new PreferencesHelperImpl(getApplicationContext());
        mediaNotificationManager = new MediaNotificationManager(getApplicationContext(), this);
        playlistHelper.setMediaNotificationManager(mediaNotificationManager);
        albumArtRetriever = new AlbumArtRetriever(this, getApplicationContext());
        moveToForeground();
    }


    public ListIndexManager getListIndexManager(){
        return listIndexManager;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        broadcastHelper.onDestroy();
        mediaPlayerHelper.stop(false, false);
        mediaPlayerHelper.onDestroy();
        mediaNotificationManager.dismissNotification();
        mediaNotificationManager = null;
        mediaPlayerHelper = null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return Service.START_NOT_STICKY; // service is not restarted when terminated
    }


    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    public void setActivity(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        playlistHelper.onSetActivity(mainActivity);
        playerViewHelper = mainActivity.getPlayerViewHelper();
        setShuffleState(preferencesHelper.getBoolean(IS_SHUFFLE_ENABLED));
        checkPath();
    }


    public void checkPath(){
        if(preferencesHelper != null && preferencesHelper.hasPathChanged()){
            refreshTrackDataFromFilesystem();
        }
    }


    public void updateViews(PlaylistManager playlistManager){
        Track currentTrack = mediaPlayerHelper.getCurrentTrack();
        if(currentTrack != null){
            mainActivity.setTrackDetails(currentTrack, 0);
            playerViewHelper.setElapsedTime(mediaPlayerHelper.getElapsedTime());
            mainActivity.setAlbumArt(albumArtRetriever.getCurrentAlbumArt());
        }
        updateListViews(playlistManager);
    }


    public void updateListViews(PlaylistManager playlistManager){
        updateViewTrackList(playlistManager);
        mainActivity.updateAlbumsList(playlistManager.getAlbumNames(), playlistManager.getCurrentArtistName().orElse(""));
        mainActivity.updateArtistsList(playlistManager.getArtistNames());
        mainActivity.updateGenresList(playlistManager.getGenreNames());
    }


    public MediaPlayerHelper getMediaPlayerHelper(){
        return mediaPlayerHelper;
    }


    public PreferencesHelperImpl getPreferencesHelper(){ return preferencesHelper;}


    public void setFirstTrackAndUpdateViewVisibility(){
       setCurrentTrackAndUpdatePlayerViewVisibility(this::loadFirstTrack);
    }


    public void setCurrentTrackAndUpdateViewVisibility(){
        setCurrentTrackAndUpdatePlayerViewVisibility(this::loadNextTrack);
    }


    private void setCurrentTrackAndUpdatePlayerViewVisibility(Runnable runnable){
        if(!isCurrentTrackEmpty()){
            playerViewHelper.setVisibilityOnPlayerViews(View.VISIBLE);
            return;
        }
        if(getPlaylistManager().hasAnyTracks()){
            runnable.run();
            return;
        }
        playerViewHelper.setVisibilityOnPlayerViews(INVISIBLE);
    }


    public void updateMainViewOfStop(boolean shouldUpdateMainView){
        if(shouldUpdateMainView) {
            if(playerViewHelper != null) {
                playerViewHelper.notify(MEDIA_PLAYER_STOPPED);
            }
        }
    }


    public void refreshTrackDataFromFilesystem() {
        listIndexManager.resetAllIndexes();
        playlistHelper.refreshTrackDataFromFilesystem();
    }


    public PlaylistHelper getPlaylistHelper(){ return playlistHelper;}

    public void loadTracksFromAlbum(String albumName){ playlistHelper.loadTracksFromAlbum(albumName); }

    public int getCurrentTrackIndex(){
        return playlistHelper.getIndexOfCurrentTrack();
    }

    public void addTracksFromAristToCurrentPlaylist(String artistName){ playlistHelper.addTracksFromAristToCurrentPlaylist(artistName); }

    public void addTracksFromAlbumToCurrentPlaylist(String albumName){ playlistHelper.addTracksFromAlbumToCurrentPlaylist( albumName); }

    public void loadPlaylist(Playlist playlist){ playlistHelper.loadPlaylist(playlist);}

    public void addTrackToCurrentPlaylist(Track track){ playlistHelper.addTrackToCurrentPlaylist(track);}

    public int getTrackCount(){ return playlistHelper.getTrackCount();}

    public void addTrackToPlaylist(Track track, Playlist playlist){ playlistHelper.addTrackToPlaylist(track, playlist);}

    public void removeTrackFromCurrentPlaylist(Track track){ playlistHelper.removeTrackFromCurrentPlaylist(track);}

    public PlaylistManager getPlaylistManager(){return playlistHelper.getPlaylistManager();}

    public void loadArtistOfTrack(Track track){playlistHelper.loadArtistOfTrack(track);}

    public Bitmap getAlbumArtForNotification(){ return albumArtRetriever.getAlbumArtForNotification(); }

    public boolean isCurrentTrackEmpty(){ return mediaPlayerHelper.getCurrentTrack() == null;}

    public void stopPlayingInOneMinute(){
        mediaPlayerHelper.stopPlayingAfterNumberOfMinutes(1);
    }

    public void stopPlayingInThreeMinutes(){
        mediaPlayerHelper.stopPlayingAfterNumberOfMinutes(3);
    }

    public void stop(){
        mediaPlayerHelper.stop(true);
    }

    public void seek(int milliseconds){
        mediaPlayerHelper.seek(milliseconds);
    }

    public void selectTrack(int index){ mediaPlayerHelper.assignTrack(getPlaylistManager().selectTrack(index));}

    public void enableStopAfterTrackFinishes(){mediaPlayerHelper.enabledStopAfterTrackFinishes();}

    public boolean hasNotEncounteredError(){ return mediaPlayerHelper.hasEncounteredError();}

    public String getCurrentUrl(){ return mediaPlayerHelper.getCurrentUrl(); }

    public Track getCurrentTrack(){ return mediaPlayerHelper.getCurrentTrack(); }

    public boolean isPlaying(){
        return mediaPlayerHelper.isPlaying();
    }


    public void notifyViewOfAlbumNotLoaded(String albumName){ mainActivity.notifyAlbumNotLoaded(albumName);}

    public void notifyViewOfGenreNotLoaded(String genreName){ mainActivity.notifyGenreNotLoaded(genreName);}

    public void notifyViewToDeselectPlaylistAndArtistTabs(){ mainActivity.deselectItemsInPlaylistAndArtistTabs(); }

    public void notifyViewToDeselectNonArtistLists(){ mainActivity.deselectItemsInNonArtistTabs();}

    public void notifyViewToDeselectEverythingButGenre(){ mainActivity.deselectItemsInTabsOtherThanGenre();}

    public void resetElapsedTimeOnMainView(){
        mainActivity.resetElapsedTime();
    }

    public void notifyMainViewThatFileDoesNotExist(Track track){ mainActivity.toastFileDoesNotExistError(track);}

    public void notifyViewOfMediaPlayerStop(){ playerViewHelper.notify(MEDIA_PLAYER_STOPPED); }

    @Override
    public void setArt(Bitmap albumArt){
        mainActivity.setAlbumArt(albumArt);
    }


    public void setBlankAlbumArt(){
        mainActivity.setBlankAlbumArt();
    }


    public Bitmap getAlbumArt(){ return albumArtRetriever.getCurrentAlbumArt();}


    public void setElapsedTimeOnView(int elapsedTime){ playerViewHelper.setElapsedTime(elapsedTime);}


    public void notifyMainViewOfMediaPlayerPlaying(){
        playerViewHelper.notify(MEDIA_PLAYER_PLAYING);
    }


    public void displayErrorOnMainView(Track track){
        mainActivity.displayError(track);
    }


    public void updateArtistView(){mainActivity.updateArtistsList(getPlaylistManager().getArtistNames()); }


    public void updateAlbumsView(){
        var playlistManager = getPlaylistManager();
        mainActivity.updateAlbumsList(playlistManager.getAlbumNames(), playlistManager.getCurrentArtistName().orElse(""));
    }


    public void setBlankTrackInfoOnMainView(){
        playerViewHelper.setBlankTrackInfo();
    }


    public void displayPlaylistRefreshedMessage(int numberOfNewTracks){ mainActivity.displayPlaylistRefreshedMessage(numberOfNewTracks); }


    public AlbumArtRetriever getAlbumArtRetriever(){ return albumArtRetriever;}


    public void updateNotification(String source){
        log("entered updateNotification() from: " + source);
        mediaNotificationManager.updateNotification();}


    public void updateViewTrackList(PlaylistManager playlistManager) {
        var currentTrack = mediaPlayerHelper.getCurrentTrack();
        int currentTrackIndex = currentTrack == null ? -1 : currentTrack.getIndex();
        mainActivity.updateTracksList(playlistManager.getCurrentPlaylist(), currentTrack, currentTrackIndex);
    }


    public void updateViewTrackList(){
        updateViewTrackList(playlistHelper.getPlaylistManager());
    }


    public void updateViewTrackListAndDeselectList(PlaylistManager playlistManager){
        mainActivity.updateTracksList(playlistManager.getCurrentPlaylist(), mediaPlayerHelper.getCurrentTrack(),-1);
    }


    public void selectAndPlayTrack(Track track){
        mediaPlayerHelper.selectAndPlayTrack(track);
        getPlaylistManager().addToTrackHistory(track);
        getPlaylistManager().assignCurrentIndexIfApplicable(track);
        mainActivity.setTrackDetails(mediaPlayerHelper.getCurrentTrack(), 0);
    }


    public void loadNextTrack(){
        getPlaylistManager().getNextTrack().ifPresent(mediaPlayerHelper::loadNext);
    }


    public void loadFirstTrack(){
        getPlaylistManager().getFirstTrack().ifPresent(mediaPlayerHelper::loadNext);
    }


    public void loadPreviousTrack(){
        getPlaylistManager().getPreviousTrack().ifPresent(mediaPlayerHelper::loadPreviousTrack);
    }


    public void scrollToPositionOf(Track track){
        scrollToPositionOf(track, false);
    }


    public void scrollToPositionOf(Track track, boolean isSearchResult){
        int trackIndexOnCurrentPlaylist = getPlaylistManager().getCurrentIndexOf(track);
        if(trackIndexOnCurrentPlaylist == - 1){
            new Handler(Looper.getMainLooper()).postDelayed(  ()-> mainActivity.deselectCurrentTrack(), 300);
        }
        else {
            mainActivity.scrollToAndSelectPosition(trackIndexOnCurrentPlaylist, isSearchResult);
        }
    }


    public void updateViewsOnTrackAssigned(){
        updateNotification("updateViewsOnTrackAssigned()");
        mainActivity.setTrackDetails(mediaPlayerHelper.getCurrentTrack(), 0);
        if(mediaPlayerHelper.isPaused()){
            playerViewHelper.hideTrackSeekBar();
        }
    }


    public void enableShuffle(){
        setShuffleState(true);
        preferencesHelper.saveShuffleState(true);
    }


    public void disableShuffle(){
        setShuffleState(false);
        preferencesHelper.saveShuffleState(false);
    }


    private void setShuffleState(boolean isEnabled){
        getPlaylistManager().setShuffleState(isEnabled);
        mainActivity.getPlayerViewHelper().setShuffleState(isEnabled);
    }


    public boolean isShuffleEnabled(){
        return getPlaylistManager().isShuffleEnabled();
    }


    private void moveToForeground(){
        mediaNotificationManager.init();
        var notification = mediaNotificationManager.createNotification(getCurrentStatus());
        startForeground(NOTIFICATION_ID, notification);
    }


    public String getCurrentStatus(){
        int resId = mediaPlayerHelper.hasEncounteredError() ? R.string.status_error
                : mediaPlayerHelper.isPlaying() ? R.string.status_playing
                : mediaPlayerHelper.isPaused() ? R.string.status_paused
                : R.string.status_ready;
        return getApplicationContext().getString(resId);
    }


    private void log(String msg){
        System.out.println("^^^ MediaPlayerService: " +  msg);
    }


    public String getReadyStatusStr(){
        return getApplicationContext().getString(R.string.status_ready);
    }


    public void updateViewsForConnecting(){
        broadcastHelper.notifyViewOfConnectingStatus();
        updateNotification("updateViewsForConnecting()");
    }


    public void setCpuWakeLock(){
        if (checkSelfPermission(Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED) {
            mediaPlayerHelper.setCpuWakeLock(getApplicationContext());
        }
    }


    public void pause(){
        mediaPlayerHelper.pauseMediaPlayer();
        mediaNotificationManager.updateNotification();
        playerViewHelper.notify(MEDIA_PLAYER_PAUSED);
    }


}
