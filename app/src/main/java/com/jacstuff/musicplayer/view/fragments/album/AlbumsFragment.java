package com.jacstuff.musicplayer.view.fragments.album;

import static com.jacstuff.musicplayer.MainActivity.SEND_ALBUMS_TO_FRAGMENT;
import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.sendMessage;
import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.setListener;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TO_DESELECT_ALBUM_ITEMS;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TO_DESELECT_PLAYLIST_ITEMS;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TO_LOAD_ALBUM;
import static com.jacstuff.musicplayer.view.utils.ListUtils.setVisibilityOnNoItemsFoundText;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

public class AlbumsFragment extends Fragment {

    private RecyclerView recyclerView;
    private StringListAdapter listAdapter;
    private View parentView;
    private TextView noAlbumsFoundTextView;


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
        recyclerView = parentView.findViewById(R.id.albumsRecyclerView);
        noAlbumsFoundTextView = parentView.findViewById(R.id.noAlbumsFoundTextView);
        refreshList();
        setupFragmentListener();
    }


    private void setupFragmentListener(){
        setListener(this, SEND_ALBUMS_TO_FRAGMENT, this::loadAlbums);
        setListener(this, NOTIFY_TO_LOAD_ALBUM, (bundle) -> listAdapter.selectLongClickItem());
        setListener(this, NOTIFY_TO_DESELECT_ALBUM_ITEMS, (bundle) -> listAdapter.deselectCurrentlySelectedItem());
    }


    private void loadAlbums(Bundle bundle){
        ArrayList<String> albumNames =  bundle.getStringArrayList(MainActivity.BUNDLE_KEY_ALBUM_UPDATES);
        listAdapter.setItems(albumNames);
        listAdapter.deselectCurrentlySelectedItem();
        listAdapter.resetSelections();
        setVisibilityOnNoAlbumsFoundText(albumNames);
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


    private void setVisibilityOnNoAlbumsFoundText(List<String> tracks){
        setVisibilityOnNoItemsFoundText(tracks, recyclerView, noAlbumsFoundTextView);
    }


    private void loadTracksFromAlbum(String albumName){
        getMainActivity().loadTracksFromAlbum(albumName);
        sendMessage(this, NOTIFY_TO_DESELECT_PLAYLIST_ITEMS);
        toastLoaded();
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
        Toast.makeText(getContext(), getString(R.string.toast_album_tracks_loaded), Toast.LENGTH_SHORT).show();
    }


}