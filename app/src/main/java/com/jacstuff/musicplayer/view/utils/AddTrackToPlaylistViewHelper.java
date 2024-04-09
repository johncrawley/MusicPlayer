package com.jacstuff.musicplayer.view.utils;

import static com.jacstuff.musicplayer.view.utils.AnimatorHelper.createShowAnimatorFor;

import android.animation.Animator;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.db.entities.Playlist;
import com.jacstuff.musicplayer.view.fragments.playlist.PlaylistRecyclerAdapter;
import com.jacstuff.musicplayer.view.search.SearchViewHelper;
import com.jacstuff.musicplayer.view.utils.AnimatorHelper;

import java.util.ArrayList;
import java.util.List;

public class AddTrackToPlaylistViewHelper {


    private final MainActivity mainActivity;
    private View addTrackToPlaylistView;
    private OnBackPressedCallback dismissAddTrackToPlaylistViewOnBackPressedCallback;
    private List<Playlist> playlists;
    private PlaylistRecyclerAdapter playlistRecyclerAdapter;


    public AddTrackToPlaylistViewHelper(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        setupAddTrackToPlaylistView();
        setupDismissViewOnBackPressed();
    }


    public void setupAddTrackToPlaylistView() {
        addTrackToPlaylistView = mainActivity.findViewById(R.id.addTrackToPlaylistView);
        RecyclerView addTrackToPlaylistRecyclerView = mainActivity.findViewById(R.id.addTrackToPlaylistRecyclerView);
        playlists = new ArrayList<>(mainActivity.getAllUserPlaylists());
        playlistRecyclerAdapter = new PlaylistRecyclerAdapter(playlists, mainActivity::addTrackToPlaylist);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mainActivity);
        addTrackToPlaylistRecyclerView.setLayoutManager(layoutManager);
        addTrackToPlaylistRecyclerView.setItemAnimator(new DefaultItemAnimator());
        addTrackToPlaylistRecyclerView.setAdapter(playlistRecyclerAdapter);
    }


    private void setupDismissViewOnBackPressed(){
        dismissAddTrackToPlaylistViewOnBackPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                hideAddTrackToPlaylistView();
            }
        };
        mainActivity.getOnBackPressedDispatcher().addCallback(mainActivity, dismissAddTrackToPlaylistViewOnBackPressedCallback);
    }


    public void showAddTrackToPlaylistView(){
        SearchViewHelper searchViewHelper = mainActivity.getSearchViewHelper();
        refreshPlaylist();
        if(searchViewHelper != null){
            searchViewHelper.hideAllSearchResultsButtons();
        }
        Animator animator = createShowAnimatorFor(addTrackToPlaylistView, ()-> {});
        addTrackToPlaylistView.setVisibility(View.VISIBLE);
        dismissAddTrackToPlaylistViewOnBackPressedCallback.setEnabled(true);
        animator.start();
    }


    public void hideView(){
        addTrackToPlaylistView.setVisibility(View.GONE);
    }


    private void refreshPlaylist(){
        playlists.clear();
        playlists.addAll(mainActivity.getAllUserPlaylists());
        playlistRecyclerAdapter.refresh(playlists);
    }


    private void hideAddTrackToPlaylistView(){
        if(addTrackToPlaylistView.getVisibility() != View.VISIBLE){
            return;
        }
        Animator animator = AnimatorHelper.createHideAnimatorFor(addTrackToPlaylistView, ()->
                addTrackToPlaylistView.setVisibility(View.GONE));
        dismissAddTrackToPlaylistViewOnBackPressedCallback.setEnabled(false);
        animator.start();
    }

}
