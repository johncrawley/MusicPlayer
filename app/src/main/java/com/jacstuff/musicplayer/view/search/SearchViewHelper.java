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
import com.jacstuff.musicplayer.service.db.entities.Track;
import com.jacstuff.musicplayer.view.utils.AddTrackToPlaylistViewHelper;
import com.jacstuff.musicplayer.view.utils.AnimatorHelper;
import com.jacstuff.musicplayer.view.utils.ButtonMaker;
import com.jacstuff.musicplayer.view.utils.KeyboardHelper;

import java.util.Collections;
import java.util.List;

public class SearchViewHelper {

    private final MainActivity mainActivity;
    private final KeyboardHelper keyboardHelper;
    private RecyclerView recyclerView;
    private Button addSearchResultButton, enqueueSearchResultButton, playSearchResultButton;
    private SearchResultsListAdapter searchResultsListAdapter;
    private Track selectedSearchResultTrack;
    private EditText searchEditText;
    private View searchView;
    private boolean hasSearchResultBeenPlayed = false;
    private OnBackPressedCallback dismissSearchViewOnBackPressedCallback;
    private MediaPlayerService mediaPlayerService;
    private final AddTrackToPlaylistViewHelper addTrackToPlaylistViewHelper;
    private boolean isAddingTrackToPlaylist;


    public SearchViewHelper(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        addTrackToPlaylistViewHelper = mainActivity.getAddTrackToPlaylistViewHelper();
        keyboardHelper = new KeyboardHelper(mainActivity);
        setupViews();
        setupDismissSearchOnBackPressed();
    }


    private void setupViews() {
        searchView = mainActivity.findViewById(R.id.searchView);
        searchEditText = mainActivity.findViewById(R.id.trackSearchEditText);
        recyclerView = mainActivity.findViewById(R.id.searchResultsRecyclerView);
        setupRecyclerView();
        setupKeyListener();
        setupButtons();
    }


    private void setupButtons(){
        addSearchResultButton       = ButtonMaker.createButton(searchView, R.id.addSelectedButton, this::addSelectedSearchResultToPlaylist);
        playSearchResultButton      = ButtonMaker.createButton(searchView, R.id.playSelectedButton, this::playSelectedSearchResult);
        enqueueSearchResultButton   = ButtonMaker.createButton(searchView, R.id.playNextButton, this::addSearchResultToQueue);
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
        showSearch(false);
    }


    public void showSearch(boolean isAddingTrackToPlaylist){
        this.isAddingTrackToPlaylist = isAddingTrackToPlaylist;
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
        keyboardHelper.hideKeyboard(searchView);
        Animator animator = AnimatorHelper.createHideAnimatorFor(searchView, ()->{
            searchView.setVisibility(View.GONE);
            searchEditText.setText("");
            clearSearchResults();
            scrollToPositionIfSearchResultHasBeenPlayed();
            mainActivity.ensureSelectedTrackIsVisible();
        });
        dismissSearchViewOnBackPressedCallback.setEnabled(false);
        animator.start();
    }


    private void scrollToPositionIfSearchResultHasBeenPlayed(){
        if(hasSearchResultBeenPlayed){
            mainActivity.getMediaPlayerService().scrollToPositionOf(selectedSearchResultTrack, true);
        }
        hasSearchResultBeenPlayed = false;
    }


    private void setupRecyclerView(){
        searchResultsListAdapter = new SearchResultsListAdapter(Collections.emptyList(), this::onSearchResultSelect);
        recyclerView.setLayoutManager(new LinearLayoutManager(mainActivity));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(searchResultsListAdapter);
    }


    private void onSearchResultSelect(Track track){
        selectedSearchResultTrack = track;
        if(isAddingTrackToPlaylist){
            addSelectedSearchResultToPlaylist();
            return;
        }
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


    private void setupKeyListener(){
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


    private void showSearchResultsButtons(){
        if(isAddingTrackToPlaylist){
            setButtonVisibilityForAddingTrackToPlaylist();
            return;
        }
        setButtonVisibilityForNormalSearch();
    }


    private void setButtonVisibilityForAddingTrackToPlaylist(){
        addSearchResultButton.setVisibility(View.VISIBLE);
        playSearchResultButton.setVisibility(View.GONE);
        enqueueSearchResultButton.setVisibility(View.GONE);
    }


    private void setButtonVisibilityForNormalSearch(){
        playSearchResultButton.setVisibility(View.VISIBLE);
        enqueueSearchResultButton.setVisibility(View.VISIBLE);
        if(mainActivity.isUserPlaylistLoaded()){
            addSearchResultButton.setVisibility(View.VISIBLE);
        }
    }


    private void playSelectedSearchResult(){
        mainActivity.disableViewForAWhile(playSearchResultButton);
        if(selectedSearchResultTrack != null) {
            mediaPlayerService.selectAndPlayTrack(selectedSearchResultTrack);
            hasSearchResultBeenPlayed = true;
        }
    }


    private void addSearchResultToQueue(){
        if(selectedSearchResultTrack != null){
           mainActivity.enqueue(selectedSearchResultTrack);
        }
    }
}
