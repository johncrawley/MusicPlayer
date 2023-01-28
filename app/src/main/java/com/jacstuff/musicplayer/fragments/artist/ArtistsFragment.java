package com.jacstuff.musicplayer.fragments.artist;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.MediaPlayerView;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.db.artist.Artist;
import com.jacstuff.musicplayer.db.artist.ArtistRepository;
import com.jacstuff.musicplayer.utils.ButtonMaker;

import java.util.List;

public class ArtistsFragment extends Fragment implements MediaPlayerView {

    private RecyclerView recyclerView;
    private ArtistListAdapter artistListAdapter;
    private int previousIndex = 0;
    private View parentView;
    private ArtistRepository artistRepository;

    public ArtistsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artists, container, false);
        artistRepository = new ArtistRepository(getContext());
        return view;
    }


    @Override
    public void onViewCreated(View view,  Bundle savedInstanceState){
        this.parentView = view;
        recyclerView = parentView.findViewById(R.id.artistsRecyclerView);
        setupButtons(parentView);
        refreshArtistsList();
    }


    private void setupButtons(View parentView){
        ButtonMaker.createButton(parentView, R.id.loadTracksFromArtistButton, ()->{
            getMainActivity().loadTracksFromArtist(getSelectedArtist());
            getMainActivity().switchToTracksTab();
        });

        ButtonMaker.createButton(parentView, R.id.addTracksFromArtistToPlaylistButton, ()->
            getMainActivity().getMediaPlayerService().addTracksFromAristToCurrentPlaylist(getSelectedArtist()));
    }


    private Artist getSelectedArtist(){
        return artistListAdapter.getCurrentlySelectedItem();
    }


    public void notifyCurrentlySelectedTrack(int position){
        getMainActivity().selectTrack(position);
    }


    private MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }


    public void updateArtistsList(List<Artist> artists, int currentTrackIndex){
        refreshArtistsList();
        scrollToListPosition(currentTrackIndex);
    }


    private void refreshArtistsList(){
        List<Artist> artists = artistRepository.getAllArtists();
        if(this.parentView == null ||artists == null){
            return;
        }
        artistListAdapter = new ArtistListAdapter(artists, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(artistListAdapter);
    }


    public void scrollToListPosition(int index){
        if(artistListAdapter == null){
            return;
        }
        artistListAdapter.selectItemAt(index);
        int calculatedScrollIndex = calculateIndexWithOffset(index);
        recyclerView.smoothScrollToPosition(calculatedScrollIndex);
    }


    private int calculateIndexWithOffset(int index){
        int indexWithOffset = getPlaylistItemOffset(index);
        if ( indexWithOffset > artistListAdapter.getItemCount() || indexWithOffset < 0) {
            indexWithOffset = index;
        }
        previousIndex = index;
        return indexWithOffset;
    }


    private int getPlaylistItemOffset(int index){
        if(previousIndex == 0){
            return index;
        }
        int direction = index > previousIndex ? 1 : -1;
        int offset =  getResources().getInteger(R.integer.playlist_item_offset) * direction ;
        return index + offset;
    }


    @SuppressLint("NotifyDataSetChanged")
    private void updateTrackViews(){
        artistListAdapter.notifyDataSetChanged();
    }

}