package com.jacstuff.musicplayer.view.fragments.playlist;

import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.sendMessage;
import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.setListener;
import static com.jacstuff.musicplayer.view.fragments.playlist.PlaylistOptionsFragment.NOTIFY_PLAYLISTS_FRAGMENT_TO_CREATE;
import static com.jacstuff.musicplayer.view.fragments.playlist.PlaylistOptionsFragment.NOTIFY_PLAYLISTS_FRAGMENT_TO_DELETE;
import static com.jacstuff.musicplayer.view.fragments.playlist.PlaylistOptionsFragment.NOTIFY_PLAYLISTS_FRAGMENT_TO_LOAD;


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
import com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper;
import com.jacstuff.musicplayer.view.fragments.album.AlbumsFragment;
import com.jacstuff.musicplayer.view.fragments.artist.ArtistsFragment;
import com.jacstuff.musicplayer.view.utils.ButtonMaker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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
    public static final String BUNDLE_KEY_IS_USER_PLAYLIST = "bundle_key_is_user_playlist";
    public final static String NOTIFY_TO_DESELECT_ITEMS = "Notify_Playlists_Fragment_To_Deselect_Items";

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


    private void setupFragmentListeners(){
        setListener(this, NOTIFY_PLAYLISTS_FRAGMENT_TO_DELETE, (bundle)->  showDeletePlaylistDialog());
        setListener(this, NOTIFY_PLAYLISTS_FRAGMENT_TO_LOAD, (bundle) -> loadSelectedPlaylist());
        setListener(this, NOTIFY_PLAYLISTS_FRAGMENT_TO_CREATE, (bundle) -> startAddPlaylistFragment());
        setListener(this, NOTIFY_TO_DESELECT_ITEMS, (bundle) -> playlistRecyclerAdapter.deselectCurrentlySelectedItem());
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
        FragmentManagerHelper.showOptionsDialog(this, AddPlaylistFragment.newInstance(), "playlist_options", new Bundle());
    }


    private void startPlaylistOptionsFragment(Playlist playlist){
        Bundle bundle = new Bundle();
        bundle.putBoolean(BUNDLE_KEY_IS_USER_PLAYLIST, playlist.isUserPlaylist());
        FragmentManagerHelper.showOptionsDialog(this, PlaylistOptionsFragment.newInstance(), "playlist_options", bundle);
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
        loadSelectedPlaylist();
    }


    private void loadSelectedPlaylist(){
        playlistRecyclerAdapter.selectLongClickedView();
        loadSelectedPlaylist(playlistRecyclerAdapter.getSelectedPlaylist());
    }


    private void loadSelectedPlaylist(Playlist playlist){
        if(playlist != null){
            MainActivity mainActivity = getMainActivity();
            if(mainActivity != null){
                mainActivity.loadPlaylist(playlist, false);
                notifyOtherFragmentsToDeselectItems();
            }
        }
    }


    private void notifyOtherFragmentsToDeselectItems(){
        sendMessage(this, AlbumsFragment.NOTIFY_TO_DESELECT_ITEMS);
        sendMessage(this, ArtistsFragment.NOTIFY_TO_DESELECT_ITEMS);
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