package com.jacstuff.musicplayer.service;


import static com.jacstuff.musicplayer.service.MediaPlayerService.ACTION_PAUSE_PLAYER;
import static com.jacstuff.musicplayer.service.MediaPlayerService.ACTION_PLAY;
import static com.jacstuff.musicplayer.service.MediaPlayerService.ACTION_SELECT_NEXT_TRACK;
import static com.jacstuff.musicplayer.service.MediaPlayerService.ACTION_SELECT_PREVIOUS_TRACK;
import static com.jacstuff.musicplayer.service.MediaPlayerService.ACTION_STOP_PLAYER;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.NotificationCompat;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;


public class MediaNotificationManager {

    private final Context context;
    private final MediaPlayerService mediaPlayerService;
    final static int NOTIFICATION_ID = 1001;
    private PendingIntent pendingIntent;
    final static String NOTIFICATION_CHANNEL_ID = "com.jcrawley.webradio-notification";


    MediaNotificationManager(Context context, MediaPlayerService mediaPlayerService){
        this.context = context;
        this.mediaPlayerService = mediaPlayerService;
    }


    Notification createNotification(String heading, String channelName){
        final NotificationCompat.Builder notification = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(heading)
                .setContentText(channelName)
                .setSilent(true)
                .setSmallIcon(R.drawable.recycler_bg_selector)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                .setNumber(-1)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setShowWhen(false)
                .setOngoing(true);
        addPreviousButtonTo(notification);
        addPlayButtonTo(notification);
        //addStopButtonTo(notification);
        addPauseButtonTo(notification);
        addNextButtonTo(notification);
        return notification.build();
    }

    void init(){
        setupNotificationChannel();
        setupNotificationClickForActivity();
    }

    void setupNotificationClickForActivity(){
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_IMMUTABLE);
    }


    void updateNotification() {
        new Handler(Looper.getMainLooper()).post(() -> {
            Notification notification = createNotification(mediaPlayerService.getCurrentStatus(), mediaPlayerService.getCurrentTrackName());
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, notification);
        });
    }


    private void setupNotificationChannel(){
        String channelName = "music_player-notification-channel";
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName,  NotificationManager.IMPORTANCE_DEFAULT);
        channel.setSound(null, null);
        channel.setShowBadge(false);
        notificationManager.createNotificationChannel(channel);
    }


    private void addPlayButtonTo(NotificationCompat.Builder notification){
        String currentUrl = mediaPlayerService.getCurrentUrl();
        if(currentUrl == null){
            return;
        }
        if(!mediaPlayerService.isPlaying() && !currentUrl.isEmpty()){
            notification.addAction(android.R.drawable.ic_media_play,
                    context.getString(R.string.notification_button_title_play),
                    createPendingIntentFor(ACTION_PLAY));
        }
    }


    private void addStopButtonTo(NotificationCompat.Builder notification){
        if(mediaPlayerService.isPlaying()){
            notification.addAction(android.R.drawable.ic_media_previous,
                    context.getString(R.string.notification_button_title_stop),
                    createPendingIntentFor(ACTION_STOP_PLAYER));
        }
    }

    private void log(String msg){
        System.out.println("^^^ MediaNotificationManager : "+  msg);
    }


    private void addPauseButtonTo(NotificationCompat.Builder notification){
        log("Entered addPauseButtonTo(), mediaService is playing: " + mediaPlayerService.isPlaying());
        if(mediaPlayerService.isPlaying()){
            notification.addAction(android.R.drawable.ic_media_pause,
                    context.getString(R.string.notification_button_title_pause),
                    createPendingIntentFor(ACTION_PAUSE_PLAYER));
        }
    }


    private void addPreviousButtonTo(NotificationCompat.Builder notification){
        if(isThereLessThanTwoStations()) {
            return;
        }
        notification.addAction(android.R.drawable.ic_media_previous,
                context.getString(R.string.notification_button_title_previous),
                createPendingIntentFor(ACTION_SELECT_PREVIOUS_TRACK));
    }


    private void addNextButtonTo(NotificationCompat.Builder notification){
        if(isThereLessThanTwoStations()) {
            return;
        }
        notification.addAction(android.R.drawable.ic_media_next,
                context.getString(R.string.notification_button_title_next),
                createPendingIntentFor(ACTION_SELECT_NEXT_TRACK));
    }


    private boolean isThereLessThanTwoStations(){
        return mediaPlayerService.getTrackCount() < 2;
    }


    private PendingIntent createPendingIntentFor(String action){
        return PendingIntent.getBroadcast(context,
                0,
                new Intent(action),
                PendingIntent.FLAG_IMMUTABLE);
    }


    void dismissNotification(){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
