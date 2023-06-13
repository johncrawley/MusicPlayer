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
import com.jacstuff.musicplayer.service.db.playlist.Playlist;
import com.jacstuff.musicplayer.service.db.track.Track;
import com.jacstuff.musicplayer.service.playlist.PlaylistManager;

import java.util.List;

public class MediaPlayerService extends Service{


    private MediaNotificationManager mediaNotificationManager;

    private MainActivity mainActivity;
    private final IBinder binder = new LocalBinder();
    private final PlaylistHelper playlistHelper;
    private final MediaPlayerHelper mediaPlayerHelper;
    private BroadcastHelper broadcastHelper;


    public MediaPlayerService() {
        playlistHelper = new PlaylistHelper(this);
        mediaPlayerHelper = new MediaPlayerHelper(this);
    }


    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }


    void notifyMainViewOfMediaPlayerPlaying(){
        mainActivity.notifyMediaPlayerPlaying();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    public void refreshTrackDataFromFilesystem() {
        playlistHelper.refreshTrackDataFromFilesystem();
    }


    public void updateListViews(PlaylistManager playlistManager){
        updateViewTrackList(playlistManager);
        mainActivity.updateAlbumsList(playlistManager.getAlbumNames());
        mainActivity.updateArtistsList(playlistManager.getArtistNames());
    }


    public MediaPlayerHelper getMediaPlayerHelper(){
        return mediaPlayerHelper;
    }


    void displayErrorOnMainView(Track track){
        mainActivity.displayError(track);
    }


    public void updateArtistView(){
        mainActivity.updateArtistsList(getPlaylistManager().getArtistNames());
    }


    public void updateAlbumsView(){
        mainActivity.updateAlbumsList(getPlaylistManager().getAlbumNames());
    }


    public void setCurrentTrackAndUpdatePlayerViewVisibility(){
        if(mediaPlayerHelper.getCurrentTrack() != null){
            mainActivity.showPlayerViews();
            return;
        }
        if(getPlaylistManager().hasAnyTracks()){
            loadNextTrack();
            return;
        }
        mainActivity.hidePlayerViews();
    }


    public List<Track> getTracksForSearch(String str){ return playlistHelper.searchForTracks(str);}


    public void setBlankTrackInfoOnMainView(){
        mainActivity.setBlankTrackInfo();
    }


    public void displayPlaylistRefreshedMessage(int numberOfNewTracks){
        mainActivity.displayPlaylistRefreshedMessage(numberOfNewTracks);
    }


    public void stop(){
        mediaPlayerHelper.stop(true);
    }


    public void seek(int milliseconds){
       mediaPlayerHelper.seek(milliseconds);
    }


    public void updateMainViewOfStop(boolean shouldUpdateMainView){
        if(shouldUpdateMainView) {
            if(mainActivity != null) {
                mainActivity.notifyMediaPlayerStopped();
            }
        }
    }


    public void loadTracksFromArtist(String artistName){ playlistHelper.loadTracksFromArtist(artistName);}

    public void loadTracksFromAlbum(String albumName){ playlistHelper.loadTracksFromAlbum(albumName);  }

    public void addTracksFromAristToCurrentPlaylist(String artistName){ playlistHelper.addTracksFromAristToCurrentPlaylist(artistName); }

    public void addTracksFromAlbumToCurrentPlaylist(String albumName){ playlistHelper.addTracksFromAlbumToCurrentPlaylist( albumName); }

    public void loadPlaylist(Playlist playlist){ playlistHelper.loadPlaylist(playlist);}

    public void addTrackToCurrentPlaylist(Track track){ playlistHelper.addTrackToCurrentPlaylist(track);}

    public void addTrackToPlaylist(Track track, Playlist playlist){ playlistHelper.addTrackToPlaylist(track, playlist);}

    public void removeTrackFromCurrentPlaylist(Track track){ playlistHelper.removeTrackFromCurrentPlaylist(track);}

    public PlaylistManager getPlaylistManager(){return playlistHelper.getPlaylistManager();}

    public void loadAlbumOTrack(Track track){
        playlistHelper.loadAlbumOfTrack(track);
    }


    public void updateViewTrackList(PlaylistManager playlistManager) {
        Track currentTrack = mediaPlayerHelper.getCurrentTrack();
        int currentTrackIndex = currentTrack == null ? -1 : currentTrack.getIndex();
        mainActivity.updateTracksList(playlistManager.getCurrentPlaylist(), currentTrack, currentTrackIndex);
    }


    public void updateViewTrackListAndDeselectList(PlaylistManager playlistManager){
        mainActivity.updateTracksList(playlistManager.getCurrentPlaylist(), mediaPlayerHelper.getCurrentTrack(),-1);
    }


    public void notifyViewToDeselectPlaylistAndArtistTabs(){
        mainActivity.deselectItemsInPlaylistAndArtistTabs();
    }


    public List<Track> getTrackList(){
        return getPlaylistManager().getCurrentPlaylist().getTracks();
    }


    public void selectAndPlayTrack(Track track){
        mediaPlayerHelper.selectAndPlayTrack(track);
        getPlaylistManager().addToTrackHistory(track);
        mainActivity.setTrackDetails(mediaPlayerHelper.getCurrentTrack(), 0);
    }


    public void selectTrack(int index){
        mediaPlayerHelper.assignTrack(getPlaylistManager().selectTrack(index));
    }


    public void loadNextTrack(){
        Track track = getPlaylistManager().getNextTrack();
        mediaPlayerHelper.loadNext(track);
    }


    public void loadPreviousTrack(){
        mediaPlayerHelper.loadPreviousTrack(getPlaylistManager().getPreviousTrack());
    }


    void scrollToPositionOf(Track track){
        int trackIndexOnCurrentPlaylist = getPlaylistManager().getCurrentIndexOf(track);
        if(trackIndexOnCurrentPlaylist == - 1){
            mainActivity.deselectCurrentTrack();
        }
        else {
            mainActivity.scrollToAndSelectPosition(trackIndexOnCurrentPlaylist);
        }
    }


    public void enableStopAfterTrackFinishes(){
        mediaPlayerHelper.enabledStopAfterTrackFinishes();
    }


    public void stopPlayingInOneMinute(){
        mediaPlayerHelper.stopPlayingInThreeMinutes(1);
    }


    public void stopPlayingInThreeMinutes(){
        mediaPlayerHelper.stopPlayingInThreeMinutes(3);
    }


    void resetElapsedTimeOnMainView(){
        mainActivity.resetElapsedTime();
    }


    void updateViewsOnTrackAssigned(){
        mediaNotificationManager.updateNotification();
        mainActivity.setTrackDetails(mediaPlayerHelper.getCurrentTrack(), 0);
        if(mediaPlayerHelper.isPaused()){
            mainActivity.hideTrackSeekBar();
        }
    }


    public void setActivity(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        playlistHelper.onSetActivity(mainActivity);
    }


    public void setAlbumArtOnMainView(Bitmap albumArt){
        mainActivity.setAlbumArt(albumArt);
    }


    public void updateViews(PlaylistManager playlistManager){
        Track currentTrack = mediaPlayerHelper.getCurrentTrack();
        if(currentTrack != null){
            mainActivity.setTrackDetails(currentTrack, 0);
            mainActivity.setElapsedTime(mediaPlayerHelper.getElapsedTime());
            mainActivity.setAlbumArt(mediaPlayerHelper.getCurrentAlbumArt());
        }
        updateListViews(playlistManager);
    }


    public boolean isPlaying(){
        return mediaPlayerHelper.isPlaying();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayerHelper.createMediaPlayer();
        broadcastHelper = new BroadcastHelper(this);
        mediaNotificationManager = new MediaNotificationManager(getApplicationContext(), this);
        playlistHelper.setMediaNotificationManager(mediaNotificationManager);
        moveToForeground();
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


    public void enableShuffle(){
        getPlaylistManager().enableShuffle();
        mainActivity.notifyShuffleEnabled();
    }


    public boolean isShuffleEnabled(){
        return getPlaylistManager().isShuffleEnabled();
    }


    public void disableShuffle(){
        getPlaylistManager().disableShuffle();
        mainActivity.notifyShuffleDisabled();
    }


    public void playTrack(){
        mediaPlayerHelper.playTrack();
    }


    public void stopUpdatingElapsedTimeOnView(){
        mediaPlayerHelper.stopUpdatingElapsedTimeOnView();
    }


    public void setElapsedTimeOnView(int elapsedTime){
        mainActivity.setElapsedTime(elapsedTime);
    }


    int getTrackCount(){
        return playlistHelper.getTrackCount();
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


    public Track getCurrentTrack(){
        return mediaPlayerHelper.getCurrentTrack();
    }


    public Bitmap getAlbumArt(){
        return mediaPlayerHelper.getCurrentAlbumArt();
    }



    public void notifyMainViewThatFileDoesNotExist(Track track){
        mainActivity.toastFileDoesNotExistError(track);
    }


    String getCurrentUrl(){
        return mediaPlayerHelper.getCurrentUrl();
    }


    void updateViewsForConnecting(){
        broadcastHelper.notifyViewOfConnectingStatus();
        mediaNotificationManager.updateNotification();
    }


    public void notifyViewOfMediaPlayerStop(){
         mainActivity.notifyMediaPlayerStopped();
     }


    void setCpuWakeLock(){
        if (checkSelfPermission(Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED) {
            mediaPlayerHelper.setCpuWakeLock(getApplicationContext());
        }
    }


    public void updateNotification(){
        mediaNotificationManager.updateNotification();
    }


    public void pause(){
        mediaPlayerHelper.pauseMediaPlayer();
        mediaNotificationManager.updateNotification();
        mainActivity.notifyMediaPlayerPaused();
        mediaPlayerHelper.cancelScheduledStoppageOfTrack();
    }

}
