package com.jacstuff.musicplayer.fragments.artist;

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
import com.jacstuff.musicplayer.db.artist.Artist;
import com.jacstuff.musicplayer.db.artist.ArtistRepository;
import com.jacstuff.musicplayer.fragments.playlist.PlaylistsFragment;
import com.jacstuff.musicplayer.utils.ButtonMaker;

import java.util.List;

public class ArtistsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArtistListAdapter listAdapter;
    private int previousIndex = 0;
    private View parentView;
    private ArtistRepository artistRepository;
    private Button loadTracksFromArtistButton, addTracksToPlaylistButton;

    public ArtistsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artists, container, false);
        artistRepository = new ArtistRepository(getContext());
        return view;
    }


    @Override
    public void onViewCreated(View view,  Bundle savedInstanceState){
        this.parentView = view;
        recyclerView = parentView.findViewById(R.id.artistsRecyclerView);
        setupButtons(parentView);
        refreshArtistsList();
        setupFragmentListener();
    }


    private void setupFragmentListener(){
        getParentFragmentManager().setFragmentResultListener(PlaylistsFragment.NOTIFY_ARTISTS_FRAGMENT_OF_PLAYLIST, this, (requestKey, bundle) -> {
            int visibility = isBundleUserPlaylistLoaded(bundle) && isItemSelected()? View.VISIBLE : View.INVISIBLE;
            addTracksToPlaylistButton.setVisibility(visibility);
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


    private Artist getSelectedArtist(){
        return listAdapter.getCurrentlySelectedItem();
    }


    public void notifyCurrentlySelectedTrack(int position){
        getMainActivity().selectTrack(position);
    }


    private MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }


    private void refreshArtistsList(){
        List<Artist> artists = artistRepository.getAllArtists();
        if(this.parentView == null ||artists == null){
            return;
        }
        listAdapter = new ArtistListAdapter(artists, this::setButtonsVisibility);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(listAdapter);
    }


    @SuppressLint("NotifyDataSetChanged")
    private void updateTrackViews(){
        listAdapter.notifyDataSetChanged();
    }



    private void setButtonsVisibility(Artist artist){
        addTracksToPlaylistButton.setVisibility(getVisibilityForAddTracksButton());
        loadTracksFromArtistButton.setVisibility(View.VISIBLE);
    }


    private int getVisibilityForAddTracksButton(){
        return getMainActivity().getMediaPlayerService().getPlaylistManager().isUserPlaylistLoaded() ? View.VISIBLE : View.INVISIBLE;
    }


}