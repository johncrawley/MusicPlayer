package com.jacstuff.musicplayer.view.fragments;

import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.service.db.entities.PlaylistType;
import com.jacstuff.musicplayer.service.playlist.PlaylistManager;
import com.jacstuff.musicplayer.view.fragments.list.SimpleListAdapter;
import com.jacstuff.musicplayer.view.utils.ButtonMaker;

import java.util.List;

public class Utils {



    public static void selectPositionFromCurrentPlaylist(Fragment fragment,
                                                         SimpleListAdapter listAdapter,
                                                         RecyclerView recyclerView,
                                                         PlaylistType expectedType) {
        if (listAdapter.isPositionSelected()) {
            return;
        }

        var playlistManager = getPlaylistManager(fragment);
        if (playlistManager != null && playlistManager.getCurrentPlaylist() != null) {
            var currentPlaylistType = getCurrentPlaylistType(fragment);
            if (currentPlaylistType != null && currentPlaylistType == expectedType) {
                scrollToAndSelect(listAdapter, recyclerView, playlistManager.getCurrentPlaylist().getName());
            }
        }
    }



    public static PlaylistType getCurrentPlaylistType(Fragment fragment){
        var playlistManager = getPlaylistManager(fragment);
        if(playlistManager != null) {
            var playlist = playlistManager.getCurrentPlaylist();
            if (playlist != null) {
                return playlist.getType();
            }
        }
        return null;
    }


    public static Button setupButtonAndMakeVisible(View parentView, int buttonId, Runnable runnable){
        var button = ButtonMaker.setupButton(parentView,
                buttonId,
                runnable);

        if(button != null){
            button.setVisibility(VISIBLE);
        }
        return button;
    }


    public static void disableButton(Button button){
        if(button != null) {
            button.setEnabled(false);
        }
    }


    private static void scrollToAndSelect(SimpleListAdapter listAdapter, RecyclerView recyclerView, String name){
        int position = listAdapter.selectItemAt(name);
        if(position > RecyclerView.NO_POSITION){
            recyclerView.scrollToPosition(position);
        }
    }


    private static PlaylistManager getPlaylistManager(Fragment fragment){
        var mainActivity = (MainActivity) fragment.getActivity();
        if(mainActivity == null){
            return null;
        }
        var mps = mainActivity.getMediaPlayerService();
        if(mps == null){
            return null;
        }
        return mps.getPlaylistManager();
    }



    public static int getInt(Bundle bundle, MessageKey key){
        return bundle.getInt(key.toString(), 0);
    }


    public static boolean getBoolean(Bundle bundle, MessageKey key){
        return bundle.getBoolean(key.toString(), false);
    }


    public static List<String> getStrList(Bundle bundle, MessageKey key){
        return bundle.getStringArrayList(key.toString());
    }


    public static void putInt(Bundle bundle, MessageKey messageKey, int value){
        bundle.putInt(messageKey.toString(), value);
    }


    public static void putLong(Bundle bundle, MessageKey messageKey, long value){
        bundle.putLong(messageKey.toString(), value);
    }


    public static long getLong(Bundle bundle, MessageKey messageKey){
        return bundle.getLong(messageKey.toString(), -1L);
    }


    public static void putBoolean(Bundle bundle, MessageKey messageKey, boolean value){
        bundle.putBoolean(messageKey.toString(), value);
    }


    public static void sendFragmentMessage(AppCompatActivity activity, String messageKey){
        activity.getSupportFragmentManager().setFragmentResult(messageKey, new Bundle());
    }


    public static void sendFragmentMessage(AppCompatActivity activity, Message message){
        sendFragmentMessage(activity, message, new Bundle());
    }


    public static void sendFragmentMessage(AppCompatActivity activity, Message message, Bundle bundle){
        activity.getSupportFragmentManager().setFragmentResult(message.toString(), bundle);
    }

}
