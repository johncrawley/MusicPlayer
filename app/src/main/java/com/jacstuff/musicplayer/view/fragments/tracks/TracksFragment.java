package com.jacstuff.musicplayer.view.fragments.tracks;


import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.setListener;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.db.playlist.Playlist;
import com.jacstuff.musicplayer.service.db.track.Track;
import com.jacstuff.musicplayer.view.utils.ButtonMaker;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TracksFragment extends Fragment{

    private RecyclerView recyclerView;
    private TrackListAdapter trackListAdapter;
    private int previousIndex = 0;
    private View parentView;
    private View addTracksToPlaylistButtonOuterLayout;
    private TextView noTracksFoundTextView, playlistInfoTextView;
    public final static String NOTIFY_USER_PLAYLIST_LOADED= "notify_user_playlist_loaded";
    public final static String IS_USER_PLAYLIST_LOADED_KEY= "is_user_playlist_loaded_key";

    public TracksFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tracks, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        this.parentView = view;
        recyclerView = parentView.findViewById(R.id.recyclerView);
        noTracksFoundTextView = parentView.findViewById(R.id.noTracksFoundTextView);
        playlistInfoTextView = parentView.findViewById(R.id.playlistNameTextView);
        setupRecyclerView(getMainActivity().getTrackList());
        setupAddTracksButton(view);
        getMainActivity().setPlayerFragment(this);
        setListener(this, NOTIFY_USER_PLAYLIST_LOADED, this::setVisibilityOnAddTracksToPlaylistButton);
    }


    public void updateTracksList(Playlist playlist, int currentTrackIndex){
        List<Track> updatedTracks = playlist.getTracks();
        updatePlaylistInfoView(playlist);
        refreshTrackList(updatedTracks);
        setVisibilityOnNoTracksFoundText(updatedTracks);
        setVisibilityOnAddTracksToPlaylistButton(playlist.isUserPlaylist());
        previousIndex = 0;
        if(currentTrackIndex < 0){
            setupRecyclerView(updatedTracks);
            return;
        }
        scrollToAndSelectListPosition(currentTrackIndex);
    }


    @SuppressWarnings("notifyDataSetChanged")
    public void refreshTrackList(List<Track> tracks){
        trackListAdapter.setItems(tracks);
        trackListAdapter.notifyDataSetChanged();
        setVisibilityOnNoTracksFoundText(tracks);
    }


    private void setupRecyclerView(List<Track> tracks){
        if(this.parentView == null ||tracks == null){
            return;
        }
        trackListAdapter = new TrackListAdapter(tracks, this::selectTrack, this::createTrackOptionsFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(trackListAdapter);
    }


    private void setVisibilityOnAddTracksToPlaylistButton(Bundle bundle){
        int visibility = bundle.getBoolean(IS_USER_PLAYLIST_LOADED_KEY) ? View.VISIBLE : View.INVISIBLE;
        addTracksToPlaylistButtonOuterLayout.setVisibility(visibility);
        setVisibilityOnAddTracksToPlaylistButton(bundle.getBoolean(IS_USER_PLAYLIST_LOADED_KEY) );
    }


    private void setupAddTracksButton(View parentView){
        addTracksToPlaylistButtonOuterLayout = parentView.findViewById(R.id.addTracksToPlaylistButtonOuterLayout);
        ButtonMaker.createImageButton(parentView, R.id.addTracksToPlaylistButton, this::addTracksToPlaylist);
    }


    private void addTracksToPlaylist(){
        MainActivity mainActivity = getMainActivity();
        if(mainActivity != null){
            mainActivity.getSearchViewHelper().showSearch(true);
        }
    }


    public void deselectCurrentItem(){
        if(trackListAdapter != null){
            trackListAdapter.deselectCurrentlySelectedItem();
        }
    }


    public void deselectCurrentItemAndNotify(){
        if(trackListAdapter != null){
            trackListAdapter.deselectCurrentlySelectedItemAndNotify();
        }
    }


    private MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }


    private void updatePlaylistInfoView(Playlist playlist){
        int resId = R.string.playlist_info_playlist_prefix;
        switch (playlist.getPlaylistType()){
            case ALBUM: resId = R.string.playlist_info_album_prefix; break;
            case ARTIST: resId = R.string.playlist_info_artist_prefix; break;
        }
        String info = getString(resId) + " " + playlist.getName();
        playlistInfoTextView.setText(info);
    }


    private void setVisibilityOnNoTracksFoundText(List<Track> tracks){
        if(tracks == null){
            return;
        }
        recyclerView.setVisibility(tracks.isEmpty()? View.GONE : View.VISIBLE);
        noTracksFoundTextView.setVisibility(tracks.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void log(String msg){
        System.out.println("^^^ TracksFragment: " + msg);
    }


    private void setVisibilityOnAddTracksToPlaylistButton(boolean isUserPlaylistLoaded){
        log("setVisibilityOnAddTracksToPlaylistButton() is userPlaylist? " + isUserPlaylistLoaded);
        int visibility = isUserPlaylistLoaded? View.VISIBLE : View.INVISIBLE;
        addTracksToPlaylistButtonOuterLayout.setVisibility(visibility);
    }


    private void createTrackOptionsFragment(Track track){
        String tag = "track_options_dialog";
        MainActivity mainActivity = getMainActivity();
        if(mainActivity == null){
            return;
        }
        mainActivity.setSelectedTrack(track);
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        removePreviousFragmentTransaction(tag, fragmentTransaction);
        TrackOptionsDialog trackOptionsDialog = TrackOptionsDialog.newInstance();
        trackOptionsDialog.show(fragmentTransaction, tag);
    }


    private void removePreviousFragmentTransaction(String tag, FragmentTransaction fragmentTransaction){
        Fragment prev = getParentFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
    }


    public void selectTrack(Track track){
        int position = track.getIndex();
        getMainActivity().selectTrack(position);
    }


    public void scrollToAndSelectListPosition(int index){
        if(trackListAdapter == null){
            return;
        }
        trackListAdapter.selectItemAt(index);
        //could use: smoothScrollToPosition(calculatedScrollIndex)
        // but it would take too long for large list
        recyclerView.scrollToPosition(calculateIndexWithOffset(index));
    }


    private int calculateIndexWithOffset(int index){
        int indexWithOffset = getItemScrollOffset(index);
        if ( indexWithOffset > trackListAdapter.getItemCount() || indexWithOffset < 0) {
            indexWithOffset = index;
        }
        previousIndex = index;
        return indexWithOffset;
    }


    private int getItemScrollOffset(int index){
        if(previousIndex == 0){
            return index;
        }
        int offset = index > previousIndex ? 4 : -4;
        return index + offset;
    }

}