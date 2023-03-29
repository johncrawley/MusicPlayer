package com.jacstuff.musicplayer.view.fragments.tracks;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.db.track.Track;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TracksFragment extends Fragment{

    private RecyclerView recyclerView;
    private TrackListAdapter trackListAdapter;
    private int previousIndex = 0;
    private View parentView;

    public TracksFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tracks, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
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


    public void deselectCurrentItemAndNotify(){
        if(trackListAdapter != null){
            trackListAdapter.deselectCurrentlySelectedItemAndNotify();
        }
    }


    private MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }


    public void updateTracksList(List<Track> updatedTracks, int currentTrackIndex){
        previousIndex = 0;
        if(currentTrackIndex < 0){
            setupRecyclerView(updatedTracks);
            return;
        }
        refreshTrackList(updatedTracks);
        scrollToAndSelectListPosition(currentTrackIndex);
    }


    @SuppressWarnings("notifyDataSetChanged")
    public void refreshTrackList(List<Track> tracks){
        trackListAdapter.setItems(tracks);
        trackListAdapter.notifyDataSetChanged();
    }


    public void setupRecyclerView(List<Track> tracks){
        if(this.parentView == null ||tracks == null){
            return;
        }
        trackListAdapter = new TrackListAdapter(tracks, this::selectTrack, this::createTrackOptionsFragment );
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(trackListAdapter);
    }


    private void createTrackOptionsFragment(Track track){
        String tag = "track_options_dialog";
        MainActivity mainActivity = getMainActivity();
        if(mainActivity == null){
            return;
        }
        mainActivity.setSelectedTrack(track);
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        removePreviousFragmentTransaction(tag, fragmentTransaction);
        TrackOptionsDialog trackOptionsDialog = TrackOptionsDialog.newInstance();
        trackOptionsDialog.show(fragmentTransaction, tag);
    }


    private void removePreviousFragmentTransaction(String tag, FragmentTransaction fragmentTransaction){
        Fragment prev = getParentFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
    }


    public void selectTrack(Track track){
        int position = track.getIndex();
        getMainActivity().selectTrack(position);
    }


    public void scrollToAndSelectListPosition(int index){
        if(trackListAdapter == null){
            return;
        }
        trackListAdapter.selectItemAt(index);
        //could use: smoothScrollToPosition(calculatedScrollIndex)
        // but it would take too long for large list
        recyclerView.scrollToPosition(calculateIndexWithOffset(index));
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
        int offset = index > previousIndex ? 4 : -4;
        return index + offset;
    }

}