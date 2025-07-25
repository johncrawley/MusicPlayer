package com.jacstuff.musicplayer.view.fragments.playlist;

import static com.jacstuff.musicplayer.service.helpers.preferences.PrefKey.ARE_TABS_SWITCHED_AFTER_PLAYLIST_SELECTION;
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
import com.jacstuff.musicplayer.view.fragments.AlertHelper;
import com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper;
import com.jacstuff.musicplayer.view.fragments.Message;
import com.jacstuff.musicplayer.view.fragments.MessageKey;
import com.jacstuff.musicplayer.view.utils.ButtonMaker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistsFragment extends Fragment {

    private Context context;
    private boolean hasClicked;
    private PlaylistListAdapter listAdapter;
    private RecyclerView recyclerView;
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
        log("entered onViewCreated()");
        setupButtons(view);
        selectSavedIndex();
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
        new Handler(Looper.getMainLooper()).postDelayed(()->{
            if(listIndexManager != null){
                listIndexManager.getPlaylistIndex().ifPresentOrElse(this::scrollToAndSelect, ()-> log(" selectSavedIndex() : listIndexManager not found!"));
            }
            else{
                log("listIndexManager is null!");
            }
        }, 200);

    }


    private void scrollToAndSelect(int index){
        log("entered scrollToAndSelect(" + index + ")");
        listAdapter.selectItemAt(index);
        recyclerView.scrollToPosition(index);
    }


    private void log(String msg){
        System.out.println("^^^ PlaylistsFragment" + msg);
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
        ButtonMaker.initImageButton(parentView, R.id.addButton, this::startAddPlaylistFragment);
    }


    private void setupListView(View parentView){
        if(parentView == null){
            return;
        }
        recyclerView = parentView.findViewById(R.id.playlistRecyclerView);
        listAdapter = new PlaylistListAdapter(getAllPlaylists(),
                this::loadSelectedPlaylist,
                this::startPlaylistOptionsFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(listAdapter);
        assignListIndexManager();
        selectSavedIndex();
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


    private void showDeletePlaylistDialog(){
        var playlist = listAdapter.getLongClickedPlaylist();
        if(playlist != null){
            AlertHelper.showDialogForPlaylist(getContext(),
                    R.string.delete_list_title,
                    R.string.delete_list_confirm,
                    playlist.getName(),
                    this::deletePlaylistAndSelectFirstPlaylist);
        }
    }


    private void startAddRandomTracksDialogFragment(){
        Bundle bundle = new Bundle();
        FragmentManagerHelper.showDialog(this, PlaylistOptionsFragment.newInstance(), "playlist_options", bundle);
    }


    private void deletePlaylistAndSelectFirstPlaylist(){
        var playlist = listAdapter.getLongClickedPlaylist();
        navigateToFirstPlaylistIfDeletedPlaylistIsLoaded(playlist);
        listAdapter.clearLongClickedView();
        delete(playlist);
        refreshList();
        toast(R.string.list_deleted);
    }


    private void navigateToFirstPlaylistIfDeletedPlaylistIsLoaded(Playlist playlist){
        if(Objects.equals(listAdapter.getCurrentItem().getId(), playlist.getId())){
            int position = 0;
            View item = recyclerView.getChildAt(position);
            setAutoSwitchTabs(false);
            item.callOnClick();
            listAdapter.select(item, position);
            setAutoSwitchTabs(true);
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
        loadSelectedPlaylist(listAdapter.getCurrentItem(), longClickedPosition);
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
        return loadAllPlaylists();
    }


    private List<Playlist> loadAllPlaylists(){
        int INITIAL_PLAYLIST_CAPACITY = 50;
        List<Playlist> playlists = new ArrayList<>(INITIAL_PLAYLIST_CAPACITY);
        getMediaPlayerService().ifPresent(mps ->
                playlists.addAll(mps.getPlaylistManager().getAllPlaylists()));
        return playlists;
    }


    private Optional<MediaPlayerService> getMediaPlayerService(){
        MainActivity mainActivity = getMainActivity();
        return mainActivity == null ? Optional.empty() : Optional.ofNullable(mainActivity.getMediaPlayerService());
    }


    private void toastLoaded(){
        getMain().ifPresent(ma -> ma.toastIfTabsNotAutoSwitched(R.string.list_loaded));
    }


    private void toastPlaylistCreated(){
        new Handler(Looper.getMainLooper())
                .postDelayed(()-> toast(R.string.created) , 500);
    }

    private void toast(int strId){
        getMain().ifPresent(ma -> ma.toast(strId));
    }


    public void setAutoSwitchTabs(boolean isEnabled){
        getMain().ifPresent(ma -> {
            ma.getPreferencesHelper().set(ARE_TABS_SWITCHED_AFTER_PLAYLIST_SELECTION, isEnabled);
        });
    }

}