package com.jacstuff.musicplayer.view.fragments.tracks;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.setListener;
import static com.jacstuff.musicplayer.view.fragments.Message.DESELECT_CURRENT_TRACK_ITEM;
import static com.jacstuff.musicplayer.view.fragments.Message.ENSURE_SELECTED_TRACK_IS_VISIBLE;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TRACKS_TAB_TO_RELOAD;
import static com.jacstuff.musicplayer.view.fragments.Message.SCROLL_TO_CURRENT_TRACK;
import static com.jacstuff.musicplayer.view.fragments.MessageKey.IS_USER_PLAYLIST;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_USER_PLAYLIST_LOADED;
import static com.jacstuff.musicplayer.view.fragments.Utils.getBoolean;
import static com.jacstuff.musicplayer.view.fragments.Utils.getInt;
import static com.jacstuff.musicplayer.view.utils.ListUtils.setVisibilityOnNoItemsFoundText;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.TextView;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.db.entities.Playlist;
import com.jacstuff.musicplayer.service.db.entities.Track;
import com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper;
import com.jacstuff.musicplayer.view.fragments.MessageKey;
import com.jacstuff.musicplayer.view.fragments.options.TrackOptionsDialog;
import com.jacstuff.musicplayer.view.utils.AnimatorHelper;
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
    private int listRefreshCount;
    private ViewGroup listHolder;
    private Animation fadeInListAnimation, fadeOutTextAnimation, fadeOutListAnimation, fadeInTextAnimation;
    private TextView loadingTextView;

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
        initFadeInListAnimation();
        assignPlaylist();
        setupAddTracksButton(view);
        setListeners();
        getMainActivity().requestTracksUpdate();
    }


    private void initFadeInListAnimation(){
        fadeInListAnimation = AnimatorHelper.createFadeInAnimation(getContext(), this::onListFadedIn);
        fadeOutListAnimation = AnimatorHelper.createFadeInAnimation(getContext(), this::onListFadedIn);
        fadeInTextAnimation = AnimatorHelper.createFadeInAnimation(getContext(), this::onLoadingTextFadedIn);
        fadeOutTextAnimation = AnimatorHelper.createFadeInAnimation(getContext(), this::onLoadingTextFadedOut);
    }


    private void onListFadedIn(){

    }


    private void onListFadedOut(){

    }



    private void onLoadingTextFadedIn(){

    }


    private void onLoadingTextFadedOut(){
        listHolder.setAnimation(fadeInListAnimation);
    }



    private void initViews(){
        noTracksFoundTextView = parentView.findViewById(R.id.noTracksFoundTextView);
        playlistInfoTextView = parentView.findViewById(R.id.playlistNameTextView);
        listHolder = parentView.findViewById(R.id.listHolder);
        loadingTextView = parentView.findViewById(R.id.loadingTracksText);
        initRecyclerView();
    }


    private void initRecyclerView(){
        recyclerView = parentView.findViewById(R.id.tracksRecyclerView);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }


    private void selectCurrentTrack(){
        if(getMainActivity() != null && getMainActivity().getMediaPlayerService() != null){
            int currentIndex = getMainActivity().getMediaPlayerService().getCurrentTrackIndex();
            if(playlist != null && !playlist.getTracks().isEmpty()){
                scrollToAndSelectListPosition(currentIndex);
            }
        }
    }


    private void setListeners(){
        setListener(this, NOTIFY_USER_PLAYLIST_LOADED, this::setVisibilityOnAddTracksToPlaylistButton);
        setListener(this, ENSURE_SELECTED_TRACK_IS_VISIBLE, this::ensureSelectedTrackIsVisible);
        setListener(this, DESELECT_CURRENT_TRACK_ITEM, this::deselectCurrentItem);
        setListener(this, NOTIFY_TRACKS_TAB_TO_RELOAD, this::updateTracksList);
        setListener(this, SCROLL_TO_CURRENT_TRACK, this::scrollToCurrentTrack);
    }


    private void updateTracksList(Bundle bundle){
        log("entered updateTracksList()");
        if(isBeingRefreshed.get()){
            return;
        }
        isBeingRefreshed.set(true);
        assignPlaylist();
        updatePlaylistInfoView(playlist);
        setVisibilityOnAddTracksToPlaylistButton(playlist.isUserPlaylist());
        previousIndex = 0;
        setupRecyclerView(parentView);
        loadingTextView.setVisibility(GONE);
        listHolder.setVisibility(VISIBLE);
        scrollToAndSelectListPosition(getInt(bundle, MessageKey.TRACK_INDEX));
        isBeingRefreshed.set(false);
    }


    private void assignPlaylist(){
        playlist = getMainActivity().getCurrentPlaylist();
        scrollToPreviouslySelectedTrack();
    }


    @Override
    public void onResume(){
        super.onResume();
        scrollToPreviouslySelectedTrack();
    }


    private void scrollToPreviouslySelectedTrack(){
        if(playlist == null){
            return;
        }
        int playlistSize = playlist.getTracks().size();
        var service = getMainActivity().getMediaPlayerService();
        if(service == null){
            return;
        }
        int selectedTrackIndex = service.getCurrentTrackIndex();
        if(playlistSize  > selectedTrackIndex){
            scrollToAndSelectListPosition(selectedTrackIndex);
        }
    }


    public void selectTrack(Track track){
        int position = track.getIndex();
        getMainActivity().selectTrack(position);
        previousIndex = position;
    }


    private void scrollToAndSelectListPosition(int index){
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
        new Handler(Looper.getMainLooper())
                .postDelayed(runnable, 150);
    }


    private void setupRecyclerView(View parentView){
        log("entered setupRecyclerView()");
        if(parentView == null
                || playlist == null
                || playlist.getTracks() == null
                || !isMediaPlayerServiceAvailable()){
            return;
        }
        listRefreshCount++;
        trackListAdapter = new TrackListAdapter(playlist, this::selectTrack, this::createTrackOptionsFragment);
        recyclerView.setAdapter(trackListAdapter);
        updatePlaylistInfoView(playlist);
        setVisibilityOnNoTracksFoundText();
        getMain().ifPresent(activity -> activity.notifyNumberOfTracks(playlist.getTracks().size()));
    }


    private void log(String msg){
        System.out.println("^^^ TracksFragment: " + msg);
    }


    private void setVisibilityOnAddTracksToPlaylistButton(Bundle bundle){
        boolean isUserPlaylistLoaded = getBoolean(bundle,IS_USER_PLAYLIST);
        addTracksToPlaylistButtonOuterLayout.setVisibility(isUserPlaylistLoaded ? VISIBLE : View.INVISIBLE);
        setVisibilityOnAddTracksToPlaylistButton(isUserPlaylistLoaded );
    }


    private boolean isMediaPlayerServiceAvailable(){
        var mainActivity = getMainActivity();
        if(mainActivity != null){
            var mps = mainActivity.getMediaPlayerService();
            return mps != null;
        }
        return false;
    }


    private void setupAddTracksButton(View parentView){
        addTracksToPlaylistButtonOuterLayout = parentView.findViewById(R.id.addButtonOuterLayout);
        ButtonMaker.initImageButton(parentView, R.id.addButton, this::addTracksToPlaylist);
        getMain().ifPresent(ma -> addTracksToPlaylistButtonOuterLayout.setVisibility(ma.isUserPlaylistLoaded() ? VISIBLE : View.INVISIBLE));
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
            case PLAYLIST -> R.string.list_prefix;
            case ALBUM   -> R.string.album_prefix;
            case ARTIST -> R.string.artist_prefix;
            case GENRE -> R.string.genre_prefix;
        };
        String prefix = getString(resId);
        String info = resId == R.string.default_playlist_info ? prefix : prefix + " " + playlist.getName();
        playlistInfoTextView.setText(info);
    }


    private void setVisibilityOnNoTracksFoundText(){
        new Handler(Looper.getMainLooper()).postDelayed(() -> showNoTracksIfNotBeingRefreshed(listRefreshCount), 1000);
    }


    private void showNoTracksIfNotBeingRefreshed(int oldRefreshCount){
        if(isBeingRefreshed.get()
                || listRefreshCount != oldRefreshCount
                || getContext() == null){
            return;
        }
        setVisibilityOnNoItemsFoundText(playlist.getTracks(), recyclerView, noTracksFoundTextView, noTracksFoundStr);
    }


    private void setVisibilityOnAddTracksToPlaylistButton(boolean isUserPlaylistLoaded){
        int visibility = isUserPlaylistLoaded? VISIBLE : View.INVISIBLE;
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