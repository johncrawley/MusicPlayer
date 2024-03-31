package com.jacstuff.musicplayer.view.trackplayer;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


import com.jacstuff.musicplayer.OpenTrackActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.PlayTrackService;
import com.jacstuff.musicplayer.service.db.entities.Track;
import com.jacstuff.musicplayer.service.playtrack.TrackPlayerHelper;
import com.jacstuff.musicplayer.view.utils.TimeConverter;

import java.util.Arrays;
import java.util.List;


public class TrackPlayerViewHelper implements TrackPlayerView{

    private final OpenTrackActivity activity;
    private PlayTrackService playTrackService;


    private TextView trackTime, trackTitle, trackAlbum, trackArtist;
    private ImageButton playButton, pauseButton, stopButton;
    private SeekBar trackTimeSeekBar;
    private boolean isTrackTimeSeekBarHeld = false;
    private String totalTrackTime = "0:00";
    private TrackPlayerHelper trackPlayerHelper;


    public TrackPlayerViewHelper(OpenTrackActivity openTrackActivity){
        activity = openTrackActivity;
    }


    public void setService(PlayTrackService playTrackService){
        this.playTrackService = playTrackService;
        trackPlayerHelper = playTrackService.getTrackPlayerHelper();
        trackPlayerHelper.setTrackPlayerView(this);
    }


    public void setupViews(){
        setupPlayerButtonPanelViews();
        initTrackDetailViews();
        setupTrackTimeSeekBar();
        resetElapsedTime();
    }


    public void playTrack() {
        trackPlayerHelper.playOrResume();
    }


    public void pauseTrack() {
        disableViewForAWhile(playButton, 300);
        trackPlayerHelper.pause();
    }


    public void disableViewForAWhile(View view, int delayTime) {
        view.setEnabled(false);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(()->view.setEnabled(true), delayTime);
    }


    public void stopTrack(){
        resetElapsedTime();
        trackPlayerHelper.stop(true);
    }


    public void resetElapsedTime(){
        setElapsedTime("0:00");
    }


    @Override
    public void setElapsedTime(long elapsedMilliseconds){
        setElapsedTime(TimeConverter.convert(elapsedMilliseconds));
        activity.runOnUiThread(()->{
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
        activity.runOnUiThread(()->{
            if(trackTime != null){
                String time = elapsedTime + "/" + totalTrackTime;
                trackTime.setText(time);
            }
        });
    }


    private void setupTrackTimeSeekBar(){
        trackTimeSeekBar = activity.findViewById(R.id.trackTimeSeekBar);
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
                trackPlayerHelper.seek(progress);
                isTrackTimeSeekBarHeld = false;
                setPlayerControlsEnabled(true);
            }
        });
    }


    private void setPlayerControlsEnabled(boolean isEnabled){
        List<View> controls = Arrays.asList(stopButton, playButton, pauseButton);
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
        trackTime = activity.findViewById(R.id.trackTime);
        trackTitle =  activity.findViewById(R.id.trackTitleText);
        trackAlbum =  activity.findViewById(R.id.albumText);
        trackArtist = activity.findViewById(R.id.artistText);
    }


    private void setupPlayerButtonPanelViews(){
        playButton  = setupImageButton(R.id.playButton, this::playTrack);
        pauseButton = setupImageButton(R.id.pauseButton, this::pauseTrack);
        stopButton  = setupImageButton(R.id.stopButton, this::stopTrack);
    }


    private void setPlayPauseAndTrackSeekBarVisibility(){
        if(trackPlayerHelper.isPlaying()){
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
        ImageButton button = activity.findViewById(buttonId);
        button.setOnClickListener((View v)-> runnable.run());
        return button;
    }


    @Override
    public void updateViewsOnTrackAssigned() {

    }


    public void setBlankTrackInfo(){
        activity.runOnUiThread(()-> trackTitle.setText(""));
    }


    @Override
    public void setBlankTrackInfoOnMainView() {

    }


    @Override
    public void stopUpdatingElapsedTimeOnView() {

    }


    public void hideTrackSeekBar(){
        trackTimeSeekBar.setVisibility(View.INVISIBLE);
    }


    public void notifyMediaPlayerPlaying(){
        activity.runOnUiThread(()->{
            playButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
            trackTimeSeekBar.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void displayError(Track track) {

    }


    @Override
    public void notifyThatFileDoesNotExist(Track track) {

    }


    @Override
    public void setAlbumArt(Bitmap bitmap) {
        if(bitmap == null){
            return;
        }
        activity.runOnUiThread(()-> {
            ImageView albumArtView = activity.findViewById(R.id.albumArtImageView);
            albumArtView.setImageBitmap(bitmap);
            // viewModel.currentAlbumArt = updatedAlbumArt;
        });
    }


    @Override
    public void displayInfoFrom(Track track) {
        activity.runOnUiThread(()-> {
            String titleText = track.getTitle();
            trackTitle.setText(titleText.isEmpty()? activity.getString(R.string.no_tracks_found) : titleText);
            trackAlbum.setText(track.getAlbum());
            trackArtist.setText(track.getArtist());
            setTrackTimeInfo(0, track.getDuration());
            trackTimeSeekBar.setProgress(0);
        });
    }


    @Override
    public void notifyMediaPlayerStopped(){
        activity.runOnUiThread(()->{
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
            trackTimeSeekBar.setProgress(0);
            trackTimeSeekBar.setVisibility(View.INVISIBLE);
        });
    }


    @Override
    public void notifyMediaPlayerPaused(){
        activity.runOnUiThread(()->{
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
        });
    }


}
