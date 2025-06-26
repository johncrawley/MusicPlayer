package com.jacstuff.musicplayer.view.fragments.playlist;

import static android.view.View.VISIBLE;
import static com.jacstuff.musicplayer.view.fragments.DialogFragmentUtils.getBundleStr;
import static com.jacstuff.musicplayer.view.fragments.Message.TRACKS_ADDED;
import static com.jacstuff.musicplayer.view.fragments.MessageKey.PLAYLIST_ID;
import static com.jacstuff.musicplayer.view.fragments.MessageKey.PLAYLIST_NAME;
import static com.jacstuff.musicplayer.view.fragments.Utils.getLong;
import static com.jacstuff.musicplayer.view.utils.AnimatorHelper.fadeIn;
import static com.jacstuff.musicplayer.view.utils.AnimatorHelper.fadeOut;
import static com.jacstuff.musicplayer.view.utils.AnimatorHelper.hideIfVisible;
import static com.jacstuff.musicplayer.view.utils.AnimatorHelper.switchViews;
import static com.jacstuff.musicplayer.view.utils.ListUtils.setVisibilityOnNoItemsFoundText;
import static com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper.setListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;


public class AddRandomTracksFragment extends DialogFragment {

    private Button addTracksButton;
    public static String TAG = "ADD_RANDOM_TRACKS_FRAGMENT";
    private ViewGroup artistsLayout, genresLayout;
    private final Set<String> selectedGenres = new HashSet<>(100);
    private final Set<String> selectedArtists = new HashSet<>(100);
    private EditText numberOfTracksEditText;
    private String playlistName;
    private PlaylistType playlistType = PlaylistType.ALL_TRACKS;
    private long playlistId;
    private int numberOfTracksToAdd = 30;
    private final int numberOfTracksIncrement = 10;
    private final int minNumberOfTracksToAdd = 1;
    private List<ToggleButton> toggleButtons;


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
        artistsLayout = view.findViewById(R.id.artistsRecyclerLayout);
        RecyclerView artistsRecyclerView = artistsLayout.findViewById(R.id.recyclerView);
        TextView noArtistsFoundTextView = artistsLayout.findViewById(R.id.noItemsFoundText);
        refreshList(artistsRecyclerView, getMainActivity().getArtistNames(), noArtistsFoundTextView, this::toggleArtistSelection);

        genresLayout = view.findViewById(R.id.genresRecyclerLayout);
        RecyclerView genreRecyclerView = genresLayout.findViewById(R.id.recyclerView);
        TextView noGenresFoundTextView = genresLayout.findViewById(R.id.noItemsFoundText);
        refreshList(genreRecyclerView, getMainActivity().getGenreNames(), noGenresFoundTextView, this::toggleGenreSelection);

        numberOfTracksEditText = view.findViewById(R.id.numberOfTracksEditText);
        setNumberOfTracksText();
        assignArgs();
        setupButtons(view);
        DialogFragmentUtils.setTransparentBackground(this);
        setupFragmentListener();
        setupTitle(view);
    }


    private void setNumberOfTracksText(){
        var numTracksStr = String.valueOf(numberOfTracksToAdd);
        numberOfTracksEditText.setText(numTracksStr);
    }


    private void setupFragmentListener(){
        setListener(this, TRACKS_ADDED, (bundle)-> addTracksButton.setEnabled(true));
    }


    private void assignArgs(){
        Bundle bundle = getArguments();
        assert bundle != null;
        playlistName = getBundleStr(bundle, PLAYLIST_NAME);
        playlistId = getLong(bundle, PLAYLIST_ID);
    }


    private void setupTitle(View parentView){
        TextView title = parentView.findViewById(R.id.dialogTitleText);
        String text = getString(R.string.add_random_tracks_to_playlist_dialog_title, playlistName);
        title.setText(text);
    }


    private void setupButtons(View parentView){
        setupBottomPanelButtons(parentView);
        setupNumberOfRandomTracksButtons(parentView);
        setupToggleButtons(parentView);
    }


    private void setupBottomPanelButtons(View parentView){
        addTracksButton = setupButton(parentView, R.id.addTracksButton, this::addRandomTracks);
    }


    private void setupNumberOfRandomTracksButtons(View parentView){
        setupButton(parentView, R.id.decreaseNumberOfRandomTracksButton, this::decreaseNumberOfRandomTracks);
        setupButton(parentView, R.id.increaseNumberOfRandomTracksButton, this::increaseNumberOfRandomTracks);
    }


    private void setupToggleButtons(View parentView){
        ToggleButton allTracksToggleButton = setupToggleButton(parentView, R.id.allTracksToggleButton, this::showAllTracks);
        ToggleButton artistsToggleButton = setupToggleButton(parentView, R.id.artistsToggleButton, this::showArtistsList);
        ToggleButton genresToggleButton = setupToggleButton(parentView, R.id.genresToggleButton, this::showGenresList);
        toggleButtons = List.of(allTracksToggleButton, artistsToggleButton, genresToggleButton);

        allTracksToggleButton.setChecked(true);
        allTracksToggleButton.setEnabled(false);
    }


    private ToggleButton setupToggleButton(View parentView, int id, Runnable runnable){
        ToggleButton toggleButton = parentView.findViewById(id);
        toggleButton.setChecked(false);
        toggleButton.setOnClickListener(tb -> {
            tb.setEnabled(false);
            enableAndUncheckOtherToggleButtons((ToggleButton) tb);
            runnable.run();
        });
        return toggleButton;
    }


    private void showAllTracks(){
        playlistType = PlaylistType.ALL_TRACKS;
        hideIfVisible(artistsLayout, getContext());
        hideIfVisible(genresLayout, getContext());
        fadeInAddButtonIfInVisible();
    }


    private void showArtistsList(){
        playlistType = PlaylistType.ARTIST;
        switchViews(genresLayout, artistsLayout, getContext());
        setVisibilityOfAddButtonBasedOn(selectedArtists);
    }


    private void showGenresList(){
        playlistType = PlaylistType.GENRE;
        switchViews(artistsLayout, genresLayout, getContext());
        setVisibilityOfAddButtonBasedOn(selectedGenres);
    }


    private void enableAndUncheckOtherToggleButtons(ToggleButton toggleButton){
        toggleButtons.stream()
                .filter( tb -> tb != toggleButton)
                .forEach(tb -> {
                    tb.setChecked(false);
                    tb.setEnabled(true);
                });
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


    private void addRandomTracks(){
        disableAllButtons();
        getService().ifPresent( service ->
                service.getPlaylistHelper().addRandomTracksToPlaylist(createRandomTrackConfig()));
    }


    private RandomTrackConfig createRandomTrackConfig(){
         List<String> selection = switch (playlistType){
             case GENRE -> new ArrayList<>(selectedGenres);
             case ARTIST -> new ArrayList<>(selectedArtists);
             default -> Collections.emptyList();
         };
        return new RandomTrackConfig(playlistId, playlistName, playlistType, selection, getNumberOfTracks());
    }


    private Optional<MediaPlayerService> getService(){
        MainActivity mainActivity = (MainActivity) getActivity();
        return mainActivity == null ? Optional.empty() : Optional.of(mainActivity.getMediaPlayerService());
    }


    private void disableAllButtons(){
        addTracksButton.setEnabled(false);
    }


    private void dismissAfterPause(){
        new Handler(Looper.getMainLooper()).postDelayed(this::dismiss, 150);
    }


    private Button setupButton(View parentView, int id, Runnable runnable){
        Button button = parentView.findViewById(id);
        button.setOnClickListener((View v)-> runnable.run());
        return button;
    }


    private void refreshList(RecyclerView recyclerView, List<String> itemsList, TextView noItemsFoundTextView, BiConsumer<String, Integer> onClickConsumer){
        setVisibilityOnNoItemsFoundText(itemsList, recyclerView, noItemsFoundTextView );
        if(itemsList == null){
            return;
        }
        MultiSelectionStringListAdapter listAdapter = new MultiSelectionStringListAdapter(itemsList, onClickConsumer);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(listAdapter);
    }


    private void toggleGenreSelection(String name, int position){
        toggleSelection(name, selectedGenres);
    }


    private void toggleArtistSelection(String name, int position){
        toggleSelection(name, selectedArtists);
    }


    private void toggleSelection(String name, Set<String> set){
        if(set.contains(name)){
            removeSelectionAndFadeOutAddButton(name, set);
        }
        else{
            addSelectionAndFadeInAddButton(name, set);
        }
    }


    private void removeSelectionAndFadeOutAddButton(String name, Set<String> set){
        set.remove(name);
        if(set.isEmpty()){
            fadeOutAndDisableAddTracksButton();
        }
    }


    private void addSelectionAndFadeInAddButton(String name, Set<String> set){
        boolean wasEmpty = set.isEmpty();
        set.add(name);
        if(wasEmpty){
            fadeInAndEnableAddTracksButton();
        }
    }


    private void setVisibilityOfAddButtonBasedOn(Set<String> set){
        if(!set.isEmpty()){
            fadeInAddButtonIfInVisible();
        }
        else{
            fadeOutAddButtonIfVisible();
        }
    }


    private void fadeOutAddButtonIfVisible(){
        if(addTracksButton.getVisibility() == VISIBLE){
            fadeOutAndDisableAddTracksButton();
        }
    }


    private void fadeInAddButtonIfInVisible(){
        if(addTracksButton.getVisibility() != VISIBLE){
            fadeInAndEnableAddTracksButton();
        }
    }


    private void fadeInAndEnableAddTracksButton(){
        fadeIn(addTracksButton, getContext());
        addTracksButton.setEnabled(true);
    }


    private void fadeOutAndDisableAddTracksButton(){
        fadeOut(addTracksButton, getContext());
        addTracksButton.setEnabled(false);
    }


    private MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }



}
