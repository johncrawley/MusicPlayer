package com.jacstuff.musicplayer.view.fragments.genre;

import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.sendMessages;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TO_DESELECT_ALBUM_ITEMS;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TO_DESELECT_ARTIST_ITEMS;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TO_DESELECT_PLAYLIST_ITEMS;
import static com.jacstuff.musicplayer.view.utils.ListUtils.setVisibilityOnNoItemsFoundText;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.view.fragments.DialogFragmentUtils;
import com.jacstuff.musicplayer.view.fragments.StringListAdapter;

import java.util.List;

public class GenresFragment extends DialogFragment {

    private RecyclerView recyclerView;
    private View parentView;
    private TextView noGenresFoundTextView;

    public GenresFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_load_genre, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        this.parentView = view;
        recyclerView = parentView.findViewById(R.id.recyclerView);
        noGenresFoundTextView = parentView.findViewById(R.id.noItemsFoundText);
        log("Entered onViewCreated()");
        refreshList();
        DialogFragmentUtils.setTransparentBackground(this);
    }



    private void log(String msg){
        System.out.println("^^^ GenresFragment: " + msg);
    }


    private void refreshList(){
        List<String> genreNames = getMainActivity().getGenreNames();
        setVisibilityOnNoGenresFoundText(genreNames);
        if(this.parentView == null || genreNames == null){
            return;
        }
        StringListAdapter listAdapter = new StringListAdapter(genreNames, this::loadTracksFromGenre);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(listAdapter);
    }


    private void loadTracksFromGenre(String genreName, int position){
        getMainActivity().loadTracksFromGenre(genreName);
        notifyOtherFragmentsToDeselectItems();
        toastLoaded();
        dismissAfterDelay();
    }


    private void notifyOtherFragmentsToDeselectItems(){
        sendMessages(this,
                NOTIFY_TO_DESELECT_PLAYLIST_ITEMS,
                NOTIFY_TO_DESELECT_ALBUM_ITEMS,
                NOTIFY_TO_DESELECT_ARTIST_ITEMS);
    }


    private void toastLoaded(){
        getMainActivity().toastIfTabsNotAutoSwitched(R.string.toast_genre_tracks_loaded);
    }


    private void setVisibilityOnNoGenresFoundText(List<String> tracks){
        setVisibilityOnNoItemsFoundText(tracks, recyclerView, noGenresFoundTextView);
    }


    private MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }



    private void dismissAfterDelay(){
        new Handler(Looper.getMainLooper()).postDelayed(this::dismiss, 200);
    }

}