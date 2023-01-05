package com.jacstuff.musicplayer.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.db.track.Track;
import com.jacstuff.musicplayer.list.SearchResultsListAdapter;

import java.util.Collections;
import java.util.List;

public class SearchFragment extends Fragment {

    MainActivity activity;
    private RecyclerView recyclerView;
    private SearchResultsListAdapter searchResultsListAdapter;


    public static SearchFragment newInstance() {
        return new SearchFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = (MainActivity) getActivity();
        recyclerView = view.findViewById(R.id.searchResultsRecyclerView);
        setupRecyclerView(Collections.emptyList());
        setupSearchKeyListener();
        if(activity == null){
            return;
        }
        setupButtons(view);
       // setupDimensions(view, activity);
    }


    private void setupRecyclerView(List<Track> tracks){
        if(tracks == null){
            return;
        }
        searchResultsListAdapter = new SearchResultsListAdapter(tracks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(searchResultsListAdapter);
    }


    @SuppressLint("NotifyDataSetChanged")
    private void refreshTrackList(List<Track> tracks){
       // setupRecyclerView(tracks);
        log("refreshTrackList() update size: " + tracks.size());
        searchResultsListAdapter.setTracks(tracks);
        searchResultsListAdapter.notifyDataSetChanged();
      //  recyclerView.setAdapter(searchResultsListAdapter);

    }

    void setupDimensions(View rootView, Activity activity){
        DisplayMetrics metrics = getDisplayMetrics(activity);
        int width = (int)(metrics.widthPixels /1.5f);
        rootView.setLayoutParams(new FrameLayout.LayoutParams(width, rootView.getLayoutParams().height));
    }


    private void setupSearchKeyListener(){
        EditText inputEditText = getView().findViewById(R.id.trackSearchEditText);

        inputEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void afterTextChanged(Editable s) {
                log("key struck: ");
                List<Track> tracks = activity.getTracksForSearch(inputEditText.getText().toString());
                refreshTrackList(tracks);
            }
        });
    }

    private void log(String msg){
        System.out.println("^^^ SearchFragment: " + msg);
    }

    public DisplayMetrics getDisplayMetrics(Activity activity){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }



    private void setupButtons(View parentView){
        /*
        setupButton(parentView, R.id.addSelectedButton, this::dismiss);
        setupButton(parentView, R.id.addAllButton, this::dismiss);
        setupButton(parentView, R.id.playSelectedButton, this::dismiss);
        setupButton(parentView, R.id.cancelButton, this::dismiss);

         */
    }

    private void setupButton(View parentView, int buttonId, Runnable runnable){
        Button button = parentView.findViewById(buttonId);
        button.setOnClickListener((View v)-> runnable.run());
    }



}