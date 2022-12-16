package com.jacstuff.musicplayer.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jacstuff.musicplayer.ListNotifier;
import com.jacstuff.musicplayer.ListSubscriber;
import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.MediaPlayerView;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.db.track.Track;
import com.jacstuff.musicplayer.list.TrackListAdapter;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PlayerFragment extends Fragment implements MediaPlayerView, ListSubscriber {

    private Context context;
//    private MainActivity mainActivity;
    private RecyclerView recyclerView;
    private TrackListAdapter trackListAdapter;
    private int previousIndex = 0;
    ImageView coverArt;
    private List<Track> tracks;
    private View parentView;
    private String address;
    private ListNotifier listNotifier;

    public PlayerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getContext();
        createTimestamp();
        log("Entered onCreateView()");
        listNotifier = getMainActivity().getListNotifier();
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        return view;
    }


    @Override
    public void onViewCreated(View view,  Bundle savedInstanceState){
        log("Entered onViewCreated()");
       // setupRecyclerView(tracks, view);
        getMainActivity().onFragmentsReady();
        boolean isViewNull = view == null;
        log("onViewCreated is view null: " + isViewNull);
        this.parentView = view;
        recyclerView = parentView.findViewById(R.id.recyclerView);
        listNotifier.registerSubscriber(this);
        boolean isRecyclerViewNull = recyclerView == null;
        log("Is recyclerView null? " + isRecyclerViewNull);
    }



    private void log(String msg){
        System.out.println("^^^ PlayerFragment: " + timestamp + " : " + address + " : " + msg);
    }

    private long timestamp;

    private void createTimestamp(){
        timestamp = System.currentTimeMillis();
    }

    public void onServiceReady(List<Track> tracks){
        log("Entered onServiceReady()");
        this.tracks = tracks;
        View parentView = getView();
        if(parentView == null){
            log("parentView is null, returning from onServiceReady()");
            boolean isParentViewFieldNull = this.parentView == null;
            log(" is parentView field null : " + isParentViewFieldNull);
            return;
        }
        log("onServiceReady() about to setup recyclerView");
    }




    public void notifyCurrentlySelectedTrack(int position){
        getMainActivity().selectTrack(position);
    }


    public void setCoverImage(Bitmap bitmap){
        coverArt.setImageBitmap(bitmap);
    }


    private MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }


    public void updateTracksList(List<Track> updatedTracks, int currentTrackIndex){
        refreshTrackList(updatedTracks, parentView);
        scrollToListPosition(currentTrackIndex);
    }


    public void refreshTrackList(List<Track> trackDetailsList, View parentView){
        log("Entered refreshTrackList()");
        setupRecyclerView(trackDetailsList, parentView);
    }


    private void setupRecyclerView(List<Track> tracks, View parentView){
        log("Entered setupRecyclerView()");
        if(this.parentView == null){
            boolean isTracksNull = tracks == null;
            log("parent view is null, carrying on");
        }
        if(tracks == null){
            log("tracks are null, returning");
            return;
        }
        //recyclerView = this.parentView.findViewById(R.id.recyclerView);
        trackListAdapter = new TrackListAdapter(tracks, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(trackListAdapter);
    }


    public void scrollToListPosition(int index){
        if(trackListAdapter == null){
            return;
        }
        trackListAdapter.deselectCurrentlySelectedItem();
        trackListAdapter.setIndexToScrollTo(index);
        trackListAdapter.changePositionTo(index);
        recyclerView.scrollToPosition(calculateIndexWithOffset(index));
    }


    private int calculateIndexWithOffset(int index){
        int indexWithOffset = getPlaylistItemOffset(index);
        if ( indexWithOffset > trackListAdapter.getItemCount() || indexWithOffset < 0) {
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


    @Override
    public void notifyListUpdated() {
        if(getView() == null){
            log("notifyListUpdated() getView() is null!");
            return;
        }
        setupRecyclerView(listNotifier.getList(), getView());
    }
}