package com.jacstuff.musicplayer.view.search;

import static com.jacstuff.musicplayer.view.utils.AnimatorHelper.createShowAnimatorFor;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.MediaPlayerService;
import com.jacstuff.musicplayer.service.db.track.Track;
import com.jacstuff.musicplayer.view.list.SearchResultsListAdapter;
import com.jacstuff.musicplayer.view.playlist.AddTrackToPlaylistViewHelper;
import com.jacstuff.musicplayer.view.utils.AnimatorHelper;
import com.jacstuff.musicplayer.view.utils.ButtonMaker;
import com.jacstuff.musicplayer.view.utils.KeyboardHelper;

import java.util.Collections;
import java.util.List;

public class SearchViewHelper {

    private final MainActivity mainActivity;
    private final KeyboardHelper keyboardHelper;
    private RecyclerView searchResultsRecyclerView;
    private Button addSearchResultButton, enqueueSearchResultButton, playSearchResultButton;
    private SearchResultsListAdapter searchResultsListAdapter;
    private Track selectedSearchResultTrack;
    private EditText searchEditText;
    private View searchView;
    private boolean hasSearchResultBeenPlayed = false;
    private OnBackPressedCallback dismissSearchViewOnBackPressedCallback;
    private MediaPlayerService mediaPlayerService;
    private final AddTrackToPlaylistViewHelper addTrackToPlaylistViewHelper;


    public SearchViewHelper(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        addTrackToPlaylistViewHelper = mainActivity.getAddTrackToPlaylistViewHelper();
        keyboardHelper = new KeyboardHelper(mainActivity);
        setupViews();
        setupDismissSearchOnBackPressed();
    }


    private void setupSearchViewButtons(){
        addSearchResultButton       = ButtonMaker.createButton(searchView, R.id.addSelectedButton, this::addSelectedSearchResultToPlaylist);
        playSearchResultButton      = ButtonMaker.createButton(searchView, R.id.playSelectedButton, this::playSelectedSearchResult);
        enqueueSearchResultButton   = ButtonMaker.createButton(searchView, R.id.playNextButton, this::addSearchResultToQueue);
    }


    private void setupViews() {
        searchView = mainActivity.findViewById(R.id.searchView);
        searchEditText = mainActivity.findViewById(R.id.trackSearchEditText);
        searchResultsRecyclerView = mainActivity.findViewById(R.id.searchResultsRecyclerView);
        setupSearchRecyclerView(Collections.emptyList());
        setupSearchKeyListener();
        setupSearchViewButtons();
    }


    public void setMediaPlayerService(MediaPlayerService mediaPlayerService){
        this.mediaPlayerService = mediaPlayerService;
    }


    public void toggleSearch(){
        if(searchView.getVisibility() == View.VISIBLE){
            hideSearch();
            return;
        }
        showSearch();
    }


    private void showSearch(){
        addTrackToPlaylistViewHelper.hideView();
        hideAllSearchResultsButtons();
        Animator animator = createShowAnimatorFor(searchView, ()-> keyboardHelper.showKeyboardAndFocusOn(searchEditText));
        searchView.setVisibility(View.VISIBLE);
        dismissSearchViewOnBackPressedCallback.setEnabled(true);
        animator.start();
    }


    private void addSelectedSearchResultToPlaylist(){
        if(selectedSearchResultTrack != null){
            mediaPlayerService.addTrackToCurrentPlaylist(selectedSearchResultTrack);
        }
    }


    private void setupDismissSearchOnBackPressed(){
        dismissSearchViewOnBackPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                hideSearch();
            }
        };
        mainActivity.getOnBackPressedDispatcher().addCallback(mainActivity, dismissSearchViewOnBackPressedCallback);
    }


    private void hideSearch(){
        if(searchView.getVisibility() != View.VISIBLE){
            return;
        }
        Animator animator = AnimatorHelper.createHideAnimatorFor(searchView, ()->{
            searchView.setVisibility(View.GONE);
            searchEditText.setText("");
            clearSearchResults();
            scrollToPositionIfSearchResultHasBeenPlayed();
        });
        keyboardHelper.hideKeyboard(searchView);
        dismissSearchViewOnBackPressedCallback.setEnabled(false);
        animator.start();
    }


    private void scrollToPositionIfSearchResultHasBeenPlayed(){
        if(hasSearchResultBeenPlayed){
            mainActivity.scrollToTrack(selectedSearchResultTrack);
        }
        hasSearchResultBeenPlayed = false;
    }


    private void setupSearchRecyclerView(List<Track> tracks){
        if(tracks == null){
            return;
        }
        searchResultsListAdapter = new SearchResultsListAdapter(tracks, this::onSearchResultSelect);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(mainActivity));
        searchResultsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        searchResultsRecyclerView.setAdapter(searchResultsListAdapter);
    }


    private void onSearchResultSelect(Track track){
        selectedSearchResultTrack = track;
        showSearchResultsButtons();
    }


    @SuppressLint("NotifyDataSetChanged")
    private void setSearchResults(List<Track> tracks){
        searchResultsListAdapter.setTracks(tracks);
        searchResultsListAdapter.notifyDataSetChanged();
    }


    private void clearSearchResults(){
        setSearchResults(Collections.emptyList());
    }


    private void setupSearchKeyListener(){
        KeyListenerHelper.setListener(searchEditText, () ->{
            List<Track> tracks = getTracksForSearch(searchEditText.getText().toString());
            setSearchResults(tracks);
        });
    }


    private List<Track> getTracksForSearch(String str){
        if(mediaPlayerService == null){
            return Collections.emptyList();
        }
        return mediaPlayerService.getTracksForSearch(str);
    }


    public void hideAllSearchResultsButtons(){
        addSearchResultButton.setVisibility(View.GONE);
        playSearchResultButton.setVisibility(View.GONE);
        enqueueSearchResultButton.setVisibility(View.GONE);
    }


    public void showSearchResultsButtons(){
        playSearchResultButton.setVisibility(View.VISIBLE);
        enqueueSearchResultButton.setVisibility(View.VISIBLE);
        if(mainActivity.isUserPlaylistLoaded()){
            addSearchResultButton.setVisibility(View.VISIBLE);
        }
    }


    public void playSelectedSearchResult(){
        mainActivity.disableViewForAWhile(playSearchResultButton);
        if(selectedSearchResultTrack != null) {
            mediaPlayerService.selectAndPlayTrack(selectedSearchResultTrack);
            hasSearchResultBeenPlayed = true;
        }
    }


    public void addSearchResultToQueue(){
        if(selectedSearchResultTrack != null){
           mainActivity.enqueue(selectedSearchResultTrack);
        }
    }
}
