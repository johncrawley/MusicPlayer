package com.jacstuff.musicplayer.view.fragments.genre;

import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.sendMessage;
import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.setListener;
import static com.jacstuff.musicplayer.view.fragments.Message.NOTIFY_TO_DESELECT_PLAYLIST_ITEMS;
import static com.jacstuff.musicplayer.view.fragments.MessageKey.GENRE_UPDATES;
import static com.jacstuff.musicplayer.view.utils.ListUtils.setVisibilityOnNoItemsFoundText;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.view.fragments.Message;
import com.jacstuff.musicplayer.view.fragments.StringListAdapter;

import java.util.ArrayList;
import java.util.List;

public class GenresFragment extends Fragment {

    private RecyclerView recyclerView;
    private StringListAdapter listAdapter;
    private View parentView;
    private TextView noGenresFoundTextView;


    public GenresFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_genres, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        this.parentView = view;
        recyclerView = parentView.findViewById(R.id.genresRecyclerView);
        noGenresFoundTextView = parentView.findViewById(R.id.noGenresFoundTextView);
        refreshList();
        setupFragmentListener();
    }


    private void setupFragmentListener(){
        setListener(this, Message.SEND_GENRES_TO_FRAGMENT.toString(), this::loadGenres);
        setListener(this, Message.NOTIFY_TO_LOAD_GENRE.toString(), (bundle) -> listAdapter.selectLongClickItem());
        setListener(this, Message.NOTIFY_TO_DESELECT_GENRE_ITEMS, (bundle) -> listAdapter.deselectCurrentlySelectedItem());
    }


    private void loadGenres(Bundle bundle){
        ArrayList<String> genreNames =  bundle.getStringArrayList(GENRE_UPDATES.toString());
        listAdapter.setItems(genreNames);
        listAdapter.deselectCurrentlySelectedItem();
        listAdapter.resetSelections();
        setVisibilityOnNoGenresFoundText(genreNames);
    }


    private void refreshList(){
        List<String> genreNames = getMainActivity().getGenreNames();
        setVisibilityOnNoGenresFoundText(genreNames);
        if(this.parentView == null || genreNames == null){
            return;
        }
        listAdapter = new StringListAdapter(genreNames, this::loadTracksFromGenre);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(listAdapter);
    }


    private void loadTracksFromGenre(String genreName){
        getMainActivity().loadTracksFromGenre(genreName);
        sendMessage(this, NOTIFY_TO_DESELECT_PLAYLIST_ITEMS);
        toastLoaded();
    }


    private MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }


    private void toastLoaded(){
        Toast.makeText(getContext(), getString(R.string.toast_genre_tracks_loaded), Toast.LENGTH_SHORT).show();
    }


    private void setVisibilityOnNoGenresFoundText(List<String> tracks){
        setVisibilityOnNoItemsFoundText(tracks, recyclerView, noGenresFoundTextView);
    }



}