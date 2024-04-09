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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private LinearLayoutManager layoutManager;
    private final AtomicBoolean isBeingRefreshed = new AtomicBoolean(false);
    private Playlist playlist;
    private String noTracksFoundStr = "";

    public TracksFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_tracks, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        isFirstScroll = true;
        this.parentView = view;
        noTracksFoundStr = getString(R.string.no_tracks_found);
        initViews();
        assignPlaylist();
        setupRecyclerView(view);
        setupAddTracksButton(view);
        getMainActivity().notifyTracksFragmentReady();
        setListeners();
        selectCurrentTrack();
    }


    private void initViews(){
        initRecyclerView();
        noTracksFoundTextView = parentView.findViewById(R.id.noTracksFoundTextView);
        playlistInfoTextView = parentView.findViewById(R.id.playlistNameTextView);
    }

    private void initRecyclerView(){
        recyclerView = parentView.findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }


    private void selectCurrentTrack(){
        if(getMainActivity() != null && getMainActivity().getMediaPlayerService() != null){
            int currentIndex = getMainActivity().getMediaPlayerService().getCurrentTrackIndex();
            scrollToAndSelectListPosition(currentIndex);
        }
    }


    private void assignPlaylist(){
        playlist = getMainActivity().getCurrentPlaylist();
    }


    private void setListeners(){
        setListener(this, NOTIFY_USER_PLAYLIST_LOADED, this::setVisibilityOnAddTracksToPlaylistButton);
        setListener(this, ENSURE_SELECTED_TRACK_IS_VISIBLE, this::ensureSelectedTrackIsVisible);
        setListener(this, DESELECT_CURRENT_TRACK_ITEM, this::deselectCurrentItem);
        setListener(this, NOTIFY_TO_REQUEST_UPDATED_PLAYLIST, this::updateTracksList);
        setListener(this, SCROLL_TO_CURRENT_TRACK, this::scrollToCurrentTrack);
    }


    private void updateTracksList(Bundle bundle){
        isBeingRefreshed.set(true);
        assignPlaylist();
        updatePlaylistInfoView(playlist);
        setVisibilityOnAddTracksToPlaylistButton(playlist.isUserPlaylist());
        previousIndex = 0;
        setupRecyclerView(parentView);
        scrollToAndSelectListPosition(getInt(bundle, MessageKey.TRACK_INDEX));
        isBeingRefreshed.set(false);
    }


    public void selectTrack(Track track){
        int position = track.getIndex();
        getMainActivity().selectTrack(position);
        previousIndex = position;
    }


    public void scrollToAndSelectListPosition(int index){
        if(index >= 0) {
            scrollToIndex(index, false);
        }
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


    private void scrollToCurrentTrack(Bundle bundle){
        int index = getInt(bundle, MessageKey.TRACK_INDEX);
        boolean isSearchResult = getBoolean(bundle, MessageKey.IS_SEARCH_RESULT);
        saveScrollIndex(index);
        scrollToIndex(index, isSearchResult);
    }


    private void scrollToIndex(int index, boolean isSearchResult){
        if(isFirstScroll){
            handleFirstScroll(index, isSearchResult);
            return;
        }
        scrollToAndSelectListPosition(index, isSearchResult);
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


    private void setupRecyclerView(View parentView){
        if(parentView == null || playlist == null || playlist.getTracks() == null){
            return;
        }
        trackListAdapter = new TrackListAdapter(playlist, this::selectTrack, this::createTrackOptionsFragment);
        recyclerView.setAdapter(trackListAdapter);
        updatePlaylistInfoView(playlist);
        setVisibilityOnNoTracksFoundText();
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
        getMain().ifPresent(m -> m.getSearchViewHelper().showSearch(true));
    }


    private MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }


    private Optional<MainActivity> getMain(){
        return Optional.ofNullable((MainActivity) getActivity());
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


    private void setVisibilityOnNoTracksFoundText(){
        new Handler(Looper.getMainLooper()).postDelayed(this::showNoTracksIfNotBeingRefreshed, 1000);
    }


    private void showNoTracksIfNotBeingRefreshed(){
        if(!isBeingRefreshed.get() || getContext() != null){
            setVisibilityOnNoItemsFoundText(playlist.getTracks(), recyclerView, noTracksFoundTextView, noTracksFoundStr);
        }
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
        if(isShortJumpTo(index)) {
            recyclerView.smoothScrollToPosition(index);
        }
        else {
            int offsetSetPosition = calculateIndexWithOffset(index, isSearchResult);
            recyclerView.scrollToPosition(offsetSetPosition);
        }
        previousIndex = index;
    }


    private boolean isShortJumpTo(int index){
        int indexDiff = Math.abs(index - previousIndex);
        int visiblePositionDiff = Math.abs(index - getFirstVisiblePosition());
        int smoothScrollLimit = 15;
        return indexDiff < smoothScrollLimit && visiblePositionDiff < smoothScrollLimit;
    }


    private int getFirstVisiblePosition(){
        return layoutManager == null ? 0 : layoutManager.findFirstVisibleItemPosition();
    }


    /* for some reason, after scrolling up using RecyclerView's scrollToPosition method,
        opening and closing the search window will make the track list jump back to the top.
        We can avoid this behaviour by scrolling down a further one pixel after a scroll.
     */
    private void scrollDownAPixel(){
        recyclerView.scrollBy(0,1);
    }


    private int calculateIndexWithOffset(int index, boolean isSearchResult){
        return isSearchResult ? index : getBoundedIndexWithOffset(index);
    }


    private int getBoundedIndexWithOffset(int index){
        int indexWithOffset = index + getScrollOffset(index);
        return indexWithOffset > trackListAdapter.getItemCount() || indexWithOffset < 0 ? index : indexWithOffset;
    }


    private int getScrollOffset(int index){
        if(Math.abs(previousIndex - index) < 2){
            return 0;
        }
        int scrollOffset = 4;
        return previousIndex == 0 ? 0 :
                index > getFirstVisiblePosition() ? scrollOffset : -scrollOffset;
    }

}