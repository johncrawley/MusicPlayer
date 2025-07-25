package com.jacstuff.musicplayer.trackplayer.service;

import static com.jacstuff.musicplayer.service.helpers.BroadcastHelper.ACTION_PAUSE_PLAYER;
import static com.jacstuff.musicplayer.service.helpers.BroadcastHelper.ACTION_PLAY;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.NotificationCompat;

import com.jacstuff.musicplayer.trackplayer.OpenTrackActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.db.entities.Track;

import java.util.concurrent.atomic.AtomicBoolean;

public class PlayTrackNotificationManager {

    private final Context context;
    private final PlayTrackService playTrackService;
    public final static int PLAY_TRACK_NOTIFICATION_ID = 100201;
    private PendingIntent pendingIntent;
    final static String NOTIFICATION_CHANNEL_ID = "com.jcrawley.music-player-play-track";
    private final AtomicBoolean hasErrorNotificationBeenReplaced = new AtomicBoolean(false);
    private final TrackPlayer trackPlayer;

    public PlayTrackNotificationManager(Context context, PlayTrackService playTrackService, TrackPlayer trackPlayer){
        this.context = context;
        this.playTrackService = playTrackService;
        this.trackPlayer = trackPlayer;
    }


    public Notification createNotification(String heading, String channelName){
        final NotificationCompat.Builder notification = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(heading)
                .setContentText(channelName)
                .setSilent(true)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                .setLargeIcon(playTrackService.getAlbumArtForNotification())
                .setNumber(-1)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setShowWhen(false)
                .setOngoing(true);

        addPlayButtonTo(notification);
        addPauseButtonTo(notification);
        return notification.build();
    }


    public void init(){
        setupNotificationChannel();
        setupNotificationClickForActivity();
    }


    void setupNotificationClickForActivity(){
        Intent resultIntent = new Intent(context, OpenTrackActivity.class);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_IMMUTABLE);
    }


    public void updateNotification() {
        if(!isPostNotificationsPermitted()){
            return;
        }
        resetErrorStatusAfterDelay();
        new Handler(Looper.getMainLooper()).post(() ->
                sendNotification(playTrackService.getCurrentStatus(), trackPlayer.getCurrentTrack()));
    }


    private void resetErrorStatusAfterDelay(){
        int numberOfMillisecondsBeforeResettingStatus = 9_000;
        hasErrorNotificationBeenReplaced.set(true);
        if(!trackPlayer.hasEncounteredError()){
            return;
        }else{
            hasErrorNotificationBeenReplaced.set(false);
        }
        String status = playTrackService.getReadyStatusStr();
        Track track = trackPlayer.getCurrentTrack();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if(!hasErrorNotificationBeenReplaced.get()){
                sendNotification(status, track);
                hasErrorNotificationBeenReplaced.set(true);
            }
        }, numberOfMillisecondsBeforeResettingStatus);
    }


    private void sendNotification( String status, Track track){
        String trackInfo = parseTrackDetails(track);
        Notification notification = createNotification(status, trackInfo);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
     //   notificationManager.notify(PLAY_TRACK_NOTIFICATION_ID, notification);
    }


    public void dismissNotification(){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(PLAY_TRACK_NOTIFICATION_ID);
    }


    private boolean isPostNotificationsPermitted(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            String requiredPermission = Manifest.permission.POST_NOTIFICATIONS;
            return context.checkCallingOrSelfPermission(requiredPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }


    private String parseTrackDetails(Track track){
        if(track == null){
            return "";
        }
        return track.getArtist() + " - " + track.getTitle();
    }


    private void setupNotificationChannel(){
        String channelName = "music_player-play-track-notification-channel";
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName,  NotificationManager.IMPORTANCE_DEFAULT);
        channel.setSound(null, null);
        channel.setShowBadge(false);
        notificationManager.createNotificationChannel(channel);
    }


    private void addPlayButtonTo(NotificationCompat.Builder notification){
        String currentUrl = trackPlayer.getCurrentUrl();
        if(currentUrl == null){
            return;
        }
        if(!trackPlayer.isPlaying() && !currentUrl.isEmpty()){
            notification.addAction(android.R.drawable.ic_media_play,
                    context.getString(R.string.notification_play),
                    createPendingIntentFor(ACTION_PLAY));
        }
    }


    private void addPauseButtonTo(NotificationCompat.Builder notification){
        if(playTrackService.getTrackPlayerHelper().isPlaying()){
            notification.addAction(android.R.drawable.ic_media_pause,
                    context.getString(R.string.notification_pause),
                    createPendingIntentFor(ACTION_PAUSE_PLAYER));
        }
    }


    private PendingIntent createPendingIntentFor(String action){
        return PendingIntent.getBroadcast(context,
                0,
                new Intent(action),
                PendingIntent.FLAG_IMMUTABLE);
    }

}
