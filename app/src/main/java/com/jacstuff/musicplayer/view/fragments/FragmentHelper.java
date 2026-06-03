package com.jacstuff.musicplayer.view.fragments;

import static com.jacstuff.musicplayer.view.fragments.Utils.putLong;
import static com.jacstuff.musicplayer.view.fragments.dialog.DialogFragmentUtils.addStrTo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.jacstuff.musicplayer.service.db.entities.PlaylistType;
import com.jacstuff.musicplayer.view.fragments.about.AboutDialogFragment;
import com.jacstuff.musicplayer.view.fragments.album.AlbumOptionsFragment;
import com.jacstuff.musicplayer.view.fragments.artist.ArtistOptionsFragment;
import com.jacstuff.musicplayer.view.fragments.config.ConfigDialogFragment;
import com.jacstuff.musicplayer.view.fragments.genre.GenresFragment;
import com.jacstuff.musicplayer.view.fragments.playlist.AddRandomTracksFragment;
import com.jacstuff.musicplayer.view.fragments.playlist.CreatePlaylistFragment;
import com.jacstuff.musicplayer.view.fragments.tracks.TrackOptionsDialog;

import java.util.Arrays;
import java.util.function.Consumer;


public class FragmentHelper {


    public static void showConfigDialog(AppCompatActivity activity){
        FragmentHelper.showDialog(activity, new ConfigDialogFragment(), "configDialogFragment");
    }


    public static void showAboutDialog(AppCompatActivity activity){
        FragmentHelper.showDialog(activity, new AboutDialogFragment(), "aboutDialogFragment");
    }


    public static void showGenreDialog(AppCompatActivity activity){
        showDialog(activity, new GenresFragment(), "loadGenreFragment");
    }


    public static void showTrackOptionsDialog(AppCompatActivity activity){
        showDialog(activity, TrackOptionsDialog.newInstance(), "track_options_dialog");
    }


    public static void showArtistOptionsDialog(Fragment parentFragment, String artistName){
            var bundle = new Bundle();
            bundle.putString(ArtistOptionsFragment.ARTIST_NAME_BUNDLE_KEY, artistName);
            showDialog(parentFragment, ArtistOptionsFragment.newInstance(), "artist_options", bundle);
    }


    public static void showAlbumOptionsDialog(Fragment parentFragment, String albumName){
        var bundle = new Bundle();
        bundle.putString(AlbumOptionsFragment.ALBUM_NAME_BUNDLE_KEY, albumName);
        showDialog(parentFragment, AlbumOptionsFragment.newInstance(), "album_options", bundle);
    }


    public static void showAddRandomTracksDialog(Fragment parentFragment, String playlistName, long playlistId){
        var bundle = new Bundle();
        addStrTo(bundle, MessageKey.PLAYLIST_NAME, playlistName);
        addStrTo(bundle, MessageKey.PLAYLIST_TYPE, PlaylistType.GENRE.name());
        putLong(bundle, MessageKey.PLAYLIST_ID, playlistId);
        showDialog(parentFragment, new AddRandomTracksFragment(), AddRandomTracksFragment.TAG, bundle);
    }


    public static void showCreatePlaylistDialog(Fragment parentFragment){
        showDialog(parentFragment, CreatePlaylistFragment.newInstance(), "create_playlist", new Bundle());
    }


    public static void showDialog(AppCompatActivity activity, DialogFragment dialogFragment, String tag){
        var fragmentManager = activity.getSupportFragmentManager();
        var existingFragment = fragmentManager.findFragmentByTag(tag);
        if (existingFragment != null) {
            fragmentManager.beginTransaction()
                    .remove(existingFragment)
                    .commit();
        }
        fragmentManager.beginTransaction()
                .add(dialogFragment , tag)
                .commit();
    }



    public static void showDialog(Fragment parentFragment, DialogFragment dialogFragment, String tag, Bundle bundle){
        var fragmentManager = parentFragment.getParentFragmentManager();
        var existingFragment = fragmentManager.findFragmentByTag(tag);
        if (existingFragment != null) {
            fragmentManager.beginTransaction()
                    .remove(existingFragment)
                    .commit();
        }
        dialogFragment.setArguments(bundle);
        fragmentManager.beginTransaction()
                .add(dialogFragment , tag)
                .commit();
    }


    public static void setListener(Fragment fragment, String key, Consumer<Bundle> consumer){
        fragment.getParentFragmentManager()
                .setFragmentResultListener(key, fragment, (requestKey, bundle) -> consumer.accept(bundle));
    }


    public static void setListener(Fragment fragment, Message key, Consumer<Bundle> consumer){
        fragment.getParentFragmentManager()
                .setFragmentResultListener(key.toString(), fragment, (requestKey, bundle) -> consumer.accept(bundle));
    }


    public static void sendMessages(Fragment fragment, Message... messages){
        Arrays.stream(messages).forEach(m -> sendMessage(fragment, m));
    }


    public static void sendMessage(Fragment fragment, Message message){
        sendMessage(fragment, message, new Bundle());
    }


    public static void sendMessage(Fragment fragment, Message message, Bundle bundle){
        fragment.getParentFragmentManager().setFragmentResult(message.toString(), bundle);
    }

}
