package com.jacstuff.musicplayer.fragments.tracks;

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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TracksFragment extends Fragment implements MediaPlayerView, ListSubscriber {

    private RecyclerView recyclerView;
    private TrackListAdapter trackListAdapter;
    private int previousIndex = 0;
    private View parentView;
    public final static String BUNDLE_TRACK_ID = "Track_ID";

    public TracksFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracks, container, false);
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


    private MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }


    public void updateTracksList(List<Track> updatedTracks, int currentTrackIndex){
        refreshTrackList(updatedTracks);
        scrollToListPosition(currentTrackIndex);
    }


    @SuppressWarnings("notifyDataSetChanged")
    public void refreshTrackList(List<Track> tracks){
        trackListAdapter.setItems(tracks);
        trackListAdapter.notifyDataSetChanged();
    }


    private void setupRecyclerView(List<Track> tracks){
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


    public void notifyCurrentlySelectedTrack(int position){
        getMainActivity().selectTrack(position);
    }


    public void selectTrack(Track track){
        int position = track.getIndex();
        getMainActivity().selectTrack(position);
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


    @Override
    public void notifyListUpdated() {
        if(getView() == null){
            return;
        }
    }
}