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
import android.graphics.Bitmap;
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

public class CustomNotificationManager {
    private final Context context;
    private final MediaPlayerService mediaPlayerService;
    public final static int NOTIFICATION_ID = 1001;
    private PendingIntent pendingIntent;
    final static String NOTIFICATION_CHANNEL_ID = "com.jcrawley.musicplayer-notification";
    private final AtomicBoolean hasErrorNotificationBeenReplaced = new AtomicBoolean(false);
    RemoteViews notificationLayout, notificationLayoutExpanded;
    private TextView expandedTitle;

    public CustomNotificationManager(Context context, MediaPlayerService mediaPlayerService){
        this.context = context;
        this.mediaPlayerService = mediaPlayerService;

        // Get the layouts to use in the custom notification
        notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification_small);
        notificationLayoutExpanded = new RemoteViews(context.getPackageName(), R.layout.notification_large);
        View expandedLayout = notificationLayoutExpanded.apply(context, null);
        expandedTitle = expandedLayout.findViewById(R.id.notification_song_title);

    }

    public Notification createCustomNotification(String heading, String channelName) {

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


    public void updateAlbumArt(Bitmap bitmap){

    }


    public void updateTimeElapsed(int timeElapsed){

    }


    public void updateVisibilityOnTrackNavigation(int numberOfTracks){

    }


    public void hidePauseButtonOnPlay(){

    }


    public void hidePlayButtonOnPause(){

    }


    public void updateStatusOnError(){

    }


    public void resetErrorStatus(){

    }


    private void setupButtonIntent(int buttonId, String action){
        Intent intent = new Intent(action);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        notificationLayoutExpanded.setOnClickPendingIntent(buttonId, pendingSwitchIntent);
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
        new Handler(Looper.getMainLooper()).post(() ->
                sendNotification(mediaPlayerService.getCurrentStatus(), mediaPlayerService.getCurrentTrack()));
    }


    private void resetErrorStatusAfterDelay(){
        int numberOfMillisecondsBeforeResettingStatus = 9_000;
        hasErrorNotificationBeenReplaced.set(true);
        if(!mediaPlayerService.hasEncounteredError()){
            return;
        }else{
            hasErrorNotificationBeenReplaced.set(false);
        }
        String status = mediaPlayerService.getReadyStatusStr();
        Track track = mediaPlayerService.getCurrentTrack();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if(!hasErrorNotificationBeenReplaced.get()){
                sendNotification(status, track);
                hasErrorNotificationBeenReplaced.set(true);
            }
        }, numberOfMillisecondsBeforeResettingStatus);
    }


    private void sendNotification( String status, Track track){
        String trackInfo = parseTrackDetails(track);
        Notification notification = createCustomNotification(status, trackInfo);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
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
        String channelName = "music_player-notification-channel";
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName,  NotificationManager.IMPORTANCE_DEFAULT);
        channel.setSound(null, null);
        channel.setShowBadge(false);
        notificationManager.createNotificationChannel(channel);
    }

}
