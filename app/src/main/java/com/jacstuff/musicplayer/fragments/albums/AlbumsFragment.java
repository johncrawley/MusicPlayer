package com.jacstuff.musicplayer.fragments.albums;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.MediaPlayerView;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.db.album.Album;
import com.jacstuff.musicplayer.db.album.AlbumRepository;
import com.jacstuff.musicplayer.db.artist.Artist;

import java.util.List;

public class AlbumsFragment extends Fragment implements MediaPlayerView {

    private RecyclerView recyclerView;
    private AlbumListAdapter listAdapter;
    private int previousIndex = 0;
    private View parentView;
    private AlbumRepository albumsRepository;

    public AlbumsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_albums, container, false);
        albumsRepository = new AlbumRepository(getContext());
        return view;
    }


    @Override
    public void onViewCreated(View view,  Bundle savedInstanceState){
        this.parentView = view;
        recyclerView = parentView.findViewById(R.id.albumsRecyclerView);
        setupLoadTracksButton(parentView);
        refreshList();
    }


    private void setupLoadTracksButton(View parentView){
        Button loadButton = parentView.findViewById(R.id.loadTracksFromAlbumButton);
        loadButton.setOnClickListener((View v) -> {
            Album album = new Album(-1, listAdapter.getCurrentlySelectedItem());
            getMainActivity().loadTracksFromAlbum(album);
            getMainActivity().switchToTracksTab();
        });
    }


    public void notifyCurrentlySelectedTrack(int position){
        getMainActivity().selectTrack(position);
    }


    private MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }


    public void updateList(List<Album> albums, int currentTrackIndex){
        refreshList();
        scrollToListPosition(currentTrackIndex);
    }


    private void refreshList(){
        List<Artist> artists = albumsRepository.getAll();
        if(this.parentView == null ||artists == null){
            return;
        }
        listAdapter = new AlbumListAdapter(artists, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(listAdapter);
    }


    public void scrollToListPosition(int index){
        if(listAdapter == null){
            return;
        }
        listAdapter.selectItemAt(index);
        int calculatedScrollIndex = calculateIndexWithOffset(index);
        recyclerView.smoothScrollToPosition(calculatedScrollIndex);
    }


    private int calculateIndexWithOffset(int index){
        int indexWithOffset = getPlaylistItemOffset(index);
        if ( indexWithOffset > listAdapter.getItemCount() || indexWithOffset < 0) {
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
        listAdapter.notifyDataSetChanged();
    }

}