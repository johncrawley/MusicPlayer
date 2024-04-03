package com.jacstuff.musicplayer.view.fragments.tracks;


import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.setListener;
import static com.jacstuff.musicplayer.view.fragments.Message.DESELECT_CURRENT_TRACK_ITEM;
import static com.jacstuff.musicplayer.view.fragments.Message.ENSURE_SELECTED_TRACK_IS_VISIBLE;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TO_REQUEST_UPDATED_PLAYLIST;
import static com.jacstuff.musicplayer.view.fragments.Message.SCROLL_TO_CURRENT_TRACK;
import static com.jacstuff.musicplayer.view.fragments.MessageKey.IS_USER_PLAYLIST;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_USER_PLAYLIST_LOADED;
import static com.jacstuff.musicplayer.view.fragments.about.Utils.getBoolean;
import static com.jacstuff.musicplayer.view.fragments.about.Utils.getInt;
import static com.jacstuff.musicplayer.view.utils.ListUtils.setVisibilityOnNoItemsFoundText;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.db.entities.Playlist;
import com.jacstuff.musicplayer.service.db.entities.Track;
import com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper;
import com.jacstuff.musicplayer.view.fragments.MessageKey;
import com.jacstuff.musicplayer.view.utils.ButtonMaker;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
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
    private boolean isFirstScroll;


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
        isFirstScroll = true;
        this.parentView = view;
        initViews();
        setupRecyclerView(view, getMainActivity().getCurrentPlaylist());
        setupAddTracksButton(view);
        getMainActivity().notifyTracksFragmentReady();
        setListeners();
        scrollToAndSelectListPosition(0, false);
        handleSavedScroll();
    }


    private void initViews(){
        noTracksFoundTextView = parentView.findViewById(R.id.noTracksFoundTextView);
        playlistInfoTextView = parentView.findViewById(R.id.playlistNameTextView);
    }


    private void setListeners(){
        setListener(this, NOTIFY_USER_PLAYLIST_LOADED, this::setVisibilityOnAddTracksToPlaylistButton);
        setListener(this, ENSURE_SELECTED_TRACK_IS_VISIBLE, this::ensureSelectedTrackIsVisible);
        setListener(this, DESELECT_CURRENT_TRACK_ITEM, this::deselectCurrentItem);
        setListener(this, NOTIFY_TO_REQUEST_UPDATED_PLAYLIST, this::updateTracksList);
        setListener(this, SCROLL_TO_CURRENT_TRACK, this::scrollToCurrentTrack);
    }


    public void updateTracksList(Bundle bundle){
        if(getMainActivity() == null){
            return;
        }
        Playlist playlist = getMainActivity().getPlaylist();
        int currentTrackIndex = getInt(bundle, MessageKey.TRACK_INDEX);
        List<Track> updatedTracks = playlist.getTracks();
        updatePlaylistInfoView(playlist);
        refreshTrackList(updatedTracks);
        setVisibilityOnNoTracksFoundText(updatedTracks);
        setVisibilityOnAddTracksToPlaylistButton(playlist.isUserPlaylist());
        previousIndex = 0;
        // we need to reinitialize the recycler view here
        //  because otherwise loading a different playlist will cause a list overlap visual bug
        if(currentTrackIndex < 0){
            setupRecyclerView(parentView, playlist);
            return;
        }
        scrollToAndSelectListPosition(currentTrackIndex);
    }


    @SuppressWarnings("notifyDataSetChanged")
    public void refreshTrackList(List<Track> tracks){
        runOnUIThread( ()->{
            if(tracks == null){
                return;
            }
            trackListAdapter.setItems(tracks);
            trackListAdapter.notifyDataSetChanged();
            setVisibilityOnNoTracksFoundText(tracks);
        });
    }


    private void runOnUIThread(Runnable runnable){
        new Handler(Looper.getMainLooper()).post(runnable);
    }


    public void selectTrack(Track track){
        int position = track.getIndex();
        getMainActivity().selectTrack(position);
    }


    public void scrollToAndSelectListPosition(int index){
        scrollToAndSelectListPosition(index, false);
    }


    private void ensureSelectedTrackIsVisible(Bundle bundle){
        int trackIndex = getInt(bundle, MessageKey.TRACK_INDEX);
        var layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if(layoutManager == null){
            return;
        }
        int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
        if(trackIndex < firstVisiblePosition || firstVisiblePosition < 0){
            recyclerView.scrollToPosition(trackIndex);
        }
    }


    public void deselectCurrentItem(Bundle bundle){
        if(trackListAdapter != null){
            trackListAdapter.deselectCurrentlySelectedItem();
        }
    }


    private void handleSavedScroll(){
        if(getMainActivity().isTracksFragmentScrollIndexSaved()){
            scrollToAndSelectListPosition(getMainActivity().getSavedScrollIndex());
        }
    }


    private void scrollToCurrentTrack(Bundle bundle){
        int index = getInt(bundle, MessageKey.TRACK_INDEX);
        boolean isSearchResult = getBoolean(bundle, MessageKey.IS_SEARCH_RESULT);
        saveScrollIndex(index);
        if(isFirstScroll){
            handleFirstScroll(index, isSearchResult);
        }
        else{
            scrollToAndSelectListPosition(index, isSearchResult);
        }
    }


    private void saveScrollIndex(int index){
        MainActivity mainActivity = getMainActivity();
        if(mainActivity == null){
            return;
        }
        if(getLifecycle().getCurrentState() != Lifecycle.State.STARTED){
            mainActivity.saveTrackIndexToScrollTo(index);
            return;
        }
        mainActivity.cancelSavedScrollIndex();
    }


    private void handleFirstScroll(int index, boolean isSearchResult){
        isFirstScroll = false;
        runAfterDelay(()-> scrollToAndSelectListPosition(index, isSearchResult));
    }


    public void scrollToAndSelectListPosition(int index, boolean isSearchResult){
        if(trackListAdapter == null){
            return;
        }
        trackListAdapter.selectItemAt(index);
        scrollToOffsetPosition(index, isSearchResult);
        scrollDownAPixel();
    }


    private void runAfterDelay(Runnable runnable){
        new Handler(Looper.getMainLooper()).postDelayed(runnable, 150);
    }


    private void setupRecyclerView(View parentView, Playlist playlist){
        List<Track> tracks = playlist.getTracks();

        if(parentView == null ||tracks == null){
            return;
        }
        recyclerView = parentView.findViewById(R.id.recyclerView);
        trackListAdapter = new TrackListAdapter(tracks, this::selectTrack, this::createTrackOptionsFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(trackListAdapter);
        updatePlaylistInfoView(playlist);
        setVisibilityOnNoTracksFoundText(playlist.getTracks());
    }


    private void setVisibilityOnAddTracksToPlaylistButton(Bundle bundle){
        boolean isUserPlaylistLoaded = getBoolean(bundle,IS_USER_PLAYLIST);
        addTracksToPlaylistButtonOuterLayout.setVisibility(isUserPlaylistLoaded ? View.VISIBLE : View.INVISIBLE);
        setVisibilityOnAddTracksToPlaylistButton(isUserPlaylistLoaded );
    }


    private void setupAddTracksButton(View parentView){
        addTracksToPlaylistButtonOuterLayout = parentView.findViewById(R.id.addTracksToPlaylistButtonOuterLayout);
        ButtonMaker.createImageButton(parentView, R.id.addTracksToPlaylistButton, this::addTracksToPlaylist);
    }


    private void addTracksToPlaylist(){
        var mainActivity = getMainActivity();
        if(mainActivity != null){
            mainActivity.getSearchViewHelper().showSearch(true);
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
        setVisibilityOnNoItemsFoundText(tracks, recyclerView, noTracksFoundTextView);
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


    private void scrollToOffsetPosition(int index, boolean isSearchResult){
        if(index == previousIndex + 1 || index == previousIndex -1){
            recyclerView.smoothScrollToPosition(index);
            return;
        }
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