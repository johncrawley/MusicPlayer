package com.jacstuff.musicplayer.view.player;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.FragmentTransaction;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.MediaPlayerService;
import com.jacstuff.musicplayer.service.db.track.Track;
import com.jacstuff.musicplayer.view.fragments.options.StopOptionsFragment;
import com.jacstuff.musicplayer.view.utils.FragmentHelper;
import com.jacstuff.musicplayer.view.utils.TimeConverter;

import java.util.Arrays;
import java.util.List;

public class PlayerViewHelper {


    private TextView trackTime, trackTitle, trackAlbum, trackArtist;
    private ImageButton playButton, pauseButton, stopButton, nextTrackButton, previousTrackButton, turnShuffleOnButton, turnShuffleOffButton;
    private SeekBar trackTimeSeekBar;
    private boolean isTrackTimeSeekBarHeld = false;
    private String totalTrackTime = "0:00";
    private ViewGroup playerButtonPanel;

    private final MainActivity mainActivity;
    private MediaPlayerService mediaPlayerService;


    public PlayerViewHelper(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }


    public void setMediaPlayerService(MediaPlayerService mediaPlayerService){
        this.mediaPlayerService = mediaPlayerService;
        setupShuffleButtons(mediaPlayerService);
    }


    public void setupViews(){
        setupPlayerButtonPanelViews();
        initTrackDetailViews();
        setupTrackTimeSeekBar();
        resetElapsedTime();
    }


    public void playTrack() {
        mediaPlayerService.playTrack();
    }


    public void pauseTrack() {
        mainActivity.disableViewForAWhile(playButton, 300);
        mediaPlayerService.pause();
    }


    public void previousTrack(){
        mainActivity.disableViewForAWhile(previousTrackButton);
        mediaPlayerService.loadPreviousTrack();
    }


    public void nextTrack(){
        mainActivity.disableViewForAWhile(nextTrackButton);
        mediaPlayerService.loadNextTrack();
    }



    public void updateViews(int numberOfTracks, boolean isCurrentTrackNull){
        if(numberOfTracks == 0 && isCurrentTrackNull){
            setVisibilityOnPlayerViews(View.INVISIBLE);
            return;
        }
        setVisibilityOnPlayerViews(View.VISIBLE);
        setSeekAndShuffleButtonsVisibility(numberOfTracks);
        setPlayPauseAndTrackSeekBarVisibility();
    }


    public void setSeekAndShuffleButtonsVisibility(int numberOfTracks){
        if(numberOfTracks < 2){
            nextTrackButton.setVisibility(View.INVISIBLE);
            previousTrackButton.setVisibility(View.INVISIBLE);
            turnShuffleOffButton.setVisibility(View.GONE);
            turnShuffleOnButton.setVisibility(View.GONE);
        }
        else{
            nextTrackButton.setVisibility(View.VISIBLE);
            previousTrackButton.setVisibility(View.VISIBLE);
            setShuffleButtonsVisibility();
        }
    }


    public void resetElapsedTime(){
        setElapsedTime("0:00");
    }


    public void setTrackDetails(final Track track, int elapsedTime){
        mainActivity.runOnUiThread(()-> {
            playerButtonPanel.setVisibility(View.VISIBLE);
            String titleText = track.getTitle();
            trackTitle.setText(titleText.isEmpty()? mainActivity.getString(R.string.no_tracks_found) : titleText);
            trackAlbum.setText(track.getAlbum());
            trackArtist.setText(track.getArtist());
            setTrackTimeInfo(elapsedTime, track.getDuration());
            trackTimeSeekBar.setProgress(elapsedTime);
        });
    }


    public void setElapsedTime(long elapsedMilliseconds){
        setElapsedTime(TimeConverter.convert(elapsedMilliseconds));
        mainActivity.runOnUiThread(()->{
            if(!isTrackTimeSeekBarHeld){
                trackTimeSeekBar.setProgress((int)elapsedMilliseconds);
            }
        });
    }


    public void setElapsedTime(String elapsedTime){
        if(!isTrackTimeSeekBarHeld){
            setElapsedTimeOnView(elapsedTime);
        }
    }


    private void setElapsedTimeOnView(String elapsedTime){
        mainActivity.runOnUiThread(()->{
            if(trackTime != null){
                String time = elapsedTime + " / " + totalTrackTime;
                trackTime.setText(time);
            }
        });
    }


    private void setupTrackTimeSeekBar(){
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
        playerButtonPanel = mainActivity.findViewById(R.id.buttonLayout);
        previousTrackButton = setupImageButton(R.id.previousTrackButton, this::previousTrack);
        nextTrackButton     = setupImageButton(R.id.nextTrackButton, this::nextTrack);
        playButton  = setupImageButton(R.id.playButton, this::playTrack);
        pauseButton = setupImageButton(R.id.pauseButton, this::pauseTrack);
        stopButton  = setupImageButton(R.id.stopButton, mainActivity:: stopTrack);
        setupLongClickListenerOnStopButton();
    }


    private void setupShuffleButtons(MediaPlayerService mediaPlayerService){
        turnShuffleOnButton =  setupImageButton(R.id.turnShuffleOnButton, mediaPlayerService::enableShuffle);
        turnShuffleOffButton = setupImageButton(R.id.turnShuffleOffButton, mediaPlayerService::disableShuffle);
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


    private void setPlayPauseAndTrackSeekBarVisibility(){
        if(mediaPlayerService.isPlaying()){
            playButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
            trackTimeSeekBar.setVisibility(View.VISIBLE);
            return;
        }
        playButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
        trackTimeSeekBar.setVisibility(View.INVISIBLE);
    }


    private ImageButton setupImageButton(int buttonId, Runnable runnable){
        ImageButton button = mainActivity.findViewById(buttonId);
        button.setOnClickListener((View v)-> runnable.run());
        return button;
    }


    public void setVisibilityOnPlayerViews(int visibility){

        trackTitle.setVisibility(visibility);
        trackAlbum.setVisibility(visibility);
        trackArtist.setVisibility(visibility);
        trackTime.setVisibility(visibility);
        playerButtonPanel.setVisibility(visibility);
    }


    public void setBlankTrackInfo(){
        mainActivity.runOnUiThread(()-> trackTitle.setText(""));
    }


    public void hideTrackSeekBar(){
        trackTimeSeekBar.setVisibility(View.INVISIBLE);
    }


    public void notifyMediaPlayerPlaying(){
        mainActivity.runOnUiThread(()->{
            playButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
            trackTimeSeekBar.setVisibility(View.VISIBLE);
        });
    }


    public void notifyMediaPlayerStopped(){
        mainActivity.runOnUiThread(()->{
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
            trackTimeSeekBar.setProgress(0);
            trackTimeSeekBar.setVisibility(View.INVISIBLE);
        });
    }


    public void notifyMediaPlayerPaused(){
        mainActivity.runOnUiThread(()->{
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
        });
    }


    public void notifyShuffleEnabled(){
        turnShuffleOnButton.setVisibility(View.GONE);
        turnShuffleOffButton.setVisibility(View.VISIBLE);
    }


    public void notifyShuffleDisabled(){
        turnShuffleOnButton.setVisibility(View.VISIBLE);
        turnShuffleOffButton.setVisibility(View.GONE);
    }


    private void setShuffleButtonsVisibility(){
        if(mediaPlayerService.isShuffleEnabled()){
            notifyShuffleEnabled();
            return;
        }
        notifyShuffleDisabled();
    }
}
