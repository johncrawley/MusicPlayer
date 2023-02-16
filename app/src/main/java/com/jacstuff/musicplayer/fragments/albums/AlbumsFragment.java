package com.jacstuff.musicplayer.fragments.albums;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.db.album.Album;
import com.jacstuff.musicplayer.db.album.AlbumRepository;
import com.jacstuff.musicplayer.db.artist.Artist;
import com.jacstuff.musicplayer.fragments.playlist.PlaylistsFragment;
import com.jacstuff.musicplayer.utils.ButtonMaker;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AlbumsFragment extends Fragment {

    private RecyclerView recyclerView;
    private AlbumListAdapter listAdapter;
    private View parentView;
    private AlbumRepository albumsRepository;
    private Button loadTracksButton, addTracksToPlaylistButton;

    public AlbumsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_albums, container, false);
        albumsRepository = new AlbumRepository(getContext());
        return view;
    }


    @Override
    public void onViewCreated(View view,  Bundle savedInstanceState){
        this.parentView = view;
        recyclerView = parentView.findViewById(R.id.albumsRecyclerView);
        setupButtons(parentView);
        refreshList();
        setupFragmentListener();
    }


    @SuppressLint("NotifyDataSetChanged")
    private void setupFragmentListener(){
        getParentFragmentManager().setFragmentResultListener(PlaylistsFragment.NOTIFY_ALBUMS_FRAGMENT_OF_PLAYLIST, this, (requestKey, bundle) -> {
            int visibility =  isBundleUserPlaylistLoaded(bundle) && isItemSelected()? View.VISIBLE : View.INVISIBLE;
            addTracksToPlaylistButton.setVisibility(visibility);
        });

        getParentFragmentManager().setFragmentResultListener(MainActivity.SEND_ALBUMS_TO_FRAGMENT, this, (requestKey, bundle) -> {
            ArrayList<String> albumNames =  bundle.getStringArrayList(MainActivity.BUNDLE_KEY_ALBUM_UPDATES);

            listAdapter.setItems(albumNames);
            listAdapter.notifyDataSetChanged();
        });
    }


    private boolean isBundleUserPlaylistLoaded(Bundle bundle){
       return bundle.getBoolean(PlaylistsFragment.BUNDLE_KEY_USER_PLAYLIST_LOADED);
    }


    private boolean isItemSelected(){
        return listAdapter.getCurrentlySelectedItem() != null;
    }


    private void setupButtons(View parentView){
       loadTracksButton = ButtonMaker.createButton(parentView, R.id.loadTracksFromAlbumButton, ()->{
            getMainActivity().loadTracksFromAlbum(listAdapter.getCurrentlySelectedItem());
            getMainActivity().switchToTracksTab();
        });

       addTracksToPlaylistButton = ButtonMaker.createButton(parentView, R.id.addTracksFromAlbumToPlaylistButton, ()->
                getMainActivity().getMediaPlayerService().addTracksFromAlbumToCurrentPlaylist(listAdapter.getCurrentlySelectedItem()));
    }


    private MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }


    private void refreshList(){
        List<Album> albums = albumsRepository.getAll();
        if(this.parentView == null ||albums == null){
            return;
        }
        listAdapter = new AlbumListAdapter(albums, this::setButtonsVisibility);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(listAdapter);
    }


    @SuppressLint("NotifyDataSetChanged")
    private void updateTrackViews(){
        listAdapter.notifyDataSetChanged();
    }


    private void setButtonsVisibility(Album album){
        addTracksToPlaylistButton.setVisibility(getVisibilityForAddTracksButton());
        loadTracksButton.setVisibility(View.VISIBLE);
    }


    private int getVisibilityForAddTracksButton(){
        return getMainActivity().getMediaPlayerService().getPlaylistManager().isUserPlaylistLoaded() ? View.VISIBLE : View.INVISIBLE;
    }


}