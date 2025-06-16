package com.jacstuff.musicplayer.view.fragments.playlist;

import static com.jacstuff.musicplayer.view.fragments.DialogFragmentUtils.addStrTo;
import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.sendMessage;
import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.sendMessages;
import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.setListener;
import static com.jacstuff.musicplayer.view.fragments.Message.ADD_RANDOM_TRACKS_TO_PLAYLIST;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_PLAYLISTS_FRAGMENT_TO_CREATE;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_PLAYLISTS_FRAGMENT_TO_DELETE;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_PLAYLISTS_FRAGMENT_TO_LOAD;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TO_DESELECT_ALBUM_ITEMS;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TO_DESELECT_ARTIST_ITEMS;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TO_DESELECT_GENRE_ITEMS;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_USER_PLAYLIST_LOADED;
import static com.jacstuff.musicplayer.view.fragments.Utils.putBoolean;
import static com.jacstuff.musicplayer.view.fragments.Utils.putLong;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.ListIndexManager;
import com.jacstuff.musicplayer.service.MediaPlayerService;
import com.jacstuff.musicplayer.service.db.entities.Playlist;
import com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper;
import com.jacstuff.musicplayer.view.fragments.Message;
import com.jacstuff.musicplayer.view.fragments.MessageKey;
import com.jacstuff.musicplayer.view.utils.ButtonMaker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistsFragment extends Fragment {

    private Context context;
    private boolean hasClicked;
    private PlaylistRecyclerAdapter listAdapter;
    private RecyclerView recyclerView;
    private Set<String> playlistNames;
    private final int INITIAL_PLAYLIST_CAPACITY = 50;
    private ListIndexManager listIndexManager;
    private int longClickedPosition;
    private View parentView;

    public PlaylistsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getContext();
        View view = inflater.inflate(R.layout.fragment_tab_playlists, container, false);
        setupListView(view);
        hasClicked = false;
        setupFragmentListeners();
        assignListIndexManager();
        selectSavedIndex();
        this.parentView = view;
        return view;
    }


    private void setupFragmentListeners(){
        setListener(this, NOTIFY_PLAYLISTS_FRAGMENT_TO_DELETE, (bundle)->  showDeletePlaylistDialog());
        setListener(this, ADD_RANDOM_TRACKS_TO_PLAYLIST, (bundle)->  startAddRandomTracksDialogFragment());
        setListener(this, NOTIFY_PLAYLISTS_FRAGMENT_TO_LOAD, (bundle) -> loadLongClickedPlaylist());
        setListener(this, NOTIFY_PLAYLISTS_FRAGMENT_TO_CREATE, (bundle) -> startAddPlaylistFragment());
        setListener(this, Message.NOTIFY_TO_DESELECT_PLAYLIST_ITEMS, (bundle) -> listAdapter.deselectCurrentlySelectedItem());
        setListener(this, Message.NOTIFY_PLAYLIST_TAB_TO_RELOAD, (bundle) -> setupListView(parentView));
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        setupButtons(view);
    }



    private void assignListIndexManager(){
        MediaPlayerService mediaPlayerService = getMainActivity().getMediaPlayerService();
        if(mediaPlayerService != null){
            listIndexManager = mediaPlayerService.getListIndexManager();
        }
    }


    private void assignIndex(int index){
        if(listIndexManager == null){
            assignListIndexManager();
        }
        if(listIndexManager != null){
            listIndexManager.setPlaylistIndex(index);
        }
    }


    private void selectSavedIndex(){
        if(listIndexManager != null){
            listIndexManager.getGenreIndex().ifPresent(this::scrollToAndSelect);
        }
    }


    private void scrollToAndSelect(int index){
        listAdapter.selectItemAt(index);
        recyclerView.scrollToPosition(index);
    }



    public Set<String> getPlaylistNames(){
        return playlistNames;
    }


    public void onAddNewPlaylist(){
        hasClicked = false;
        int previousPlaylistCount =  listAdapter.getItemCount();
        refreshList();
        toastIfPlaylistAdded(previousPlaylistCount);
    }


    public void onAddDialogDismissed(){
        hasClicked = false;
        refreshList();
    }


    private void toastIfPlaylistAdded(int previousPlaylistCount){
        if(listAdapter != null && listAdapter.getItemCount() > previousPlaylistCount){
            toastPlaylistCreated();
        }
    }


    private void setupButtons(View parentView){
        ButtonMaker.createImageButton(parentView, R.id.addPlaylistButton, this::startAddPlaylistFragment);
    }


    private void setupListView(View parentView){
        if(parentView == null){
            return;
        }
        recyclerView = parentView.findViewById(R.id.playlistRecyclerView);
        listAdapter = new PlaylistRecyclerAdapter(getAllPlaylists(),
                this::loadSelectedPlaylist,
                this::startPlaylistOptionsFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(listAdapter);
    }


    private void startAddPlaylistFragment(){
        if(hasClicked){
            return;
        }
        hasClicked = true;
        FragmentManagerHelper.showDialog(this, CreatePlaylistFragment.newInstance(), "create_playlist", new Bundle());
    }


    private void startPlaylistOptionsFragment(Playlist playlist, int position){
        longClickedPosition = position;
        Bundle bundle = new Bundle();
        putBoolean(bundle, MessageKey.IS_USER_PLAYLIST, playlist.isUserPlaylist());
        addStrTo(bundle, MessageKey.PLAYLIST_NAME, playlist.getName());
        putLong(bundle, MessageKey.PLAYLIST_ID, playlist.getId());
        FragmentManagerHelper.showDialog(this, PlaylistOptionsFragment.newInstance(), "playlist_options", bundle);
    }

    private void log(String msg){
        System.out.println("^^^ PlaylistsFragment: " + msg);
    }


    private void showDeletePlaylistDialog(){
        Playlist playlist = listAdapter.getLongClickedPlaylist();
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


    private void startAddRandomTracksDialogFragment(){
        Bundle bundle = new Bundle();
        FragmentManagerHelper.showDialog(this, PlaylistOptionsFragment.newInstance(), "playlist_options", bundle);
    }


    private void deletePlaylistAndSelectFirstPlaylist(Playlist playlist){
        navigateToFirstPlaylistIfDeletedPlaylistIsLoaded(playlist);
        listAdapter.clearLongClickedView();
        delete(playlist);
        refreshList();
        toast(R.string.delete_playlist_toast_success);
    }


    private void navigateToFirstPlaylistIfDeletedPlaylistIsLoaded(Playlist playlist){
        if(listAdapter.getSelectedPlaylist() == playlist){
            View item = recyclerView.getChildAt(0);
            item.callOnClick();
            listAdapter.select(item);
        }
    }


    private void delete(Playlist playlist){
        getMediaPlayerService().ifPresent(mps -> mps.getPlaylistManager().deletePlaylist(playlist));
    }


    private void loadSelectedPlaylist(Playlist playlist, int position){
        assignIndex(position);
        getMain().ifPresent(m -> load(playlist, m));
    }


    private void load(Playlist playlist, MainActivity mainActivity){
        if(playlist == null){
            return;
        }
        mainActivity.loadTracksFromPlaylist(playlist);
        notifyOtherFragmentsToDeselectItems();
        notifyTracksFragmentOfPlaylistLoaded(playlist.isUserPlaylist());
        toastLoaded();
    }


    private void notifyTracksFragmentOfPlaylistLoaded(boolean isUserPlaylist){
        Bundle bundle = new Bundle();
        putBoolean(bundle, MessageKey.IS_USER_PLAYLIST, isUserPlaylist);
        sendMessage(this, NOTIFY_USER_PLAYLIST_LOADED, bundle);
    }


    private void loadLongClickedPlaylist(){
        listAdapter.selectLongClickedView();
        loadSelectedPlaylist(listAdapter.getSelectedPlaylist(), longClickedPosition);
    }


    private void notifyOtherFragmentsToDeselectItems(){
        sendMessages(this,
                NOTIFY_TO_DESELECT_ALBUM_ITEMS,
                NOTIFY_TO_DESELECT_ARTIST_ITEMS,
                NOTIFY_TO_DESELECT_GENRE_ITEMS);
    }


    private MainActivity getMainActivity(){
        return (MainActivity) getActivity();
    }


    private Optional<MainActivity> getMain(){
        return Optional.ofNullable((MainActivity) getActivity());
    }


    private void refreshList(){
        listAdapter.refresh(getAllPlaylists());
    }


    private List<Playlist> getAllPlaylists(){
        List<Playlist> playlists = loadAllPlaylists();
        assignPlaylistNames(playlists);
        return playlists;
    }


    private List<Playlist> loadAllPlaylists(){
        List<Playlist> playlists = new ArrayList<>(INITIAL_PLAYLIST_CAPACITY);
        getMediaPlayerService().ifPresent(mps -> playlists.addAll(mps.getPlaylistManager().getAllPlaylists()));
        return playlists;
    }


    private Optional<MediaPlayerService> getMediaPlayerService(){
        MainActivity mainActivity = getMainActivity();
        return mainActivity == null ? Optional.empty() : Optional.ofNullable(mainActivity.getMediaPlayerService());
    }


    private void assignPlaylistNames(List<Playlist> playlists){
        playlistNames = new HashSet<>(INITIAL_PLAYLIST_CAPACITY);
        playlists.forEach((Playlist pl) -> playlistNames.add(pl.getName().toLowerCase()));
    }


    private void toastLoaded(){
        getMain().ifPresent(ma -> ma.toastIfTabsNotAutoSwitched(R.string.toast_playlist_tracks_loaded));
    }


    private void toastPlaylistCreated(){
        new Handler(Looper.getMainLooper())
                .postDelayed(()-> toast(R.string.toast_playlist_created) , 500);
    }

    private void toast(int strId){
        getMain().ifPresent(ma -> ma.toast(strId));
    }
}