package com.jacstuff.musicplayer.view.fragments.artist;

import static com.jacstuff.musicplayer.MainActivity.SEND_ARTISTS_TO_FRAGMENT;
import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.sendMessage;
import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.setListener;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TO_DESELECT_ARTIST_ITEMS;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TO_DESELECT_PLAYLIST_ITEMS;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TO_LOAD_ARTIST;
import static com.jacstuff.musicplayer.view.utils.ListUtils.setVisibilityOnNoItemsFoundText;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

public class ArtistsFragment extends Fragment {

    private RecyclerView recyclerView;
    private StringListAdapter listAdapter;
    private View parentView;
    private TextView noArtistsFoundTextView;

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
        noArtistsFoundTextView = parentView.findViewById(R.id.noArtistsFoundTextView);
        refreshArtistsList();
        setupFragmentListener();
    }


    private void setupFragmentListener(){
        setListener(this, SEND_ARTISTS_TO_FRAGMENT, this::populateArtistsList);
        setListener(this, NOTIFY_TO_LOAD_ARTIST, (bundle) -> listAdapter.selectLongClickItem());
        setListener(this, NOTIFY_TO_DESELECT_ARTIST_ITEMS, (bundle) -> listAdapter.deselectCurrentlySelectedItem());
    }


    private void populateArtistsList(Bundle bundle){
        ArrayList<String> artistNames =  bundle.getStringArrayList(MainActivity.BUNDLE_KEY_ARTIST_UPDATES);
        listAdapter.setItems(artistNames);
        setVisibilityOnNoArtistsFoundText(artistNames);
    }


    private void refreshArtistsList(){
        List<String> artistNames = getMainActivity().getArtistNames();
        setVisibilityOnNoArtistsFoundText(artistNames);
        if(this.parentView == null || artistNames == null){
            return;
        }
        listAdapter = new StringListAdapter(artistNames, this::loadTracksAndAlbumsFromArtist, this::showOptionsDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(listAdapter);
    }


    private void loadTracksAndAlbumsFromArtist(String artistName){
        getMainActivity().loadTracksFromArtist(artistName);
        sendMessage(this, NOTIFY_TO_DESELECT_PLAYLIST_ITEMS);
        toastLoaded();
    }


    private void showOptionsDialog(String artistName){
        Bundle bundle = new Bundle();
        bundle.putString(ArtistOptionsFragment.ARTIST_NAME_BUNDLE_KEY, artistName);
        FragmentManagerHelper.showDialog(this, ArtistOptionsFragment.newInstance(), "artist_options", bundle);
    }


    private void setVisibilityOnNoArtistsFoundText(List<String> tracks){
        setVisibilityOnNoItemsFoundText(tracks, recyclerView, noArtistsFoundTextView);
    }

    private MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }


    private void toastLoaded(){
        Toast.makeText(getContext(), getString(R.string.toast_artist_tracks_loaded), Toast.LENGTH_SHORT).show();
    }

}