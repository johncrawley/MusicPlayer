package com.jacstuff.musicplayer.view.fragments.options;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.jacstuff.musicplayer.view.fragments.DialogFragmentUtils;
import com.jacstuff.musicplayer.view.fragments.playlist.PlaylistRecyclerAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class AddTrackToPlaylistFragment extends DialogFragment {

    private PlaylistRecyclerAdapter listAdapter;
    private RecyclerView recyclerView;
    private Set<String> playlistNames;
    private final int INITIAL_PLAYLIST_CAPACITY = 50;

    public static AddTrackToPlaylistFragment newInstance() {
        return new AddTrackToPlaylistFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_track_to_playlist, container, false);
        log("Entered onCreateView()");
        setupPlaylistRecyclerView(view);
        setupTitle(view);
        return view;
    }


    private void log(String msg){
        System.out.println("^^^ AddTrackToPlaylistFragment: " + msg);
    }

    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DialogFragmentUtils.setTransparentBackground(this);
        log("entered onViewCreated");
    }


    private void setupTitle(View parentView){
        TextView titleTextView = parentView.findViewById(R.id.title);
        String text = getString(R.string.add_track_to_playlist_dialog_title, "placeholder");
        titleTextView.setText(text);
    }


    private void setupPlaylistRecyclerView(View parentView){
        if(parentView == null){
            return;
        }
        recyclerView = parentView.findViewById(R.id.playlistRecyclerView);
        listAdapter = new PlaylistRecyclerAdapter(getAllPlaylists(), this::addTrackToSelectedPlaylist);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(listAdapter);
    }


    private void addTrackToSelectedPlaylist(Playlist playlist, int position){
        getMain().ifPresent(m -> m.addTrackToPlaylist(playlist, position));
        dismissAfterDelay();
    }


    private List<Playlist> getAllPlaylists(){
        List<Playlist> playlists = loadAllPlaylists();
        assignPlaylistNames(playlists);
        return playlists;
    }


    private Optional<MainActivity> getMain(){
        return Optional.ofNullable((MainActivity) getActivity());
    }



    private List<Playlist> loadAllPlaylists(){
        List<Playlist> playlists = new ArrayList<>(INITIAL_PLAYLIST_CAPACITY);
        getMediaPlayerService().ifPresent(mps -> playlists.addAll(mps.getPlaylistManager().getAllUserPlaylists()));
        return playlists;
    }


    private Optional<MediaPlayerService> getMediaPlayerService(){
        MainActivity mainActivity = getMainActivity();
        return mainActivity == null ? Optional.empty() : Optional.ofNullable(mainActivity.getMediaPlayerService());
    }


    private void assignPlaylistNames(List<Playlist> playlists){
        playlistNames = new HashSet<>(INITIAL_PLAYLIST_CAPACITY);
        playlists.forEach((Playlist pl) -> playlistNames.add(pl.getName().toLowerCase()));
    }



    private void runThenDismissAfterDelay(Consumer<MainActivity> consumer){
        MainActivity mainActivity = (MainActivity) getActivity();
        if(mainActivity != null) {
            consumer.accept(mainActivity);
        }
        dismissAfterDelay();
    }


    private MainActivity getMainActivity(){
        return (MainActivity) getActivity();
    }


    private void dismissAfterDelay(){
        log("entered dismissAfterDelay()");
        new Handler(Looper.getMainLooper()).postDelayed(this::dismiss, 200);
    }
}
