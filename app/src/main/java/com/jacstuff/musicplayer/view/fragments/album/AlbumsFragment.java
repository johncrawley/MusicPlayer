package com.jacstuff.musicplayer.view.fragments.album;

import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.sendMessages;
import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.setListener;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_ALBUM_TAB_TO_RESELECT_ITEM;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TO_DESELECT_ALBUM_ITEMS;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TO_DESELECT_GENRE_ITEMS;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TO_DESELECT_PLAYLIST_ITEMS;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TO_LOAD_ALBUM;
import static com.jacstuff.musicplayer.view.fragments.Message.LOAD_ALBUMS;
import static com.jacstuff.musicplayer.view.fragments.MessageKey.ALBUM_ARTIST;
import static com.jacstuff.musicplayer.view.fragments.MessageKey.ALBUM_UPDATES;
import static com.jacstuff.musicplayer.view.utils.ListUtils.setVisibilityOnNoItemsFoundText;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.ListIndexManager;
import com.jacstuff.musicplayer.service.MediaPlayerService;
import com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper;
import com.jacstuff.musicplayer.view.fragments.StringListAdapter;
import com.jacstuff.musicplayer.view.utils.ButtonMaker;

import java.util.ArrayList;
import java.util.List;

public class AlbumsFragment extends Fragment {

    private RecyclerView recyclerView;
    private StringListAdapter listAdapter;
    private View parentView;
    private TextView noAlbumsFoundTextView;
    private ListIndexManager listIndexManager;
    private ViewGroup showAllAlbumsButtonLayout;
    private TextView albumArtistText;

    public AlbumsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_albums, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        this.parentView = view;
        initViews(parentView);
        refreshList();
        setupFragmentListener();
        assignListIndexManager();
        selectSavedIndex();
        setVisibilityOnBackButton();
    }


    private void initViews(View parentView){
        recyclerView = parentView.findViewById(R.id.albumsRecyclerView);
        noAlbumsFoundTextView = parentView.findViewById(R.id.noAlbumsFoundTextView);
        ButtonMaker.initImageButton(parentView, R.id.showAllAlbumsButton, this::loadAllAlbumNames);
        showAllAlbumsButtonLayout = parentView.findViewById(R.id.showAllAlbumsButtonLayout);
        albumArtistText = parentView.findViewById(R.id.albumsArtistNameTextView);
    }


    private void setupFragmentListener(){
        setListener(this, LOAD_ALBUMS, this::loadAlbumNamesFrom);
        setListener(this, NOTIFY_TO_LOAD_ALBUM, (bundle) -> listAdapter.selectLongClickItem());
        setListener(this, NOTIFY_TO_DESELECT_ALBUM_ITEMS, (bundle) -> listAdapter.deselectCurrentlySelectedItem());
        setListener(this, NOTIFY_ALBUM_TAB_TO_RESELECT_ITEM, (bundle) -> this.reselectItemAfterServiceConnection());
    }


    private void loadAllAlbumNames(){
       var mps = getMainActivity().getMediaPlayerService();
       if(mps != null){
           var playlistManager = mps.getPlaylistManager();
           List<String> allAlbumNames = playlistManager.getAllAlbumNamesAndClearCurrentArtist();
            if(allAlbumNames != null){
                loadAlbumNames(allAlbumNames);
                showAllAlbumsButtonLayout.setVisibility(View.GONE);
                albumArtistText.setText(getString(R.string.default_album_info));
            }
       }
    }


    private void reselectItemAfterServiceConnection(){
        assignListIndexManager();
        selectSavedIndex();
    }


    private void loadAlbumNamesFrom(Bundle bundle){
        ArrayList<String> albumNames =  bundle.getStringArrayList(ALBUM_UPDATES.toString());
        if(albumNames != null){
            loadAlbumNames(albumNames);
        }
        setArtistNameFrom(bundle);
    }


    private void setArtistNameFrom(Bundle bundle){
        String artistName = bundle.getString(ALBUM_ARTIST.toString());
        String value = artistName == null || artistName.isBlank() ?
                getString(R.string.default_album_info)
                : getString(R.string.album_artist, artistName);
        albumArtistText.setText(value);
    }


    private void loadAlbumNames(List<String> albumNames){
        listAdapter.setItems(albumNames);
        listAdapter.deselectCurrentlySelectedItem();
        listAdapter.resetSelections();
        setVisibilityOnNoAlbumsFoundText(albumNames);
        setVisibilityOnBackButton();
    }


    private void refreshList(){
        List<String> albumNames = getMainActivity().getAlbumNames();
        setVisibilityOnNoAlbumsFoundText(albumNames);
        if(this.parentView == null || albumNames == null){
            return;
        }
        listAdapter = new StringListAdapter(albumNames, this::loadTracksFromAlbum, this::showOptionsDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(listAdapter);
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
            listIndexManager.setAlbumIndex(index);
        }
    }


    private void selectSavedIndex(){
        if(listIndexManager != null){
            listIndexManager.getAlbumIndex().ifPresent(this::scrollToAndSelect);
        }
    }


    private void scrollToAndSelect(int index){
        listAdapter.selectItemAt(index);
        recyclerView.scrollToPosition(index);
    }


    private void setVisibilityOnNoAlbumsFoundText(List<String> tracks){
        setVisibilityOnNoItemsFoundText(tracks, recyclerView, noAlbumsFoundTextView);
    }


    private void loadTracksFromAlbum(String albumName, int position){
        assignIndex(position);
        getMainActivity().loadTracksFromAlbum(albumName);
        notifyOtherFragmentsToDeselectItems();
        toastLoaded();
    }


    private void notifyOtherFragmentsToDeselectItems(){
        sendMessages(this,
                NOTIFY_TO_DESELECT_PLAYLIST_ITEMS,
                NOTIFY_TO_DESELECT_GENRE_ITEMS);
    }


    private void showOptionsDialog(String albumName){
        Bundle bundle = new Bundle();
        bundle.putString(AlbumOptionsFragment.ALBUM_NAME_BUNDLE_KEY, albumName);
        FragmentManagerHelper.showDialog(this, AlbumOptionsFragment.newInstance(), "album_options", bundle);
    }


    private MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }


    private void toastLoaded(){
        getMainActivity().toastIfTabsNotAutoSwitched(R.string.album_loaded);
    }


    private void setVisibilityOnBackButton(){
        showAllAlbumsButtonLayout.setVisibility(isArtistLoaded() ? View.VISIBLE : View.GONE);
    }


    private boolean isArtistLoaded(){
        var mediaPlayerService = getMainActivity().getMediaPlayerService();
        if(mediaPlayerService != null){
            var playlistManager = mediaPlayerService.getPlaylistManager();
            String artistName =  playlistManager.getCurrentArtistName().orElse("");
            return !artistName.isBlank();
        }
        return false;
    }


}