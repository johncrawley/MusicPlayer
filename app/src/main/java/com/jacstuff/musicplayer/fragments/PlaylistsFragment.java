package com.jacstuff.musicplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.db.playlist.Playlist;
import com.jacstuff.musicplayer.db.playlist.PlaylistRepository;
import com.jacstuff.musicplayer.db.playlist.PlaylistRepositoryImpl;
import com.jacstuff.musicplayer.fragments.playlist.PlaylistRecyclerAdapter;
import com.jacstuff.musicplayer.viewmodel.MainViewModel;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistsFragment extends Fragment {

    private Context context;
    private MainViewModel viewModel;
    private boolean hasClicked;
    private RecyclerView recyclerView;
    private PlaylistRecyclerAdapter playlistRecyclerAdapter;
    private PlaylistRepository playlistRepository;

    public PlaylistsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println("Entered onCreateView");
        context = getContext();
        View view = inflater.inflate(R.layout.fragment_playlists, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        playlistRepository = new PlaylistRepositoryImpl(getContext());
        setupButtons(view);
        setupPlaylistRecyclerView(view);
        hasClicked = false;
        return view;
    }


    private void setupButtons(View parentView){
        setupAddPlaylistButton(parentView);
    }


    private void setupAddPlaylistButton(View parentView){
        Button addPlaylistButton = parentView.findViewById(R.id.addPlaylistButton);
        addPlaylistButton.setOnClickListener((View v)->{
            startAddPlaylistFragment();
        });
    }


    private void setupPlaylistRecyclerView(View parentView){
        recyclerView = parentView.findViewById(R.id.playlistRecyclerView);
        List<Playlist> playlists = playlistRepository.getAllPlaylists();
        playlists.forEach(x -> System.out.println(x.getName()));
        playlistRecyclerAdapter = new PlaylistRecyclerAdapter(playlists);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(playlistRecyclerAdapter);
    }


    public void onAddNewPlaylist(){
        hasClicked = false;
        playlistRecyclerAdapter.refresh(playlistRepository.getAllPlaylists());
    }


    private void startAddPlaylistFragment(){
        if(hasClicked){
            return;
        }
        hasClicked = true;
        String tag = "add_playlist";
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(fragmentManager == null){
            return;
        }
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        removePreviousFragmentTransaction(fragmentManager,tag, fragmentTransaction);
        AddPlaylistFragment addPlaylistFragment = AddPlaylistFragment.newInstance();
        addPlaylistFragment.show(fragmentTransaction, tag);
    }


    private FragmentManager getSupportFragmentManager(){
        FragmentActivity activity = getActivity();
        if(activity == null){
            return null;
        }
        return activity.getSupportFragmentManager();
    }


    private void removePreviousFragmentTransaction(FragmentManager fragmentManager, String tag, FragmentTransaction fragmentTransaction){
        Fragment prev = fragmentManager.findFragmentByTag(tag);
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
    }

}