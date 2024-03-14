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
import com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper;
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
    private final int SCROLL_OFFSET = 4;

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
        //setupRecyclerView(getMainActivity().getTrackList());
        setupRecyclerView(getMainActivity().getCurrentPlaylist());
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
        if(tracks == null){
            return;
        }
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


    private void setupRecyclerView(Playlist playlist){
        List<Track> tracks = playlist.getTracks();
        if(this.parentView == null ||tracks == null){
            return;
        }
        trackListAdapter = new TrackListAdapter(tracks, this::selectTrack, this::createTrackOptionsFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(trackListAdapter);
        updatePlaylistInfoView(playlist);
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


    private MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }


    private void updatePlaylistInfoView(Playlist playlist){
        int resId = switch (playlist.getType()){
            case ALL_TRACKS -> R.string.default_playlist_info;
            case PLAYLIST -> R.string.playlist_info_playlist_prefix;
            case ALBUM   -> R.string.playlist_info_album_prefix;
            case ARTIST -> R.string.playlist_info_artist_prefix;
            case GENRE -> R.string.playlist_info_genre_prefix;
        };
        String prefix = getString(resId);
        String info = resId == R.string.default_playlist_info ? prefix : prefix + " " + playlist.getName();
        playlistInfoTextView.setText(info);
    }


    private void setVisibilityOnNoTracksFoundText(List<Track> tracks){
        if(tracks == null){
            return;
        }
        recyclerView.setVisibility(tracks.isEmpty()? View.GONE : View.VISIBLE);
        noTracksFoundTextView.setVisibility(tracks.isEmpty() ? View.VISIBLE : View.GONE);
    }


    private void setVisibilityOnAddTracksToPlaylistButton(boolean isUserPlaylistLoaded){
        int visibility = isUserPlaylistLoaded? View.VISIBLE : View.INVISIBLE;
        addTracksToPlaylistButtonOuterLayout.setVisibility(visibility);
    }


    private void createTrackOptionsFragment(Track track) {
        MainActivity mainActivity = getMainActivity();
        if (mainActivity == null) {
            return;
        }
        mainActivity.setSelectedTrack(track);
        FragmentManagerHelper.showDialog(this, TrackOptionsDialog.newInstance(), "track_options_dialog", new Bundle());
    }


    public void selectTrack(Track track){
        int position = track.getIndex();
        getMainActivity().selectTrack(position);
    }


    public void scrollToAndSelectListPosition(int index){
        scrollToAndSelectListPosition(index, false);
    }


    public void scrollToAndSelectListPosition(int index, boolean isSearchResult){
        if(trackListAdapter == null){
            return;
        }
        trackListAdapter.selectItemAt(index);
        scrollToOffsetPosition(index, isSearchResult);
        scrollDownAPixel();
    }


    private void scrollToOffsetPosition(int index, boolean isSearchResult){
        //could use: smoothScrollToPosition(calculatedScrollIndex)
        // but it would take too long for large list
        int offsetSetPosition = calculateIndexWithOffset(index, isSearchResult);
        recyclerView.scrollToPosition(offsetSetPosition);
    }


    /* for some reason, after scrolling up using RecyclerView's scrollToPosition method,
        opening and closing the search window will make the track list jump back to the top.
        We can avoid this behaviour by scrolling down a further one pixel after a scroll.
     */
    private void scrollDownAPixel(){
        recyclerView.scrollBy(0,1);
    }


    public void ensureSelectedTrackIsVisible(int trackIndex){
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if(layoutManager == null){
           return;
        }
        int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
        if(trackIndex < firstVisiblePosition || firstVisiblePosition < 0){
            recyclerView.scrollToPosition(trackIndex);
        }
    }


    private int calculateIndexWithOffset(int index, boolean isSearchResult){
        int offsetIndex = isSearchResult && previousIndex < index ?
                Math.max(0, index - SCROLL_OFFSET)
                : getBoundedIndexWithOffset(index);
        previousIndex = index;
        return offsetIndex;
    }


    private int getBoundedIndexWithOffset(int index){
        int indexWithOffset = index + getScrollOffset(index);
        return indexWithOffset > trackListAdapter.getItemCount() || indexWithOffset < 0 ? index : indexWithOffset;
    }


    private int getScrollOffset(int index){
        return previousIndex == 0 ? 0 : index > previousIndex ? SCROLL_OFFSET : -SCROLL_OFFSET;
    }

}