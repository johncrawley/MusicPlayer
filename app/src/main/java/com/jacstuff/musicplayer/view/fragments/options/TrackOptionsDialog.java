package com.jacstuff.musicplayer.view.fragments.options;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.db.entities.Track;
import com.jacstuff.musicplayer.view.fragments.DialogFragmentUtils;
import com.jacstuff.musicplayer.view.fragments.FragmentManagerHelper;
import com.jacstuff.musicplayer.view.fragments.trackinfo.TrackInfoFragment;
import com.jacstuff.musicplayer.view.utils.ButtonMaker;

import java.util.function.Consumer;
import java.util.function.Function;


public class TrackOptionsDialog extends DialogFragment {

    public static TrackOptionsDialog newInstance() {
        return new TrackOptionsDialog();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_options_track, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dismissWithoutAMediaPlayerService();
        setupButtons(view);
        DialogFragmentUtils.setTransparentBackground(this);
    }


    private void setupButtons(View parentView){
        ButtonMaker.createButton(parentView, R.id.enqueueTrackButton, this::enqueueCurrentTrack);
        setupInfoButton(parentView);
        setupLoadAlbumButton(parentView);
        setupLoadArtistButton(parentView);
        setupAddTrackToPlaylistButton(parentView);
        setupRemoveTrackButton(parentView);
    }


    private void setupInfoButton(View parentView){
        setupButtonIfConditionsMet(parentView, R.id.showTrackInfoButton, Track::getAlbum, this::loadInfoFragment);
    }

    private void setupLoadAlbumButton(View parentView){
        setupButtonIfConditionsMet(parentView, R.id.loadAlbumButton, Track::getAlbum, this::loadRelatedAlbum);
    }


    private void setupLoadArtistButton(View parentView){
        setupButtonIfConditionsMet(parentView, R.id.loadArtistButton, Track::getArtist, this::loadRelatedArtist);
    }


    private void setupButtonIfConditionsMet(View parentView, int buttonId, Function<Track, String> function, Runnable runnable){
        Track track = getCurrentTrack();
        if(track == null){
            return;
        }
        String attribute = function.apply(track);
        if(attribute.isBlank() || attribute.equalsIgnoreCase("<unknown>")){
            return;
        }
        Button button = ButtonMaker.createButton(parentView, buttonId, runnable);
        if(button != null){
            button.setVisibility(View.VISIBLE);
        }
    }


    private void setupAddTrackToPlaylistButton(View parentView){
        Button addTrackToPlaylistButton =  ButtonMaker.createButton(parentView, R.id.addTrackToPlaylistButton, this::showAddTrackToPlaylistDialog);
        if(addTrackToPlaylistButton != null) {
            setupVisibilityForUserPlaylistsExist(addTrackToPlaylistButton);
        }
    }

    private void dismissWithoutAMediaPlayerService(){
        if(getMainActivity() == null || getMainActivity().getMediaPlayerService() == null){
            dismiss();
        }
    }


    private Track getCurrentTrack(){
        if(getMainActivity() == null || getMainActivity().getMediaPlayerService() == null){
            return  null;
        }
       return getMainActivity().getMediaPlayerService().getCurrentTrack();
    }


    private void setupRemoveTrackButton(View parentView){
        Button removeTrackButton =  ButtonMaker.createButton(parentView, R.id.removeFromPlaylistButton, this::removeSelectedTrackFromPlaylist);
        if(removeTrackButton != null) {
            setupVisibilityForUserPlaylistLoaded(removeTrackButton);
        }
    }


    private void setupVisibilityForUserPlaylistsExist(View view){
        MainActivity mainActivity = getMainActivity();
        int visibility = mainActivity != null && !mainActivity.getAllUserPlaylists().isEmpty() ?
                View.VISIBLE : View.GONE;
        view.setVisibility(visibility);
    }


    private void setupVisibilityForUserPlaylistLoaded(View view){
        MainActivity mainActivity = getMainActivity();
        int visibility = mainActivity != null && mainActivity.isUserPlaylistLoaded() ?
                View.VISIBLE : View.GONE;
        view.setVisibility(visibility);
    }


    private void enqueueCurrentTrack(){
        runThenDismissAfterDelay(MainActivity::addSelectedTrackToQueue);
    }


    private void loadRelatedAlbum(){
        runThenDismissAfterDelay(MainActivity::loadAlbumOfSelectedTrack);
    }


    private void loadInfoFragment(){
        dismiss();
        FragmentManagerHelper.showDialog(this, new TrackInfoFragment(), TrackInfoFragment.TAG, new Bundle());
    }


    private void loadRelatedArtist(){
        runThenDismissAfterDelay(MainActivity::loadArtistOfSelectedTrack);
    }


    private void showAddTrackToPlaylistDialog(){
        dismiss();
        getMainActivity().showAddTrackToPlaylistView();
    }


    private void removeSelectedTrackFromPlaylist(){
        runThenDismissAfterDelay(MainActivity::removeSelectedTrackFromPlaylist);
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
        new Handler(Looper.getMainLooper()).postDelayed(this::dismiss, 200);
    }
}
