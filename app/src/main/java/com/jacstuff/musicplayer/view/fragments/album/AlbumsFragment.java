package com.jacstuff.musicplayer.view.fragments.album;

import static com.jacstuff.musicplayer.MainActivity.SEND_ALBUMS_TO_FRAGMENT;
import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.sendMessage;
import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.setListener;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.jacstuff.musicplayer.view.fragments.playlist.PlaylistsFragment;

import java.util.ArrayList;
import java.util.List;

public class AlbumsFragment extends Fragment {

    public final static String NOTIFY_TO_LOAD_ALBUM = "Notify_Albums_Fragment_To_Load_Album";
    public final static String NOTIFY_TO_DESELECT_ITEMS = "Notify_Albums_Fragment_To_Deselect_Items";

    private RecyclerView recyclerView;
    private StringListAdapter listAdapter;
    private View parentView;


    public AlbumsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_albums, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        this.parentView = view;
        recyclerView = parentView.findViewById(R.id.albumsRecyclerView);
        refreshList();
        setupFragmentListener();
    }


    private void setupFragmentListener(){
        setListener(this, SEND_ALBUMS_TO_FRAGMENT, this::loadAlbums);
        setListener(this, NOTIFY_TO_LOAD_ALBUM, (bundle) -> listAdapter.selectLongClickItem());
        setListener(this, NOTIFY_TO_DESELECT_ITEMS, (bundle) -> listAdapter.deselectCurrentlySelectedItem());
    }


    private void loadAlbums(Bundle bundle){
        ArrayList<String> albumNames =  bundle.getStringArrayList(MainActivity.BUNDLE_KEY_ALBUM_UPDATES);
        listAdapter.setItems(albumNames);
        listAdapter.deselectCurrentlySelectedItem();
        listAdapter.resetSelections();
    }


    private void refreshList(){
        List<String> albums = getMainActivity().getAlbumNames();
        if(this.parentView == null || albums == null){
            return;
        }
        listAdapter = new StringListAdapter(albums, this::loadTracksFromAlbum, this::showOptionsDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(listAdapter);
    }


    private void loadTracksFromAlbum(String albumName){
        getMainActivity().loadTracksFromAlbum(albumName);
        sendMessage(this, PlaylistsFragment.NOTIFY_TO_DESELECT_ITEMS);
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