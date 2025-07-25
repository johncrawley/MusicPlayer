package com.jacstuff.musicplayer.view.fragments.options;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.MediaPlayerService;
import com.jacstuff.musicplayer.service.db.entities.Playlist;
import com.jacstuff.musicplayer.service.db.entities.Track;
import com.jacstuff.musicplayer.view.fragments.DialogFragmentUtils;
import com.jacstuff.musicplayer.view.fragments.playlist.PlaylistRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AddTrackToPlaylistFragment extends DialogFragment {

    private TextView titleTextView;
    private int numberOfPlaylists;

    public static AddTrackToPlaylistFragment newInstance() {
        return new AddTrackToPlaylistFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_track_to_playlist, container, false);
        dismissIfServiceNotReady();
        setupPlaylistRecyclerView(view);
        setupTitle(view);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DialogFragmentUtils.setTransparentBackground(this);
        setViewDimensions(view);
    }


    private void dismissIfServiceNotReady(){
        MainActivity ma = (MainActivity) getActivity();
        if(ma == null  || ma.getMediaPlayerService() == null){
            dismiss();
        }
    }


    private void setupTitle(View parentView){
        titleTextView = parentView.findViewById(R.id.title);
        String title = getCurrentTrackTitle();
        if(title.isBlank()){
            return;
        }
        String text = getString(R.string.add_track_title, title);
        titleTextView.setText(text);
    }


    public void setViewDimensions(View parentView){
        parentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                parentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Rect windowBounds = DialogFragmentUtils.getWindowBounds(AddTrackToPlaylistFragment.this);
                int height = getPlaylistHeight(windowBounds);
                int width = getPlaylistWidth(windowBounds);
                setListDimensions(parentView, width, height);
                setTitleTextDimensions(width);
            }
        });
    }


    private void setListDimensions(View parentView, int width, int height){
        LinearLayout linearLayout = parentView.findViewById(R.id.playlistRecyclerViewLayout);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(width, height));
    }


    private void setTitleTextDimensions(int width){
        var textLayoutParams = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        textLayoutParams.setMargins(5,20,5,20);
        titleTextView.setLayoutParams(textLayoutParams);
    }


    private int getPlaylistWidth(Rect windowBounds){
        int windowHeight = windowBounds.height();
        int windowWidth = windowBounds.width();
        float widthRatio = windowHeight > windowWidth ? 1.5f : 2.3f;
        return (int)(windowWidth/widthRatio);
    }


    private int getPlaylistHeight(Rect windowBounds){
        int windowHeight = windowBounds.height();
        int windowWidth = windowBounds.width();
        float heightRatio = (windowHeight > windowWidth ? getPortraitListHeightRatio() : 2.5f);
        return (int)(windowHeight/heightRatio);
    }


    private float getPortraitListHeightRatio(){
        return switch (numberOfPlaylists){
            case  0,1,2 -> 4.0f;
            case  3,4,5 -> 3.6f;
            case  6,7,8 -> 2.4f;
            default -> 2f;
        };
    }


    private String getCurrentTrackTitle(){
        String trackTitle = "";
        MainActivity mainActivity = (MainActivity)getActivity();
        if(mainActivity != null){
            Track track = mainActivity.getSelectedTrack();
            if(track != null){
                trackTitle = track.getTitle();
            }
        }
        return trackTitle;
    }


    private void setupPlaylistRecyclerView(View parentView){
        if(parentView == null){
            return;
        }
        RecyclerView recyclerView = parentView.findViewById(R.id.playlistRecyclerView);
        PlaylistRecyclerAdapter listAdapter = new PlaylistRecyclerAdapter(loadUserPlaylists(), this::addTrackToSelectedPlaylist);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(listAdapter);
    }


    private void addTrackToSelectedPlaylist(Playlist playlist, int position){
        getMain().ifPresent(m -> m.addTrackToPlaylist(playlist, position));
        dismissAfterDelay();
    }


    private Optional<MainActivity> getMain(){
        return Optional.ofNullable((MainActivity) getActivity());
    }



    private List<Playlist> loadUserPlaylists(){
        int INITIAL_PLAYLIST_CAPACITY = 50;
        List<Playlist> playlists = new ArrayList<>(INITIAL_PLAYLIST_CAPACITY);
        getMediaPlayerService().ifPresent(mps -> playlists.addAll(mps.getPlaylistManager().getAllUserPlaylists()));
        numberOfPlaylists = playlists.size();
        return playlists;
    }


    private Optional<MediaPlayerService> getMediaPlayerService(){
        MainActivity mainActivity = getMainActivity();
        return mainActivity == null ? Optional.empty() : Optional.ofNullable(mainActivity.getMediaPlayerService());
    }


    private MainActivity getMainActivity(){
        return (MainActivity) getActivity();
    }


    private void dismissAfterDelay(){
        new Handler(Looper.getMainLooper()).postDelayed(this::dismiss, 200);
    }
}
