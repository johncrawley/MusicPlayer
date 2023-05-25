package com.jacstuff.musicplayer.view.fragments.artist;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.view.fragments.StringListAdapter;
import com.jacstuff.musicplayer.view.fragments.playlist.PlaylistsFragment;
import com.jacstuff.musicplayer.view.utils.ButtonMaker;

import java.util.ArrayList;
import java.util.List;

public class ArtistsFragment extends Fragment {

    private RecyclerView recyclerView;
    private StringListAdapter listAdapter;
    private View parentView;
    private Button loadTracksFromArtistButton, addTracksToPlaylistButton;

    public ArtistsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artists, container, false);
    }


    @Override
    public void onViewCreated(@androidx.annotation.NonNull View view, Bundle savedInstanceState){
        this.parentView = view;
        recyclerView = parentView.findViewById(R.id.artistsRecyclerView);
        setupButtons(parentView);
        refreshArtistsList();
        setupFragmentListener();
    }


    @SuppressLint("NotifyDataSetChanged")
    private void setupFragmentListener(){
        getParentFragmentManager().setFragmentResultListener(PlaylistsFragment.NOTIFY_ARTISTS_FRAGMENT_OF_PLAYLIST, this, (requestKey, bundle) -> {
            int visibility = isBundleUserPlaylistLoaded(bundle) && isItemSelected()? View.VISIBLE : View.INVISIBLE;
            addTracksToPlaylistButton.setVisibility(visibility);
        });

        getParentFragmentManager().setFragmentResultListener(MainActivity.SEND_ARTISTS_TO_FRAGMENT, this, (requestKey, bundle) -> {
            ArrayList<String> artistNames =  bundle.getStringArrayList(MainActivity.BUNDLE_KEY_ARTIST_UPDATES);
            listAdapter.setItems(artistNames);
            listAdapter.notifyDataSetChanged();
        });

        getParentFragmentManager().setFragmentResultListener(ArtistOptionsFragment.NOTIFY_ARTISTS_FRAGMENT_TO_LOAD_ARTIST, this, (requestKey, bundle) -> {
            listAdapter.selectLongClickItem();
        });
    }


    private boolean isBundleUserPlaylistLoaded(Bundle bundle){
        return bundle.getBoolean(PlaylistsFragment.BUNDLE_KEY_USER_PLAYLIST_LOADED);
    }


    private boolean isItemSelected(){
        return listAdapter.getCurrentlySelectedItem() != null;
    }


    private void setupButtons(View parentView){
        loadTracksFromArtistButton = ButtonMaker.createButton(parentView, R.id.loadTracksFromArtistButton, ()->{
            getMainActivity().loadTracksFromArtist(getSelectedArtist());
            getMainActivity().switchToTracksTab();
        });

        addTracksToPlaylistButton = ButtonMaker.createButton(parentView, R.id.addTracksFromArtistToPlaylistButton, ()->
            getMainActivity().getMediaPlayerService().addTracksFromAristToCurrentPlaylist(getSelectedArtist()));
    }


    private String getSelectedArtist(){
        return listAdapter.getCurrentlySelectedItem();
    }


    private MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }


    private void refreshArtistsList(){
        List<String> artists = getMainActivity().getArtistNames();
        if(this.parentView == null ||artists == null){
            return;
        }
        listAdapter = new StringListAdapter(artists, this::loadTracksAndAlbumsFromArtist, this::showOptionsDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(listAdapter);
    }


    private void loadTracksAndAlbumsFromArtist(String artistName){
        getMainActivity().loadTracksFromArtist(artistName);
    }


    private void showOptionsDialog(String artistName){
        String tag = "artist_options";
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(fragmentManager == null){
            return;
        }
        Bundle bundle = new Bundle();
        log("showOptionsDialog, adding artist name to bundle: " + artistName);
        bundle.putString(ArtistOptionsFragment.ARTIST_NAME_BUNDLE_KEY, artistName);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        removePreviousFragmentTransaction(fragmentManager, tag, fragmentTransaction);
        ArtistOptionsFragment artistOptionsFragment = ArtistOptionsFragment.newInstance();
        artistOptionsFragment.setArguments(bundle);
        artistOptionsFragment.show(fragmentTransaction, tag);
    }


    private void log(String msg){
        System.out.println("^^ ArtistsFragment: " + msg);
    }



    private FragmentManager getSupportFragmentManager(){
        FragmentActivity activity = getActivity();
        if(activity == null){
            return null;
        }
        return activity.getSupportFragmentManager();
    }


    private void removePreviousFragmentTransaction(FragmentManager fragmentManager, String tag, FragmentTransaction fragmentTransaction){
        Fragment prev = fragmentManager.findFragmentByTag(tag);
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
    }


    @SuppressLint("NotifyDataSetChanged")
    private void updateTrackViews(){
        listAdapter.notifyDataSetChanged();
    }



    private void setButtonsVisibility(String artistName){
        addTracksToPlaylistButton.setVisibility(getVisibilityForAddTracksButton());
        loadTracksFromArtistButton.setVisibility(View.VISIBLE);
    }


    private int getVisibilityForAddTracksButton(){
        return getMainActivity().getMediaPlayerService().getPlaylistManager().isUserPlaylistLoaded() ? View.VISIBLE : View.INVISIBLE;
    }


}