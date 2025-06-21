package com.jacstuff.musicplayer.view.fragments.playlist;

import static com.jacstuff.musicplayer.view.fragments.DialogFragmentUtils.getBundleStr;
import static com.jacstuff.musicplayer.view.fragments.MessageKey.PLAYLIST_ID;
import static com.jacstuff.musicplayer.view.fragments.MessageKey.PLAYLIST_NAME;
import static com.jacstuff.musicplayer.view.fragments.MessageKey.PLAYLIST_TYPE;
import static com.jacstuff.musicplayer.view.fragments.Utils.getLong;
import static com.jacstuff.musicplayer.view.utils.ListUtils.setVisibilityOnNoItemsFoundText;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.jacstuff.musicplayer.service.db.entities.PlaylistType;
import com.jacstuff.musicplayer.service.playlist.RandomTrackConfig;
import com.jacstuff.musicplayer.view.fragments.DialogFragmentUtils;
import com.jacstuff.musicplayer.view.fragments.list.MultiSelectionStringListAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public class AddRandomTracksFragment extends DialogFragment {

    private Button okButton, cancelButton;
    public static String TAG = "ADD_RANDOM_TRACKS_FRAGMENT";
    private RecyclerView recyclerView;
    private TextView noItemsFoundTextView;
    private final Set<String> selectedGenres = new HashSet<>(100);
    private EditText numberOfTracksEditText;
    private String playlistName;
    private PlaylistType playlistType;
    private long playlistId;
    private int numberOfTracksToAdd = 30;
    private final int numberOfTracksIncrement = 10;
    private final int minNumberOfTracksToAdd = 1;


    public static PlaylistOptionsFragment newInstance() {
        return new PlaylistOptionsFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_playlist_add_random_tracks, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        noItemsFoundTextView = view.findViewById(R.id.noItemsFoundTextView);
        numberOfTracksEditText = view.findViewById(R.id.numberOfTracksEditText);
        setNumberOfTracksText();
        assignArgs();
        refreshList();
        setupButtons(view);
        DialogFragmentUtils.setTransparentBackground(this);
    }


    private void setNumberOfTracksText(){
        var numTracksStr = String.valueOf(numberOfTracksToAdd);
        numberOfTracksEditText.setText(numTracksStr);
    }


    private void assignArgs(){
        Bundle bundle = getArguments();
        assert bundle != null;
        playlistName = getBundleStr(bundle, PLAYLIST_NAME);
        playlistType = PlaylistType.valueOf(getBundleStr(bundle, PLAYLIST_TYPE));
        playlistId = getLong(bundle, PLAYLIST_ID);
    }


    private void setupButtons(View parentView){
        okButton = setupButton(parentView, R.id.okButton, this::addRandomTracks);
        cancelButton = setupButton(parentView, R.id.cancelDialogButton, this::dismissDialog);
        setupButton(parentView, R.id.decreaseNumberOfRandomTracksButton, this::decreaseNumberOfRandomTracks);
        setupButton(parentView, R.id.increaseNumberOfRandomTracksButton, this::increaseNumberOfRandomTracks);
    }


    private void decreaseNumberOfRandomTracks(){
        numberOfTracksToAdd = Math.max(minNumberOfTracksToAdd, numberOfTracksToAdd - numberOfTracksIncrement );
        setNumberOfTracksText();
    }


    private void increaseNumberOfRandomTracks(){
        final int maxNumberOfTracksToAdd = 200;
        if(numberOfTracksToAdd == minNumberOfTracksToAdd){
            numberOfTracksToAdd = 0;
        }
        numberOfTracksToAdd = Math.min(maxNumberOfTracksToAdd, numberOfTracksToAdd + numberOfTracksIncrement);
        setNumberOfTracksText();
    }

    private int getNumberOfTracks(){
        int number = Integer.parseInt(numberOfTracksEditText.getText().toString());
        return Math.max(1, number);
    }


    private void dismissDialog(){
        disableAllButtons();
        dismissAfterPause();
    }


    private void addRandomTracks(){
        disableAllButtons();

        var randomTrackConfig = new RandomTrackConfig(playlistId, playlistName, playlistType, new ArrayList<>(selectedGenres), getNumberOfTracks());

        getService().ifPresent( service ->
                service.getPlaylistHelper()
                        .addRandomTracksToPlaylist(randomTrackConfig));
        dismissAfterPause();
    }


    private Optional<MediaPlayerService> getService(){
        MainActivity mainActivity = (MainActivity) getActivity();
        return mainActivity == null ? Optional.empty() : Optional.of(mainActivity.getMediaPlayerService());
    }


    private void disableAllButtons(){
        okButton.setEnabled(false);
        cancelButton.setEnabled(false);
    }


    private void dismissAfterPause(){
        new Handler(Looper.getMainLooper()).postDelayed(this::dismiss, 150);
    }


    private Button setupButton(View parentView, int id, Runnable runnable){
        Button button = parentView.findViewById(id);
        button.setOnClickListener((View v)-> runnable.run());
        return button;
    }

    private void refreshList(){
        List<String> genreNames = getMainActivity().getGenreNames();
        setVisibilityOnNoGenresFoundText(genreNames);
        if(genreNames == null){
            return;
        }
        MultiSelectionStringListAdapter listAdapter = new MultiSelectionStringListAdapter(genreNames, this::loadTracksFromGenre);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(listAdapter);
    }


    private void loadTracksFromGenre(String name, int position){
        if(selectedGenres.contains(name)){
            selectedGenres.remove(name);
        }
        else{
            selectedGenres.add(name);
        }
    }


    private void setVisibilityOnNoGenresFoundText(List<String> tracks){
        setVisibilityOnNoItemsFoundText(tracks, recyclerView, noItemsFoundTextView);
    }


    private MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }



}
