package com.jacstuff.musicplayer.fragments.queue;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jacstuff.musicplayer.ListSubscriber;
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

public class TracksFragment extends Fragment implements MediaPlayerView, ListSubscriber {

    private RecyclerView recyclerView;
    private TrackListAdapter trackListAdapter;
    private int previousIndex = 0;
    private View parentView;

    public TracksFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_queue, container, false);
        return view;
    }


    @Override
    public void onViewCreated(View view,  Bundle savedInstanceState){
        this.parentView = view;
        recyclerView = parentView.findViewById(R.id.recyclerView);
        setupRecyclerView(getMainActivity().getTrackList());
        getMainActivity().setPlayerFragment(this);
    }


    public void deselectCurrentItem(){
        if(trackListAdapter != null){
            trackListAdapter.deselectCurrentlySelectedItem();
        }
    }


    public void notifyCurrentlySelectedTrack(int position){
        getMainActivity().selectTrack(position);
    }


    private MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }


    public void updateTracksList(List<Track> updatedTracks, int currentTrackIndex){
        log("Entered updateTrackList()");
        refreshTrackList(updatedTracks);
        scrollToListPosition(currentTrackIndex);
    }


    private void log(String msg){
        System.out.println("^^^ QueueFragment: " + msg);
    }


    public void refreshTrackList(List<Track> tracks){
        setupRecyclerView(tracks);
    }


    private void setupRecyclerView(List<Track> tracks){
        if(this.parentView == null ||tracks == null){
            return;
        }
        trackListAdapter = new TrackListAdapter(tracks);
        trackListAdapter.setMediaPlayerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(trackListAdapter);
    }


    public void scrollToListPosition(int index){
        if(trackListAdapter == null){
            return;
        }
        trackListAdapter.selectItemAt(index);
        int calculatedScrollIndex = calculateIndexWithOffset(index);
        recyclerView.smoothScrollToPosition(calculatedScrollIndex);
    }


    private int calculateIndexWithOffset(int index){
        int indexWithOffset = getItemScrollOffset(index);
        if ( indexWithOffset > trackListAdapter.getItemCount() || indexWithOffset < 0) {
            indexWithOffset = index;
        }
        previousIndex = index;
        return indexWithOffset;
    }


    private int getItemScrollOffset(int index){
        if(previousIndex == 0){
            return index;
        }
        int direction = index > previousIndex ? 1 : -1;
        MainActivity mainActivity = getMainActivity();
        if(mainActivity == null){
            return 0;
        }
        int offset =  mainActivity.getResources().getInteger(R.integer.playlist_item_offset) * direction ;
        return index + offset;
    }


    @SuppressLint("NotifyDataSetChanged")
    private void updateTrackViews(){
        trackListAdapter.notifyDataSetChanged();
    }


    @Override
    public void notifyListUpdated() {
        if(getView() == null){
            return;
        }
    }
}