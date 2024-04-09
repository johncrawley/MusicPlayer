package com.jacstuff.musicplayer.service;

import static com.jacstuff.musicplayer.service.MediaNotificationManager.NOTIFICATION_ID;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.db.entities.Playlist;
import com.jacstuff.musicplayer.service.db.entities.Track;
import com.jacstuff.musicplayer.service.helpers.art.AlbumArtConsumer;
import com.jacstuff.musicplayer.service.helpers.art.AlbumArtRetriever;
import com.jacstuff.musicplayer.service.helpers.BroadcastHelper;
import com.jacstuff.musicplayer.service.helpers.MediaPlayerHelper;
import com.jacstuff.musicplayer.service.helpers.PlaylistHelper;
import com.jacstuff.musicplayer.service.helpers.PreferencesHelper;
import com.jacstuff.musicplayer.service.playlist.PlaylistManager;

import java.util.List;

public class MediaPlayerService extends Service implements AlbumArtConsumer {

    private MediaNotificationManager mediaNotificationManager;
    private MainActivity mainActivity;
    private final IBinder binder = new LocalBinder();
    private PlaylistHelper playlistHelper;
    private MediaPlayerHelper mediaPlayerHelper;
    private BroadcastHelper broadcastHelper;
    private AlbumArtRetriever albumArtRetriever;
    private PreferencesHelper preferencesHelper;
    private ListIndexManager listIndexManager;

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
        preferencesHelper = new PreferencesHelper(getApplicationContext());
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
        preferencesHelper.assignPreferences(this);
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
            mainActivity.setElapsedTime(mediaPlayerHelper.getElapsedTime());
            mainActivity.setAlbumArt(albumArtRetriever.getCurrentAlbumArt());
        }
        updateListViews(playlistManager);
    }


    public void updateListViews(PlaylistManager playlistManager){
        updateViewTrackList(playlistManager);
        mainActivity.updateAlbumsList(playlistManager.getAlbumNames());
        mainActivity.updateArtistsList(playlistManager.getArtistNames());
        mainActivity.updateGenresList(playlistManager.getGenreNames());
    }


    public MediaPlayerHelper getMediaPlayerHelper(){
        return mediaPlayerHelper;
    }

    public PreferencesHelper getPreferencesHelper(){ return preferencesHelper;}


    public void setFirstTrackAndUpdateViewVisibility(){
       setCurrentTrackAndUpdatePlayerViewVisibility(this::loadFirstTrack);
    }


    public void setCurrentTrackAndUpdateViewVisibility(){
        setCurrentTrackAndUpdatePlayerViewVisibility(this::loadNextTrack);
    }


    private void setCurrentTrackAndUpdatePlayerViewVisibility(Runnable runnable){
        if(!isCurrentTrackEmpty()){
            mainActivity.showPlayerViews();
            return;
        }
        if(getPlaylistManager().hasAnyTracks()){
            runnable.run();
            return;
        }
        mainActivity.hidePlayerViews();
    }


    public void updateMainViewOfStop(boolean shouldUpdateMainView){
        if(shouldUpdateMainView) {
            if(mainActivity != null) {
                mainActivity.notifyMediaPlayerStopped();
            }
        }
    }


    public void refreshTrackDataFromFilesystem() {
        listIndexManager.resetAllIndexes();
        playlistHelper.refreshTrackDataFromFilesystem();
    }


    public List<Track> getTracksForSearch(String str){ return playlistHelper.searchForTracks(str);}

    public void loadTracksFromArtist(String artistName){ playlistHelper.loadTracksFromArtist(artistName);}

    public void loadTracksFromAlbum(String albumName){ playlistHelper.loadTracksFromAlbum(albumName); }

    public void loadTracksFromGenre(String genreName){ playlistHelper.loadTracksFromGenre(genreName); }

    public int getCurrentTrackIndex(){
        return playlistHelper.getIndexOfCurrentTrack();
    }

    public void addTracksFromAristToCurrentPlaylist(String artistName){ playlistHelper.addTracksFromAristToCurrentPlaylist(artistName); }

    public void addTracksFromAlbumToCurrentPlaylist(String albumName){ playlistHelper.addTracksFromAlbumToCurrentPlaylist( albumName); }

    public void loadPlaylist(Playlist playlist){ playlistHelper.loadPlaylist(playlist);}

    public void addTrackToCurrentPlaylist(Track track){ playlistHelper.addTrackToCurrentPlaylist(track);}

    int getTrackCount(){ return playlistHelper.getTrackCount();}

    public void addTrackToPlaylist(Track track, Playlist playlist){ playlistHelper.addTrackToPlaylist(track, playlist);}

    public void removeTrackFromCurrentPlaylist(Track track){ playlistHelper.removeTrackFromCurrentPlaylist(track);}

    public PlaylistManager getPlaylistManager(){return playlistHelper.getPlaylistManager();}

    public void loadAlbumOfTrack(Track track){playlistHelper.loadAlbumOfTrack(track);}

    public void loadArtistOfTrack(Track track){playlistHelper.loadArtistOfTrack(track);}

    public Bitmap getAlbumArtForNotification(){ return albumArtRetriever.getAlbumArtForNotification(); }

    public boolean isCurrentTrackEmpty(){ return mediaPlayerHelper.getCurrentTrack() == null;}

    public void stopPlayingInOneMinute(){
        mediaPlayerHelper.stopPlayingInThreeMinutes(1);
    }

    public void stopPlayingInThreeMinutes(){
        mediaPlayerHelper.stopPlayingInThreeMinutes(3);
    }

    public void stop(){
        mediaPlayerHelper.stop(true);
    }

    public void seek(int milliseconds){
        mediaPlayerHelper.seek(milliseconds);
    }

    public void selectTrack(int index){ mediaPlayerHelper.assignTrack(getPlaylistManager().selectTrack(index));}

    public void enableStopAfterTrackFinishes(){mediaPlayerHelper.enabledStopAfterTrackFinishes();}

    public boolean hasEncounteredError(){ return mediaPlayerHelper.hasEncounteredError();}

    String getCurrentUrl(){ return mediaPlayerHelper.getCurrentUrl(); }

    public Track getCurrentTrack(){ return mediaPlayerHelper.getCurrentTrack(); }

    public boolean isPlaying(){
        return mediaPlayerHelper.isPlaying();
    }

    public void playTrack(){ mediaPlayerHelper.playTrack(); }

    public void stopUpdatingElapsedTimeOnView(){ mediaPlayerHelper.stopUpdatingElapsedTimeOnView(); }

    public void notifyViewOfAlbumNotLoaded(String albumName){ mainActivity.notifyAlbumNotLoaded(albumName);}

    public void notifyViewOfGenreNotLoaded(String genreName){ mainActivity.notifyGenreNotLoaded(genreName);}

    public void notifyViewToDeselectPlaylistAndArtistTabs(){ mainActivity.deselectItemsInPlaylistAndArtistTabs(); }

    public void notifyViewToDeselectNonArtistLists(){ mainActivity.deselectItemsInNonArtistTabs();}

    public void notifyViewToDeselectEverythingButGenre(){ mainActivity.deselectItemsInTabsOtherThanGenre();}

    public void resetElapsedTimeOnMainView(){
        mainActivity.resetElapsedTime();
    }

    public void notifyMainViewThatFileDoesNotExist(Track track){ mainActivity.toastFileDoesNotExistError(track);}

    public void notifyViewOfMediaPlayerStop(){ mainActivity.notifyMediaPlayerStopped(); }

    @Override
    public void setArt(Bitmap albumArt){
        mainActivity.setAlbumArt(albumArt);
    }

    public void setBlankAlbumArt(){
        mainActivity.setBlankAlbumArt();
    }

    public Bitmap getAlbumArt(){ return albumArtRetriever.getCurrentAlbumArt();}

    public void setElapsedTimeOnView(int elapsedTime){ mainActivity.setElapsedTime(elapsedTime);}

    public void notifyMainViewOfMediaPlayerPlaying(){
        mainActivity.notifyMediaPlayerPlaying();
    }

    public void displayErrorOnMainView(Track track){
        mainActivity.displayError(track);
    }

    public void updateArtistView(){mainActivity.updateArtistsList(getPlaylistManager().getArtistNames()); }

    public void updateAlbumsView(){ mainActivity.updateAlbumsList(getPlaylistManager().getAlbumNames()); }

    public void setBlankTrackInfoOnMainView(){
        mainActivity.setBlankTrackInfo();
    }

    public void displayPlaylistRefreshedMessage(int numberOfNewTracks){ mainActivity.displayPlaylistRefreshedMessage(numberOfNewTracks); }

    public AlbumArtRetriever getAlbumArtRetriever(){ return albumArtRetriever;}

    public void updateNotification(){ mediaNotificationManager.updateNotification();}


    public void updateViewTrackList(PlaylistManager playlistManager) {
        Track currentTrack = mediaPlayerHelper.getCurrentTrack();
        int currentTrackIndex = currentTrack == null ? -1 : currentTrack.getIndex();
        mainActivity.updateTracksList(playlistManager.getCurrentPlaylist(), currentTrack, currentTrackIndex);
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
            mainActivity.deselectCurrentTrack();
        }
        else {
            mainActivity.scrollToAndSelectPosition(trackIndexOnCurrentPlaylist, isSearchResult);
        }
    }


    public void updateViewsOnTrackAssigned(){
        mediaNotificationManager.updateNotification();
        mainActivity.setTrackDetails(mediaPlayerHelper.getCurrentTrack(), 0);
        if(mediaPlayerHelper.isPaused()){
            mainActivity.hideTrackSeekBar();
        }
    }


    public void enableShuffle(){
        getPlaylistManager().enableShuffle();
        preferencesHelper.saveShuffleState(true);
        mainActivity.notifyShuffleEnabled();
    }


    public void disableShuffle(){
        getPlaylistManager().disableShuffle();
        preferencesHelper.saveShuffleState(false);
        mainActivity.notifyShuffleDisabled();
    }


    public boolean isShuffleEnabled(){
        return getPlaylistManager().isShuffleEnabled();
    }


    private void moveToForeground(){
        mediaNotificationManager.init();
        Notification notification = mediaNotificationManager.createNotification(getCurrentStatus(), "");
        startForeground(NOTIFICATION_ID, notification);
    }


    String getCurrentStatus(){
        int resId = R.string.status_ready;
        if(mediaPlayerHelper.hasEncounteredError()){
            resId = R.string.status_error;
        }
        else if(mediaPlayerHelper.isPlaying()){
            resId = R.string.status_playing;
        }
        else if(mediaPlayerHelper.isPaused()){
            resId = R.string.status_paused;
        }
        return getApplicationContext().getString(resId);
    }


    public String getReadyStatusStr(){
        return getApplicationContext().getString(R.string.status_ready);
    }


    public void updateViewsForConnecting(){
        broadcastHelper.notifyViewOfConnectingStatus();
        mediaNotificationManager.updateNotification();
    }


    public void setCpuWakeLock(){
        if (checkSelfPermission(Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED) {
            mediaPlayerHelper.setCpuWakeLock(getApplicationContext());
        }
    }


    public void pause(){
        mediaPlayerHelper.pauseMediaPlayer();
        mediaNotificationManager.updateNotification();
        mainActivity.notifyMediaPlayerPaused();
        mediaPlayerHelper.cancelScheduledStoppageOfTrack();
    }

}
