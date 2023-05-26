package com.jacstuff.musicplayer.view.fragments.album;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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


    @SuppressLint("NotifyDataSetChanged")
    private void setupFragmentListener(){

        getParentFragmentManager().setFragmentResultListener(MainActivity.SEND_ALBUMS_TO_FRAGMENT, this, (requestKey, bundle) -> {
            ArrayList<String> albumNames =  bundle.getStringArrayList(MainActivity.BUNDLE_KEY_ALBUM_UPDATES);
            listAdapter.setItems(albumNames);
            listAdapter.notifyDataSetChanged();
        });


        getParentFragmentManager().setFragmentResultListener(AlbumOptionsFragment.NOTIFY_ALBUMS_FRAGMENT_TO_LOAD_ALBUM,
                this,
                (requestKey, bundle) -> listAdapter.selectLongClickItem());
    }


    private void loadTracksFromAlbum(String albumName){
        getMainActivity().loadTracksFromAlbum(albumName);
    }


    private MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }


    private void refreshList(){
        List<String> albums = getMainActivity().getAlbumNames();
        if(this.parentView == null ||albums == null){
            return;
        }
        listAdapter = new StringListAdapter(albums, this::loadTracksFromAlbum, this::showOptionsDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(listAdapter);
    }


    private void showOptionsDialog(String albumName){
        Bundle bundle = new Bundle();
        bundle.putString(AlbumOptionsFragment.ALBUM_NAME_BUNDLE_KEY, albumName);
        FragmentManagerHelper.showOptionsDialog(this, AlbumOptionsFragment.newInstance(), "album_options", bundle);
    }

}