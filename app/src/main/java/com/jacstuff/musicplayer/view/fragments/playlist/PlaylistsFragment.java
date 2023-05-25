package com.jacstuff.musicplayer.view.fragments.playlist;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.db.playlist.Playlist;
import com.jacstuff.musicplayer.service.db.playlist.PlaylistRepository;
import com.jacstuff.musicplayer.service.db.playlist.PlaylistRepositoryImpl;
import com.jacstuff.musicplayer.view.utils.ButtonMaker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
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
    public static final String NOTIFY_ARTISTS_FRAGMENT_OF_PLAYLIST= "Notify_Artists_Fragment_Of_Playlist_Loaded";
    public static final  String NOTIFY_ALBUMS_FRAGMENT_OF_PLAYLIST = "Notify_Albums_Fragment_Of_Playlist_Loaded";
    public static final String BUNDLE_KEY_USER_PLAYLIST_LOADED = "User_Playlist_Loaded_message";
    public static final String BUNDLE_KEY_IS_USER_PLAYLIST = "bundle_key_is_user_playlist";

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
        setupFragmentListeners();
        return view;
    }


    @SuppressLint("NotifyDataSetChanged")
    private void setupFragmentListeners() {
        setupFragmentListener(PlaylistOptionsFragment.NOTIFY_PLAYLISTS_FRAGMENT_TO_DELETE, this::showDeletePlaylistDialog);
        setupFragmentListener(PlaylistOptionsFragment.NOTIFY_PLAYLISTS_FRAGMENT_TO_LOAD, this::loadSelectedPlaylist);
        setupFragmentListener(PlaylistOptionsFragment.NOTIFY_PLAYLISTS_FRAGMENT_TO_CREATE, this::startAddPlaylistFragment);
    }


    private void setupFragmentListener(String key, Runnable runnable){
        getParentFragmentManager().setFragmentResultListener(key, this, (requestKey, bundle) -> runnable.run());
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
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
        ButtonMaker.createImageButton(parentView, R.id.addPlaylistButton, this::startAddPlaylistFragment);
    }


    private void setupPlaylistRecyclerView(View parentView){
        recyclerView = parentView.findViewById(R.id.playlistRecyclerView);
        playlistRecyclerAdapter = new PlaylistRecyclerAdapter(getAllPlaylists(),
                this::loadSelectedPlaylist,
                this::startPlaylistOptionsFragment);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(playlistRecyclerAdapter);
    }


    private void startAddPlaylistFragment(){
        if(hasClicked){
            return;
        }
        hasClicked = true;
        startDialogFragment("add_playlist", AddPlaylistFragment.newInstance(), new Bundle());
    }


    private void startPlaylistOptionsFragment(Playlist playlist){
        Bundle bundle = new Bundle();
        bundle.putBoolean(BUNDLE_KEY_IS_USER_PLAYLIST, playlist.isUserPlaylist());
        startDialogFragment("playlist_options", PlaylistOptionsFragment.newInstance(), bundle);
    }


    private void startDialogFragment(String tag, DialogFragment dialogFragment, Bundle bundle){
        FragmentManager fragmentManager =  getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        removePreviousFragmentTransaction(fragmentManager,tag, fragmentTransaction);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fragmentTransaction, tag);

    }


    private void removePreviousFragmentTransaction(FragmentManager fragmentManager, String tag, FragmentTransaction fragmentTransaction){
        Fragment prev = fragmentManager.findFragmentByTag(tag);
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
    }


    private void log(String msg){
        System.out.println("^^^ PlaylistsFragment: " + msg);
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
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> deletePlaylistAndSelectFirstPlaylist(playlist))
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


    private void loadSelectedPlaylist(){
        loadSelectedPlaylist(false);
    }


    private void loadSelectedPlaylist(boolean shouldSwitchToTracksTab){
        Playlist playlist = playlistRecyclerAdapter.getSelectedPlaylist();
        if(playlist != null){
            MainActivity mainActivity = getMainActivity();
            if(mainActivity != null){
                mainActivity.loadPlaylist(playlist, shouldSwitchToTracksTab);
                notifyFragmentsOfPlaylistType(mainActivity.isUserPlaylistLoaded());
            }
        }
    }


    private void loadSelectedPlaylist(Playlist playlist){
        if(playlist != null){
            MainActivity mainActivity = getMainActivity();
            if(mainActivity != null){
                mainActivity.loadPlaylist(playlist, false);
                notifyFragmentsOfPlaylistType(mainActivity.isUserPlaylistLoaded());
            }
        }
    }


    private void notifyFragmentsOfPlaylistType(boolean isUserPlaylistLoaded){
        Bundle bundle = new Bundle();
        bundle.putBoolean(BUNDLE_KEY_USER_PLAYLIST_LOADED, isUserPlaylistLoaded);
        getParentFragmentManager().setFragmentResult(NOTIFY_ARTISTS_FRAGMENT_OF_PLAYLIST, bundle);
        getParentFragmentManager().setFragmentResult(NOTIFY_ALBUMS_FRAGMENT_OF_PLAYLIST, bundle);
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
        playlists.addAll(playlistRepository.getAllPlaylists());
        assignPlaylistNames(playlists);
        return playlists;
    }


    private void assignPlaylistNames(List<Playlist> playlists){
        playlistNames = new HashSet<>(INITIAL_PLAYLIST_CAPACITY);
        playlists.forEach((Playlist pl) -> playlistNames.add(pl.getName().toLowerCase()));
    }

}