package com.jacstuff.musicplayer.view.fragments.playlist;

import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.sendMessage;
import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.setListener;
import static com.jacstuff.musicplayer.view.fragments.playlist.PlaylistOptionsFragment.NOTIFY_PLAYLISTS_FRAGMENT_TO_CREATE;
import static com.jacstuff.musicplayer.view.fragments.playlist.PlaylistOptionsFragment.NOTIFY_PLAYLISTS_FRAGMENT_TO_DELETE;
import static com.jacstuff.musicplayer.view.fragments.playlist.PlaylistOptionsFragment.NOTIFY_PLAYLISTS_FRAGMENT_TO_LOAD;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.jacstuff.musicplayer.view.fragments.tracks.TracksFragment;
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
        setListener(this, NOTIFY_PLAYLISTS_FRAGMENT_TO_LOAD, (bundle) -> loadLongClickedPlaylist());
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
        int previousPlaylistCount =  playlistRecyclerAdapter.getItemCount();
        refreshList();
        toastIfPlaylistAdded(previousPlaylistCount);
    }


    public void onAddDialogDismissed(){
        hasClicked = false;
        refreshList();
    }


    private void toastIfPlaylistAdded(int previousPlaylistCount){
        if(playlistRecyclerAdapter != null && playlistRecyclerAdapter.getItemCount() > previousPlaylistCount){
            toastPlaylistCreated();
        }
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
        FragmentManagerHelper.showDialog(this, AddPlaylistFragment.newInstance(), "create_playlist", new Bundle());
    }


    private void startPlaylistOptionsFragment(Playlist playlist){
        Bundle bundle = new Bundle();
        bundle.putBoolean(BUNDLE_KEY_IS_USER_PLAYLIST, playlist.isUserPlaylist());
        FragmentManagerHelper.showDialog(this, PlaylistOptionsFragment.newInstance(), "playlist_options", bundle);
    }


    private void showDeletePlaylistDialog(){
        Playlist playlist = playlistRecyclerAdapter.getLongClickedPlaylist();
        if(playlist == null){
            return;
        }
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.delete_confirm_dialog_title))
                .setMessage(getResources().getString(R.string.delete_confirm_dialog_text, playlist.getName()))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, (dialog, whichButton) -> deletePlaylistAndSelectFirstPlaylist(playlist))
                .setNegativeButton(android.R.string.cancel, null).show();
    }


    private void deletePlaylistAndSelectFirstPlaylist(Playlist playlist){
        navigateToFirstPlaylistIfDeletedPlaylistIsLoaded(playlist);
        playlistRecyclerAdapter.clearLongClickedView();
        playlistRepository.deletePlaylist(playlist.getId());
        refreshList();
        showPlaylistDeletedToast();
    }


    private void navigateToFirstPlaylistIfDeletedPlaylistIsLoaded(Playlist playlist){
        if(playlistRecyclerAdapter.getSelectedPlaylist() == playlist){
            View item = recyclerView.getChildAt(0);
            item.callOnClick();
            playlistRecyclerAdapter.select(item);
        }
    }


    private void loadSelectedPlaylist(Playlist playlist){
        MainActivity mainActivity = getMainActivity();
        if(mainActivity == null || playlist == null){
            return;
        }
        mainActivity.loadPlaylist(playlist, false);
        notifyOtherFragmentsToDeselectItems();
        notifyTracksFragmentOfPlaylistLoaded(playlist.isUserPlaylist());
        toastLoaded();
    }


    private void notifyTracksFragmentOfPlaylistLoaded(boolean isUserPlaylist){
        Bundle bundle = new Bundle();
        bundle.putBoolean(TracksFragment.IS_USER_PLAYLIST_LOADED_KEY, isUserPlaylist);
        sendMessage(this, TracksFragment.NOTIFY_USER_PLAYLIST_LOADED, bundle);
    }


    private void loadLongClickedPlaylist(){
        playlistRecyclerAdapter.selectLongClickedView();
        loadSelectedPlaylist(playlistRecyclerAdapter.getSelectedPlaylist());
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


    private void refreshList(){
        playlistRecyclerAdapter.refresh(getAllPlaylists());
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


    private void toastLoaded(){
        Toast.makeText(getContext(), getString(R.string.toast_playlist_tracks_loaded), Toast.LENGTH_SHORT).show();
    }


    private void toastPlaylistCreated(){
        new Handler(Looper.getMainLooper())
                .postDelayed(()->
                    Toast.makeText(getContext(), getString(R.string.toast_playlist_created), Toast.LENGTH_SHORT).show()
        , 500);
    }

}