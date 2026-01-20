package com.jacstuff.musicplayer.service.notifications;

import static com.jacstuff.musicplayer.service.helpers.BroadcastHelper.ACTION_PAUSE_PLAYER;
import static com.jacstuff.musicplayer.service.helpers.BroadcastHelper.ACTION_PLAY;
import static com.jacstuff.musicplayer.service.helpers.BroadcastHelper.ACTION_SELECT_NEXT_TRACK;
import static com.jacstuff.musicplayer.service.helpers.BroadcastHelper.ACTION_SELECT_PREVIOUS_TRACK;

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
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.MediaPlayerService;
import com.jacstuff.musicplayer.service.db.entities.Track;

import java.util.concurrent.atomic.AtomicBoolean;


public class MediaNotificationManager {

    private final Context context;
    private final MediaPlayerService mediaPlayerService;
    public final static int NOTIFICATION_ID = 1001;
    private PendingIntent pendingIntent;
    final static String NOTIFICATION_CHANNEL_ID = "com.jcrawley.musicplayer-notification";
    private final AtomicBoolean hasErrorNotificationBeenReplaced = new AtomicBoolean(false);
    RemoteViews notificationLayout, notificationLayoutExpanded;
    private TextView expandedTitle;


    public MediaNotificationManager(Context context, MediaPlayerService mediaPlayerService){
        this.context = context;
        this.mediaPlayerService = mediaPlayerService;

        // Get the layouts to use in the custom notification
        notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification_small);
        notificationLayoutExpanded = new RemoteViews(context.getPackageName(), R.layout.notification_large);
        View expandedLayout = notificationLayoutExpanded.apply(context, null);
        expandedTitle = expandedLayout.findViewById(R.id.notification_song_title);
    }


    Notification createCustomNotification(String heading, String channelName) {
        setupButtonIntent(R.id.nextButton, ACTION_SELECT_NEXT_TRACK);
        setupButtonIntent(R.id.prevButton, ACTION_SELECT_PREVIOUS_TRACK);
        setupButtonIntent(R.id.pauseButton, ACTION_PAUSE_PLAYER);
        setupButtonIntent(R.id.playButton, ACTION_PLAY);

        return new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutExpanded)
                .setNumber(-1)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setShowWhen(false)
                .setOngoing(true).build();
    }


    private void log(String msg){
        System.out.println("^^^ MediaNotificationManager: " + msg);
    }


    private void setupButtonIntent(int buttonId, String action){
        log("entered setupButtonIntent()");
        var intent = new Intent(action);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        var pendingSwitchIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        notificationLayoutExpanded.setOnClickPendingIntent(buttonId, pendingSwitchIntent);
    }


    public Notification createNotification(String heading, String channelName){
        log("entered createNotification() heading: " + heading);
        final NotificationCompat.Builder notification = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(heading)
                .setContentText(channelName)
                .setSilent(true)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                .setLargeIcon(mediaPlayerService.getAlbumArtForNotification())
                .setNumber(-1)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setShowWhen(false)
                .setOngoing(true);

        addPreviousButtonTo(notification);
        addPlayButtonTo(notification);
        addPauseButtonTo(notification);
        addNextButtonTo(notification);
        return notification.build();
    }


    public void init(){
        setupNotificationChannel();
        setupNotificationClickForActivity();
    }


    void setupNotificationClickForActivity(){
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_IMMUTABLE);
    }


    public void updateNotification() {
        if(!isPostNotificationsPermitted()){
            return;
        }
        resetErrorStatusAfterDelay();
        new Handler(Looper.getMainLooper()).post(() -> {
            log("Entered updateNotification()");
            sendNotification(mediaPlayerService.getCurrentStatus(), mediaPlayerService.getCurrentTrack());
        });
    }


    private void resetErrorStatusAfterDelay(){
        hasErrorNotificationBeenReplaced.set(true);
        if(mediaPlayerService.getMediaPlayerHelper().hasEncounteredError()){
            hasErrorNotificationBeenReplaced.set(false);
            var status = mediaPlayerService.getReadyStatusStr();
            var track = mediaPlayerService.getCurrentTrack();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if(!hasErrorNotificationBeenReplaced.get()){
                    log("Entered resetErrorStatusAfterDelay");
                    sendNotification(status, track);
                    hasErrorNotificationBeenReplaced.set(true);
                }
            }, 5000);
        }
    }


    private void sendNotification( String status, Track track){
        log("Entered sendNotification()");
        var trackInfo = parseTrackDetails(track);
        var notification = createNotification(status, trackInfo);
        var notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }


    private void updateCustomNotification(Track track){
        expandedTitle.setText(track.getTitle());
        notificationLayoutExpanded.setTextViewText(R.id.notification_song_title, track.getTitle());
        notificationLayout.setTextViewText(R.id.notification_song_title, track.getTitle());
    }


    public void dismissNotification(){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
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
        var channelName = "music_player-notification-channel";
        var notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        var channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName,  NotificationManager.IMPORTANCE_DEFAULT);
        channel.setSound(null, null);
        channel.setShowBadge(false);
        notificationManager.createNotificationChannel(channel);
    }


    private void addPlayButtonTo(NotificationCompat.Builder notification){
        var currentUrl = mediaPlayerService.getCurrentUrl();
        if(currentUrl == null){
            return;
        }
        if(!mediaPlayerService.isPlaying() && !currentUrl.isEmpty()){
            notification.addAction(android.R.drawable.ic_media_play,
                    context.getString(R.string.notification_play),
                    createPendingIntentFor(ACTION_PLAY));
        }
    }


    private void addPauseButtonTo(NotificationCompat.Builder notification){
        if(mediaPlayerService.isPlaying()){
            notification.addAction(android.R.drawable.ic_media_pause,
                    context.getString(R.string.notification_pause),
                    createPendingIntentFor(ACTION_PAUSE_PLAYER));
        }
    }


    private void addPreviousButtonTo(NotificationCompat.Builder notification){
        if(isThereLessThanTwoTracks()) {
            return;
        }
        notification.addAction(android.R.drawable.ic_media_previous,
                context.getString(R.string.notification_prev),
                createPendingIntentFor(ACTION_SELECT_PREVIOUS_TRACK));
    }


    private void addNextButtonTo(NotificationCompat.Builder notification){
        if(isThereLessThanTwoTracks()) {
            return;
        }
        notification.addAction(android.R.drawable.ic_media_next,
                context.getString(R.string.notification_next),
                createPendingIntentFor(ACTION_SELECT_NEXT_TRACK));
    }


    private boolean isThereLessThanTwoTracks(){
        return mediaPlayerService.getTrackCount() < 2;
    }


    private PendingIntent createPendingIntentFor(String action){
        return PendingIntent.getBroadcast(context,
                0,
                new Intent(action),
                PendingIntent.FLAG_IMMUTABLE);
    }

}
