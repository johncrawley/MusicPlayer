package com.jacstuff.musicplayer.service;


import static com.jacstuff.musicplayer.service.MediaNotificationManager.NOTIFICATION_ID;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.db.playlist.Playlist;
import com.jacstuff.musicplayer.service.db.track.Track;
import com.jacstuff.musicplayer.service.playlist.PlaylistManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MediaPlayerService extends Service{

    public static final String ACTION_PLAY = "com.j.crawley.music_player.play";
    public static final String ACTION_PAUSE_PLAYER = "com.j.crawley.music_player.pausePlayer";
    public static final String ACTION_REQUEST_STATUS = "com.j.crawley.music_player.requestStatus";

    public static final String ACTION_SELECT_PREVIOUS_TRACK = "com.j.crawley.music_player.selectPreviousTrack";
    public static final String ACTION_SELECT_NEXT_TRACK = "com.j.crawley.music_player.selectNextTrack";
    public static final String ACTION_NOTIFY_VIEW_OF_STOP = "com.j.crawley.music_player.notifyViewOfStop";
    public static final String ACTION_NOTIFY_VIEW_OF_CONNECTING = "com.j.crawley.music_player.notifyViewOfPlay";
    public static final String ACTION_NOTIFY_VIEW_OF_PLAYING = "com.j.crawley.music_player.notifyViewOfPlayInfo";

    private MediaNotificationManager mediaNotificationManager;
    Map<BroadcastReceiver, String> broadcastReceiverMap;
    private final AtomicBoolean shouldSkipBroadcastReceivedForTrackChange = new AtomicBoolean();

    private MainActivity mainActivity;
    private final IBinder binder = new LocalBinder();
    private final PlaylistHelper playlistHelper;
    private final MediaPlayerHelper mediaPlayerHelper;


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


    public void updateViewTrackList(PlaylistManager playlistManager) {
        Track currentTrack = mediaPlayerHelper.getCurrentTrack();
        int currentTrackIndex = currentTrack == null ? -1 : currentTrack.getIndex();
        mainActivity.updateTracksList(playlistManager.getTracks(), currentTrack, currentTrackIndex);
    }


    public void updateViewTrackListAndDeselectList(PlaylistManager playlistManager){
        mainActivity.updateTracksList(playlistManager.getTracks(), mediaPlayerHelper.getCurrentTrack(),-1);
    }


    public List<Track> getTrackList(){
        return getPlaylistManager().getTracks();
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


    public void stopPlayingInThreeMinutes(){
        mediaPlayerHelper.stopPlayingInThreeMinutes();
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
        setupBroadcastReceivers();
        mediaNotificationManager = new MediaNotificationManager(getApplicationContext(), this);
        playlistHelper.setMediaNotificationManager(mediaNotificationManager);
        moveToForeground();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBroadcastReceivers();
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


    private void setupBroadcastReceivers(){
        setupBroadcastReceiversMap();
        registerBroadcastReceivers();
    }


    private void setupBroadcastReceiversMap(){
        broadcastReceiverMap = new HashMap<>();
        broadcastReceiverMap.put(serviceReceiverForPlay, ACTION_PLAY);
        broadcastReceiverMap.put(serviceReceiverForRequestStatus, ACTION_REQUEST_STATUS);
        broadcastReceiverMap.put(serviceReceiverForPause, ACTION_PAUSE_PLAYER);
        broadcastReceiverMap.put(serviceReceiverForNext, ACTION_SELECT_NEXT_TRACK);
        broadcastReceiverMap.put(serviceReceiverForPrevious, ACTION_SELECT_PREVIOUS_TRACK);
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
        sendBroadcast(ACTION_NOTIFY_VIEW_OF_CONNECTING);
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


    private void sendBroadcast(String action){
        sendBroadcast(new Intent(action));
    }


    private void registerBroadcastReceivers(){
        for(BroadcastReceiver bcr : broadcastReceiverMap.keySet()){
            IntentFilter intentFilter = new IntentFilter(broadcastReceiverMap.get(bcr));
            registerReceiver(bcr, intentFilter);
        }
    }


    private void unregisterBroadcastReceivers(){
        for(BroadcastReceiver bcr : broadcastReceiverMap.keySet()){
            unregisterReceiver(bcr);
        }
    }


    private final BroadcastReceiver serviceReceiverForPlay = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mediaPlayerHelper.onReceiveBroadcastForPlay();
        }
    };


    private final BroadcastReceiver serviceReceiverForNext = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleTrackChangeRequest(MediaPlayerService.this::loadNextTrack);
        }
    };


    private final BroadcastReceiver serviceReceiverForPrevious = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleTrackChangeRequest(MediaPlayerService.this::loadPreviousTrack);
        }
    };


    private void handleTrackChangeRequest(Runnable operation){
        if(shouldSkipBroadcastReceivedForTrackChange.get()){
            return;
        }
        shouldSkipBroadcastReceivedForTrackChange.set(true);
        operation.run();
        new Handler(Looper.getMainLooper())
                .postDelayed(()-> shouldSkipBroadcastReceivedForTrackChange.set(false),
                        600);
    }


    private final BroadcastReceiver serviceReceiverForRequestStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String broadcast = mediaPlayerHelper.isPlaying() ? ACTION_NOTIFY_VIEW_OF_PLAYING : ACTION_NOTIFY_VIEW_OF_STOP;
            sendBroadcast(broadcast);
        }
    };


    private final BroadcastReceiver serviceReceiverForPause = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pause();
        }
    };

}
