package com.jacstuff.musicplayer.view.fragments.artist;

import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.sendMessages;
import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.setListener;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_ARTISTS_TAB_TO_RESELECT_ITEM;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TO_DESELECT_ARTIST_ITEMS;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TO_DESELECT_GENRE_ITEMS;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TO_DESELECT_PLAYLIST_ITEMS;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TO_LOAD_ARTIST;
import static com.jacstuff.musicplayer.view.fragments.Message.LOAD_ARTISTS;
import static com.jacstuff.musicplayer.view.fragments.MessageKey.ARTIST_UPDATES;
import static com.jacstuff.musicplayer.view.utils.ListUtils.setVisibilityOnNoItemsFoundText;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;

public class ArtistsFragment extends Fragment {

    private RecyclerView recyclerView;
    private StringListAdapter listAdapter;
    private View parentView;
    private TextView noArtistsFoundTextView;
    private ListIndexManager listIndexManager;

    public ArtistsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_artists, container, false);
    }


    @Override
    public void onViewCreated(@androidx.annotation.NonNull View view, Bundle savedInstanceState){
        this.parentView = view;
        assignListIndexManager();
        recyclerView = parentView.findViewById(R.id.artistsRecyclerView);
        noArtistsFoundTextView = parentView.findViewById(R.id.noArtistsFoundTextView);
        refreshArtistsList();
        setupFragmentListener();
        selectSavedIndex();
    }


    private void assignListIndexManager(){
        MediaPlayerService mediaPlayerService = getMainActivity().getMediaPlayerService();
        if(mediaPlayerService != null){
            listIndexManager = mediaPlayerService.getListIndexManager();
        }
    }


    private void selectSavedIndex(){
        if(listIndexManager != null){
            listIndexManager.getArtistIndex().ifPresent(this::scrollToAndSelect);
        }
    }


    private void scrollToAndSelect(int index){
        listAdapter.selectItemAt(index);
        recyclerView.scrollToPosition(index);
    }


    private void setupFragmentListener(){
        setListener(this, LOAD_ARTISTS, this::populateArtistsList);
        setListener(this, NOTIFY_TO_LOAD_ARTIST, (bundle) -> listAdapter.selectLongClickItem());
        setListener(this, NOTIFY_TO_DESELECT_ARTIST_ITEMS, (bundle) -> listAdapter.deselectCurrentlySelectedItem());
        setListener(this, NOTIFY_ARTISTS_TAB_TO_RESELECT_ITEM, (bundle) -> this.reselectItemAfterServiceConnection() );
    }


    private void reselectItemAfterServiceConnection(){
        assignListIndexManager();
        selectSavedIndex();
    }


    private void populateArtistsList(Bundle bundle){
        ArrayList<String> artistNames =  bundle.getStringArrayList(ARTIST_UPDATES.toString());
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


    private void loadTracksAndAlbumsFromArtist(String artistName, int position){
        getMainActivity().loadTracksFromArtist(artistName);
        assignIndex(position);
        notifyOtherFragmentsToDeselectItems();
        toastLoaded();
    }


    private void assignIndex(int index){
        if(listIndexManager == null){
            assignListIndexManager();
        }
        if(listIndexManager != null){
            listIndexManager.setArtistIndex(index);
        }
    }


    private void notifyOtherFragmentsToDeselectItems(){
        sendMessages(this,
                NOTIFY_TO_DESELECT_PLAYLIST_ITEMS,
                NOTIFY_TO_DESELECT_GENRE_ITEMS); //NB album list will be reloaded anyway
    }


    private void showOptionsDialog(String artistName){
        Bundle bundle = new Bundle();
        bundle.putString(ArtistOptionsFragment.ARTIST_NAME_BUNDLE_KEY, artistName);
        FragmentManagerHelper.showDialog(this, ArtistOptionsFragment.newInstance(), "artist_options", bundle);
    }


    private void setVisibilityOnNoArtistsFoundText(List<String> tracks){
        setVisibilityOnNoItemsFoundText(tracks, recyclerView, noArtistsFoundTextView);
    }


    private void toastLoaded(){
        getMainActivity().toastIfTabsNotAutoSwitched(R.string.toast_artist_tracks_loaded);
    }


    private MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }


}