package com.jacstuff.musicplayer.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.MediaPlayerView;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.db.track.Track;
import com.jacstuff.musicplayer.list.TrackListAdapter;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PlayerFragment extends Fragment implements MediaPlayerView{

    private Context context;
    private MainActivity mainActivity;
    private RecyclerView recyclerView;
    private TrackListAdapter trackListAdapter;
    private int previousIndex = 0;
    ImageView coverArt;

    public PlayerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getContext();
        mainActivity = (MainActivity)getActivity();
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        return view;
    }


    public void onServiceReady(){
        View parentView = getView();
        if(parentView == null){
            return;
        }
        setupRecyclerView(mainActivity.getTrackList(), parentView);
        mainActivity.initPlaylistAndRefresh();
    }


    public void notifyCurrentlySelectedTrack(int position){
        mainActivity.selectTrack(position);
    }


    public void setCoverImage(Bitmap bitmap){
        coverArt.setImageBitmap(bitmap);
    }



    public void updateTracksList(List<Track> updatedTracks, int currentTrackIndex){
        mainActivity.runOnUiThread(()->{
            mainActivity.displayPlaylistRefreshedMessage();
            refreshTrackList(updatedTracks, getView());
            scrollToListPosition(currentTrackIndex);
        });
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


    @SuppressLint("NotifyDataSetChanged")
    private void updateTrackViews(){
        trackListAdapter.notifyDataSetChanged();
    }


}