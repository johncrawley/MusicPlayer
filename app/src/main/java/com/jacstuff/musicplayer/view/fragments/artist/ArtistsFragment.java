package com.jacstuff.musicplayer.view.fragments.artist;

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
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper;
import com.jacstuff.musicplayer.view.fragments.StringListAdapter;

import java.util.ArrayList;
import java.util.List;

public class ArtistsFragment extends Fragment {

    private RecyclerView recyclerView;
    private StringListAdapter listAdapter;
    private View parentView;

    public ArtistsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artists, container, false);
    }


    @Override
    public void onViewCreated(@androidx.annotation.NonNull View view, Bundle savedInstanceState){
        this.parentView = view;
        recyclerView = parentView.findViewById(R.id.artistsRecyclerView);
        refreshArtistsList();
        setupFragmentListener();
    }


    @SuppressLint("NotifyDataSetChanged")
    private void setupFragmentListener(){

        getParentFragmentManager().setFragmentResultListener(MainActivity.SEND_ARTISTS_TO_FRAGMENT, this, (requestKey, bundle) -> {
            ArrayList<String> artistNames =  bundle.getStringArrayList(MainActivity.BUNDLE_KEY_ARTIST_UPDATES);
            listAdapter.setItems(artistNames);
            listAdapter.notifyDataSetChanged();
        });

        getParentFragmentManager().setFragmentResultListener(ArtistOptionsFragment.NOTIFY_ARTISTS_FRAGMENT_TO_LOAD_ARTIST,
                this,
                (requestKey, bundle) -> listAdapter.selectLongClickItem());
    }


    private MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }


    private void refreshArtistsList(){
        List<String> artists = getMainActivity().getArtistNames();
        if(this.parentView == null ||artists == null){
            return;
        }
        listAdapter = new StringListAdapter(artists, this::loadTracksAndAlbumsFromArtist, this::showOptionsDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(listAdapter);
    }


    private void loadTracksAndAlbumsFromArtist(String artistName){
        getMainActivity().loadTracksFromArtist(artistName);
    }


    private void showOptionsDialog(String artistName){
        Bundle bundle = new Bundle();
        bundle.putString(ArtistOptionsFragment.ARTIST_NAME_BUNDLE_KEY, artistName);
        FragmentManagerHelper.showOptionsDialog(this, ArtistOptionsFragment.newInstance(), "artist_options", bundle);
    }

}