package com.jacstuff.musicplayer.fragments.playlist;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.db.playlist.Playlist;
import com.jacstuff.musicplayer.db.playlist.PlaylistRepository;
import com.jacstuff.musicplayer.db.playlist.PlaylistRepositoryImpl;
import com.jacstuff.musicplayer.fragments.AddPlaylistFragment;
import com.jacstuff.musicplayer.playlist.PlaylistManagerImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistsFragment extends Fragment {

    private Context context;
    private boolean hasClicked;
    private PlaylistRecyclerAdapter playlistRecyclerAdapter;
    private PlaylistRepository playlistRepository;
    private RecyclerView recyclerView;
    private Set<String> playlistNames;
    private final int INITIAL_PLAYLIST_CAPACITY = 50;

    public PlaylistsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getContext();
        View view = inflater.inflate(R.layout.fragment_playlists, container, false);
        playlistRepository = new PlaylistRepositoryImpl(getContext());
        setupPlaylistRecyclerView(view);
        hasClicked = false;
        return view;
    }


    @Override
    public void onViewCreated(View view,  Bundle savedInstanceState){
        setupButtons(view);
    }


    public Set<String> getPlaylistNames(){
        return playlistNames;
    }


    public void onAddNewPlaylist(){
        hasClicked = false;
        refreshList();
    }


    public void onAddDialogDismissed(){
        hasClicked = false;
    }


    private void setupButtons(View parentView){
        setupButton(parentView, R.id.addPlaylistButton, this::startAddPlaylistFragment);
        setupButton(parentView, R.id.deletePlaylistButton, this::showDeletePlaylistDialog);
        setupButton(parentView, R.id.loadPlaylistButton, ()->loadSelectedPlaylist(true));
    }


    private Button setupButton(View parentView, int buttonId, Runnable onClick){
        Button button = parentView.findViewById(buttonId);
        button.setOnClickListener((View v)->onClick.run());
        return button;
    }


    private void setupPlaylistRecyclerView(View parentView){
        recyclerView = parentView.findViewById(R.id.playlistRecyclerView);
        playlistRecyclerAdapter = new PlaylistRecyclerAdapter(getAllPlaylists(), (Playlist p)->{});
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(playlistRecyclerAdapter);
    }


    private void addAllTracksPlaylist(List<Playlist> playlists){
        Playlist playlist = new Playlist(PlaylistManagerImpl.ALL_TRACKS_PLAYLIST_ID, PlaylistManagerImpl.ALL_TRACKS_PLAYLIST);
        playlists.add(playlist);
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


    private void showDeletePlaylistDialog(){
        Playlist playlist = playlistRecyclerAdapter.getSelectedPlaylist();
        if(playlist == null){
            return;
        }
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.delete_confirm_dialog_title))
                .setMessage(getResources().getString(R.string.delete_confirm_dialog_text, playlist.getName()))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    deletePlaylistAndSelectFirstPlaylist(playlist);
                })
                .setNegativeButton(android.R.string.no, null).show();
    }


    private void deletePlaylistAndSelectFirstPlaylist(Playlist playlist){
        playlistRepository.deletePlaylist(playlist.getId());
        refreshList();
        showPlaylistDeletedToast();
        View item = recyclerView.getChildAt(0);
        playlistRecyclerAdapter.select(item);
        loadSelectedPlaylist(false);
    }


    private void loadSelectedPlaylist(boolean shouldSwitchToTracksTab){
        Playlist playlist = playlistRecyclerAdapter.getSelectedPlaylist();
        if(playlist != null){
            if(getMainActivity() != null){
                getMainActivity().setActivePlaylist(playlist, shouldSwitchToTracksTab);
            }
        }
    }


    private MainActivity getMainActivity(){
        return (MainActivity) getActivity();
    }


    private void showPlaylistDeletedToast(){
        Toast.makeText(getContext(), getString(R.string.delete_playlist_toast_success), Toast.LENGTH_SHORT).show();
    }


    @SuppressWarnings("notifyDataSetChanged")
    private void refreshList(){
        playlistRecyclerAdapter.refresh(getAllPlaylists());
        playlistRecyclerAdapter.notifyDataSetChanged();
    }


    private List<Playlist> getAllPlaylists(){
        List<Playlist> playlists = new ArrayList<>(INITIAL_PLAYLIST_CAPACITY);
        addAllTracksPlaylist(playlists);
        playlists.addAll(playlistRepository.getAllPlaylists());
        assignPlaylistNames(playlists);
        return playlists;
    }


    private void assignPlaylistNames(List<Playlist> playlists){
        playlistNames = new HashSet<>(INITIAL_PLAYLIST_CAPACITY);
        playlists.forEach((Playlist pl) -> playlistNames.add(pl.getName().toLowerCase()));
    }




}