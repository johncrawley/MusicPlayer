package com.jacstuff.musicplayer.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.MediaController;
import com.jacstuff.musicplayer.MediaControllerImpl;
import com.jacstuff.musicplayer.MediaPlayerView;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.db.track.Track;
import com.jacstuff.musicplayer.list.TrackListAdapter;
import com.jacstuff.musicplayer.viewmodel.MainViewModel;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PlayerFragment extends Fragment implements MediaPlayerView {

    private Context context;
    private MainViewModel viewModel;
    private MainActivity mainActivity;
    private TextView trackTime;
    private TextView trackTitle, trackAlbum, trackArtist;
    private ImageButton playButton, pauseButton;
    private ImageButton nextTrackButton;
    private String totalTrackTime = "0:00";
    private RecyclerView recyclerView;
    private TrackListAdapter trackListAdapter;
    private int previousIndex = 0;
    ImageView coverArt;
    private Track currentTrack;
    private List<Track> tracks;

    public PlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getContext();
        mainActivity = (MainActivity)getActivity();
        log("entered onCreateView()");
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        // setupKeyAction(view.findViewById(R.id.wholeWordCheckEditText));

        log("onCreateView() About to run setupViews()");
        setupViews(view);
        log("onCreateView() about to init mediaController");

        return view;
    }

    private void log(String msg){
        System.out.println("^^^ PlayerFragment: " +  msg);
    }


    public void onServiceReady(){
        View parentView = getView();
        if(parentView == null){
            return;
        }
        setupRecyclerView(mainActivity.getTrackList(), parentView);
        mainActivity.initPlaylistAndRefresh();
    }


    public void notifyTrackPlaying(){
        log("Entered notifyTrackPlaying()");
        playButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);
        log("notifyTrackPlaying() play button visibility: " + playButton.getVisibility());
        log("notifyTrackPlaying() pause button visibility: " + pauseButton.getVisibility());
    }


    public void notifyTrackPaused(){
        log("entered notifyTrackPaused()");
        playButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
        log("notifyTrackPaused() play button visibility: " + playButton.getVisibility());
        log("notifyTrackPaused() pause button visibility: " + pauseButton.getVisibility());
    }


    public void updateTrackDetails(List<Track> updatedTracks){
        tracks.clear();
        tracks.addAll(updatedTracks);
        getActivity().runOnUiThread(this::updateTrackViews);
    }


    @SuppressLint("NotifyDataSetChanged")
    private void updateTrackViews(){
        trackListAdapter.notifyDataSetChanged();
        if(tracks.isEmpty()){
            setVisibilityOnDetailsAndNavViews(View.INVISIBLE);
            return;
        }
        setVisibilityOnDetailsAndNavViews(View.VISIBLE);
        hideNextButtonIfOnlyOneTrack();
    }


    private void hideNextButtonIfOnlyOneTrack(){
        if(viewModel.tracks.size() == 1){
            nextTrackButton.setVisibility(View.INVISIBLE);
        }
    }


    private void setVisibilityOnDetailsAndNavViews(int visibility){
        trackTitle.setVisibility(visibility);
        trackAlbum.setVisibility(visibility);
        trackArtist.setVisibility(visibility);
        trackTime.setVisibility(visibility);
        playButton.setVisibility(visibility);
        nextTrackButton.setVisibility(visibility);
    }


    public void notifyCurrentlySelectedTrack(int position){
        mainActivity.selectTrack(position);
    }

    @Override
    public void updateTrackDetails() {

    }


    public void setCoverImage(Bitmap bitmap){
        coverArt.setImageBitmap(bitmap);
    }


    @Override
    public void refreshTrackList(List<Track> trackDetailsList) {

    }


    public void setElapsedTime(String elapsedTime){
        this.setTrackTime(elapsedTime);
    }


    private void resetElapsedTime(){
        setElapsedTime("0:00");
    }


    public void setTotalTrackTime(String totalTrackTime){
        this.totalTrackTime = totalTrackTime;
        resetElapsedTime();
    }


    private void setTrackTime(String elapsedTime){
        if(trackTime != null){
            String time = elapsedTime + " / " + this.totalTrackTime;
            trackTime.setText(time);
        }
    }


    public void displayPlaylistRefreshedMessage(){
        String msg = getResources().getString(R.string.playlist_refreshed_message);
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }


    public void displayPlaylistRefreshedMessage(int newTrackCount) {
        new Handler(Looper.getMainLooper()).post(() -> displayPlaylistMessage(newTrackCount));
    }


    public void displayPlaylistMessage(int newTrackCount) {
        if(newTrackCount == 0){
            displayPlaylistRefreshedMessage();
            return;
        }
        String msg = newTrackCount > 1 ?
                getResources().getString(R.string.playlist_refreshed_message_new_tracks_count, newTrackCount)
                : getResources().getString(R.string.playlist_refreshed_one_new_track);
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }


    private void setupViews(View parentView){
        assignViews(parentView);
        assignClickListeners();
        resetElapsedTime();
        playButton.setEnabled(false);
        nextTrackButton.setEnabled(false);
    }


    private void assignViews(View parentView){
        trackTime = parentView.findViewById(R.id.trackTime);
        trackTitle = parentView.findViewById(R.id.trackTitle);
        trackAlbum = parentView.findViewById(R.id.albumTextView);
        trackArtist = parentView.findViewById(R.id.artistTextView);
        playButton = parentView.findViewById(R.id.playButton);
        pauseButton = parentView.findViewById(R.id.pauseButton);
        nextTrackButton = parentView.findViewById(R.id.nextTrackButton);
        boolean isTrackTitleNull = trackTitle == null;
        System.out.println("PlayerFragment.assignViews() is Track Title View null? : " + isTrackTitleNull);
    }


    private void assignClickListeners(){
        playButton.setOnClickListener((View v) -> {
            log("current track: "+  currentTrack);
            mainActivity.playTrack();});

        pauseButton.setOnClickListener((View v) -> mainActivity.pauseMediaPlayer());

        nextTrackButton.setOnClickListener((View v) -> {
            mainActivity.nextTrack();
        });
    }


    public void setTrack(Track track){
        this.currentTrack = track;
        //mainActivity.selectTrack(track);
    }



    public void refreshTrackList(List<Track> trackDetailsList, View parentView){
        setupRecyclerView(trackDetailsList, parentView);
    }


    private void setupRecyclerView(List<Track> trackDetailsList, View parentView){
        recyclerView = parentView.findViewById(R.id.recyclerView);
        trackListAdapter = new TrackListAdapter(trackDetailsList, this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(trackListAdapter);
    }


    public void scrollToListPosition(int index){
        log("Entered scrollToListPosition()");
        trackListAdapter.deselectCurrentlySelectedItem();
        trackListAdapter.setIndexToScrollTo(index);
        recyclerView.scrollToPosition(calculateIndexWithOffset(index));
    }


    private int calculateIndexWithOffset(int index){
        int indexWithOffset = getPlaylistItemOffset(index);
        if ( indexWithOffset > mainActivity.getNumberOfTracks() || indexWithOffset < 0) {
            indexWithOffset = index;
        }
        previousIndex = index;
        return indexWithOffset;
    }


    private int getPlaylistItemOffset(int index){
        int direction = index > previousIndex ? 1 : -1;
        int offset =  getResources().getInteger(R.integer.playlist_item_offset) * direction ;
        return index + offset;
    }


    public void enableControls(){
        playButton.setEnabled(true);
        nextTrackButton.setEnabled(true);
    }


    public void setTrackInfo(String trackInfo){
        if(trackInfo.isEmpty()){
            trackInfo = getResources().getString(R.string.no_tracks_found);
        }
        this.trackTitle.setText(trackInfo);
    }


    public void setTrackInfo(Track track){
        setTrackInfo(track.getName());
        setAlbumInfo(track.getAlbum());
        setArtistInfo(track.getArtist());
    }


    public void setAlbumInfo(String albumInfo){
        this.trackAlbum.setText(albumInfo);
    }


    public void setArtistInfo(String artistInfo){
        this.trackArtist.setText(artistInfo);
    }
}