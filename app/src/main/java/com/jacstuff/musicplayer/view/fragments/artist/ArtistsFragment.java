package com.jacstuff.musicplayer.view.fragments.artist;

import static com.jacstuff.musicplayer.MainActivity.SEND_ARTISTS_TO_FRAGMENT;
import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.sendMessage;
import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.setListener;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class ArtistsFragment extends Fragment {

    public final static String NOTIFY_TO_LOAD_ARTIST = "Notify_Artists_Fragment_To_Load_Artist";
    public final static String NOTIFY_TO_DESELECT_ITEMS = "Notify_Artists_Fragment_To_Deselect_Items";
    private RecyclerView recyclerView;
    private StringListAdapter listAdapter;
    private View parentView;

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
        refreshArtistsList();
        setupFragmentListener();
    }


    private void setupFragmentListener(){
        setListener(this, SEND_ARTISTS_TO_FRAGMENT, this::loadArtists);
        setListener(this, NOTIFY_TO_LOAD_ARTIST, (bundle) -> listAdapter.selectLongClickItem());
        setListener(this, NOTIFY_TO_DESELECT_ITEMS, (bundle) -> listAdapter.deselectCurrentlySelectedItem());
    }


    @SuppressLint("NotifyDataSetChanged")
    private void loadArtists(Bundle bundle){
        ArrayList<String> artistNames =  bundle.getStringArrayList(MainActivity.BUNDLE_KEY_ARTIST_UPDATES);
        listAdapter.setItems(artistNames);
        listAdapter.notifyDataSetChanged();
    }


    private void refreshArtistsList(){
        List<String> artists = getMainActivity().getArtistNames();
        if(this.parentView == null || artists == null){
            return;
        }
        listAdapter = new StringListAdapter(artists, this::loadTracksAndAlbumsFromArtist, this::showOptionsDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(listAdapter);
    }


    private void loadTracksAndAlbumsFromArtist(String artistName){
        getMainActivity().loadTracksFromArtist(artistName);
        sendMessage(this, PlaylistsFragment.NOTIFY_TO_DESELECT_ITEMS);
    }


    private void showOptionsDialog(String artistName){
        Bundle bundle = new Bundle();
        bundle.putString(ArtistOptionsFragment.ARTIST_NAME_BUNDLE_KEY, artistName);
        FragmentManagerHelper.showOptionsDialog(this, ArtistOptionsFragment.newInstance(), "artist_options", bundle);
    }


    private MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }
}