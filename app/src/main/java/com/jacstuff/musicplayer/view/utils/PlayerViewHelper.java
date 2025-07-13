package com.jacstuff.musicplayer.view.utils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.FragmentTransaction;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.MediaPlayerService;
import com.jacstuff.musicplayer.service.db.entities.Track;
import com.jacstuff.musicplayer.service.helpers.MediaPlayerHelper;
import com.jacstuff.musicplayer.view.fragments.options.StopOptionsFragment;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PlayerViewHelper {

    private TextView trackTime, trackTitle, trackAlbum, trackArtist;
    private ImageButton playButton, pauseButton, stopButton, nextTrackButton, previousTrackButton, turnShuffleOnButton, turnShuffleOffButton;
    private SeekBar trackTimeSeekBar;
    private boolean isTrackTimeSeekBarHeld = false;
    private String totalTrackTime = "0:00";
    private ViewGroup playerButtonPanel;
    private MainActivity mainActivity;
    private MediaPlayerService mediaPlayerService;
    public enum MediaPlayerNotification {
        MEDIA_PLAYER_PLAYING, MEDIA_PLAYER_STOPPED, MEDIA_PLAYER_PAUSED, SHUFFLE_ENABLED, SHUFFLE_DISABLED}


    public PlayerViewHelper(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        setupViews();
    }


    public void onDestroy(){
        mainActivity = null;
    }


    public void setMediaPlayerService(MediaPlayerService mediaPlayerService){
        this.mediaPlayerService = mediaPlayerService;
        setupShuffleButtons();
    }



    private void setupViews(){
        setupPlayerButtonPanelViews();
        initTrackDetailViews();
        initTrackTimeSeekBar();
        resetElapsedTime();
    }


    public void playTrack() {
       var mediaPlayerHelper = mediaPlayerService.getMediaPlayerHelper();
       if(mediaPlayerHelper != null){
           mediaPlayerHelper.playTrack();
       }
    }


    public void pauseTrack() {
        if(mainActivity == null){
            return;
        }
        mainActivity.disableViewForAWhile(playButton, 300);
        mediaPlayerService.pause();
    }


    public void previousTrack(){
        if(mainActivity == null){
            return;
        }
        mainActivity.disableViewForAWhile(previousTrackButton);
        mediaPlayerService.loadPreviousTrack();
    }


    public void nextTrack(){
        if(mainActivity == null){
            return;
        }
        mainActivity.disableViewForAWhile(nextTrackButton);
        mediaPlayerService.loadNextTrack();
    }


    public void updateViews(int numberOfTracks, boolean isCurrentTrackNull){
        if(mainActivity == null){
            return;
        }
        if(numberOfTracks == 0 && isCurrentTrackNull){
            setVisibilityOnPlayerViews(View.INVISIBLE);
            return;
        }
        setVisibilityOnPlayerViews(VISIBLE);
        setSeekAndShuffleButtonsVisibility(numberOfTracks);
        setPlayPauseVisibility();
        setSeekBarVisibility();
    }


    private void setSeekAndShuffleButtonsVisibility(int numberOfTracks){
        if(numberOfTracks < 2){
            nextTrackButton.setVisibility(View.INVISIBLE);
            previousTrackButton.setVisibility(View.INVISIBLE);
            turnShuffleOffButton.setVisibility(GONE);
            turnShuffleOnButton.setVisibility(GONE);
        }
        else{
            nextTrackButton.setVisibility(VISIBLE);
            previousTrackButton.setVisibility(VISIBLE);
            setShuffleButtonsVisibility();
        }
    }


    public void resetElapsedTime(){
        setElapsedTime("0:00");
    }


    public void setTrackDetails(final Track track, int elapsedTime){
        if(mainActivity == null){
            return;
        }
        mainActivity.runOnUiThread(()-> {
            playerButtonPanel.setVisibility(VISIBLE);
            String titleText = track.getTitle();
            trackTitle.setText(titleText.isEmpty()? mainActivity.getString(R.string.no_tracks_found) : titleText);
            trackAlbum.setText(track.getAlbum());
            setVisibilityOnAlbumText(track);
            trackArtist.setText(track.getArtist());
            setTrackTimeInfo(elapsedTime, track.getDuration());
            trackTimeSeekBar.setProgress(elapsedTime);
        });
    }


    private void setVisibilityOnAlbumText(Track track){
        boolean isAlbumAndArtistTheSame = track.getAlbum().equals(track.getArtist());
        trackAlbum.setVisibility(isAlbumAndArtistTheSame ? GONE : VISIBLE);
    }


    public void setElapsedTime(long elapsedMilliseconds){
        if(mainActivity == null){
            return;
        }
        setElapsedTime(TimeConverter.convert(elapsedMilliseconds));
        mainActivity.runOnUiThread(()->{
            if(!isTrackTimeSeekBarHeld){
                trackTimeSeekBar.setProgress((int)elapsedMilliseconds);
            }
        });
    }


    public void setElapsedTime(String elapsedTime){
        if(mainActivity == null){
            return;
        }
        if(!isTrackTimeSeekBarHeld){
            setElapsedTimeOnView(elapsedTime);
        }
    }


    private void setElapsedTimeOnView(String elapsedTime){
        mainActivity.runOnUiThread(()->{
            if(trackTime != null){
                String time = elapsedTime + "/" + totalTrackTime;
                trackTime.setText(time);
            }
        });
    }


    private void initTrackTimeSeekBar(){
        trackTimeSeekBar = mainActivity.findViewById(R.id.trackTimeSeekBar);
        trackTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(isTrackTimeSeekBarHeld) {
                    setElapsedTimeOnView(TimeConverter.convert(seekBar.getProgress()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTrackTimeSeekBarHeld = true;
                setPlayerControlsEnabled(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = Math.min(seekBar.getMax() - 500, seekBar.getProgress());
                mainActivity.getMediaPlayerService().seek(progress);
                isTrackTimeSeekBarHeld = false;
                setPlayerControlsEnabled(true);
            }
        });

    }


    private void setPlayerControlsEnabled(boolean isEnabled){
        List<View> controls = Arrays.asList(stopButton, playButton, pauseButton, nextTrackButton, previousTrackButton, turnShuffleOnButton, turnShuffleOffButton);
        for(View control : controls){
            control.setEnabled(isEnabled);
        }
    }


    private void setTrackTimeInfo(int elapsedTime, long trackDuration){
        this.totalTrackTime = TimeConverter.convert(trackDuration);
        trackTimeSeekBar.setMax((int)trackDuration);
        setElapsedTime(TimeConverter.convert(elapsedTime));
    }


    private void initTrackDetailViews(){
        trackTime = mainActivity.findViewById(R.id.trackTime);
        trackTitle =  mainActivity.findViewById(R.id.trackTitle);
        trackAlbum =  mainActivity.findViewById(R.id.albumTextView);
        trackArtist = mainActivity.findViewById(R.id.artistTextView);
    }


    private void setupPlayerButtonPanelViews(){
        playerButtonPanel = mainActivity.findViewById(R.id.playerButtonsInclude);
        previousTrackButton = setupImageButton(R.id.previousTrackButton, this::previousTrack);
        nextTrackButton     = setupImageButton(R.id.nextTrackButton, this::nextTrack);
        playButton  = setupImageButton(R.id.playButton, this::playTrack);
        pauseButton = setupImageButton(R.id.pauseButton, this::pauseTrack);
        stopButton  = setupImageButton(R.id.stopButton, mainActivity:: stopTrack);
        setupLongClickListenerOnStopButton();
    }


    private void setupShuffleButtons(){
        turnShuffleOnButton =  setupImageButton(R.id.turnShuffleOnButton, this::enableShuffle);
        turnShuffleOffButton = setupImageButton(R.id.turnShuffleOffButton, this::disableShuffle);
    }


    private void enableShuffle(){
        mediaPlayerService.enableShuffle();
        setShuffleState(true);
    }


    private void disableShuffle(){
        mediaPlayerService.disableShuffle();
        setShuffleState(false);
    }

    public void notifyNumberOfTracks(int numberOfTracks){
        if(mainActivity != null
                && turnShuffleOnButton  != null
                && turnShuffleOffButton != null
                && numberOfTracks < 2){
            turnShuffleOffButton.setVisibility(View.INVISIBLE);
            turnShuffleOnButton.setVisibility(View.INVISIBLE);
        }
    }


    private void setupLongClickListenerOnStopButton(){
        stopButton.setOnLongClickListener((View v)->{
            if(mediaPlayerService.isPlaying()){
                createStopOptionsFragment();
            }
            return true;
        });
    }


    private void createStopOptionsFragment(){
        String tag = "stop_options_dialog";
        FragmentTransaction fragmentTransaction = FragmentHelper.createTransaction(mainActivity, tag);
        StopOptionsFragment.newInstance().show(fragmentTransaction, tag);
    }


    private void setPlayPauseVisibility(){
        if(mediaPlayerService.isPlaying()){
            playButton.setVisibility(GONE);
            pauseButton.setVisibility(VISIBLE);
            return;
        }
        playButton.setVisibility(VISIBLE);
        pauseButton.setVisibility(GONE);
    }

    private void setSeekBarVisibility(){
        getMediaPlayerHelper().ifPresent(mph -> {
            if(mph.isPaused() || mph.isPlaying()){
                trackTimeSeekBar.setVisibility(VISIBLE);
                setElapsedTime(mph.getElapsedTime());
                return;
            }
            trackTimeSeekBar.setVisibility(View.INVISIBLE);

        });
    }


    private Optional<MediaPlayerHelper> getMediaPlayerHelper(){
        if(mediaPlayerService == null){
            return Optional.empty();
        }
        return Optional.ofNullable(mediaPlayerService.getMediaPlayerHelper());
    }


    private ImageButton setupImageButton(int buttonId, Runnable runnable){
        ImageButton button = mainActivity.findViewById(buttonId);
        button.setOnClickListener((View v)-> runnable.run());
        return button;
    }


    public void setVisibilityOnPlayerViews(int visibility){
        if(mainActivity == null){
            return;
        }
        mainActivity.runOnUiThread(()->{
            trackTitle.setVisibility(visibility);
            trackAlbum.setVisibility(visibility);
            trackArtist.setVisibility(visibility);
            trackTime.setVisibility(visibility);
            playerButtonPanel.setVisibility(visibility);
        });
    }


    public void setBlankTrackInfo(){
        if(mainActivity == null){
            return;
        }
        mainActivity.runOnUiThread(()-> trackTitle.setText(""));
    }


    public void hideTrackSeekBar(){
        if(mainActivity == null){
            return;
        }
        trackTimeSeekBar.setVisibility(View.INVISIBLE);
    }


    public void notify(MediaPlayerNotification notification){
        if(mainActivity == null){
            return;
        }
        switch (notification){
            case SHUFFLE_ENABLED -> notifyShuffleEnabled();
            case SHUFFLE_DISABLED -> notifyShuffleDisabled();
            case MEDIA_PLAYER_PLAYING -> notifyMediaPlayerPlaying();
            case MEDIA_PLAYER_PAUSED -> notifyMediaPlayerPaused();
            case MEDIA_PLAYER_STOPPED -> notifyMediaPlayerStopped();
        }
    }


    private void notifyMediaPlayerPlaying(){
        mainActivity.runOnUiThread(()->{
            playButton.setVisibility(GONE);
            pauseButton.setVisibility(VISIBLE);
            trackTimeSeekBar.setVisibility(VISIBLE);
        });
    }


    private void notifyMediaPlayerStopped(){
        mainActivity.runOnUiThread(()->{
            playButton.setVisibility(VISIBLE);
            pauseButton.setVisibility(GONE);
            trackTimeSeekBar.setProgress(0);
            trackTimeSeekBar.setVisibility(View.INVISIBLE);
        });
    }


    private void notifyMediaPlayerPaused(){
        mainActivity.runOnUiThread(()->{
            playButton.setVisibility(VISIBLE);
            pauseButton.setVisibility(GONE);
        });
    }


    private void notifyShuffleEnabled(){
        turnShuffleOnButton.setVisibility(GONE);
        turnShuffleOffButton.setVisibility(VISIBLE);
    }


    public void setShuffleState(boolean isEnabled){
        if(mainActivity == null){
            return;
        }
        mainActivity.runOnUiThread(()->{
            turnShuffleOnButton.setVisibility(isEnabled ? GONE : VISIBLE);
            turnShuffleOffButton.setVisibility(isEnabled ? VISIBLE : GONE);
        });
    }


    private void notifyShuffleDisabled(){
        turnShuffleOnButton.setVisibility(VISIBLE);
        turnShuffleOffButton.setVisibility(GONE);
    }


    private void setShuffleButtonsVisibility(){
        if(mediaPlayerService.isShuffleEnabled()){
            notifyShuffleEnabled();
            return;
        }
        notifyShuffleDisabled();
    }
}
