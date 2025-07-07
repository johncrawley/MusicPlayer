package com.jacstuff.musicplayer.view.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.util.TypedValue;

import com.jacstuff.musicplayer.R;

public class AlertHelper {


    public static void showDialogForPlaylist(Context context, int titleResId, int messageResId, String playlistName, Runnable onOkRunnable){

        if(context == null){
            onOkRunnable.run();
            return;
        }
        var typedValue = new TypedValue();
        var theme = context.getTheme();
        theme.resolveAttribute(R.attr.alert_warning_drawable, typedValue,true);

        new AlertDialog.Builder(context)
                .setTitle(context.getString(titleResId))
                .setMessage(context.getString(messageResId, playlistName))
                .setIcon(typedValue.resourceId)
                .setPositiveButton(android.R.string.ok, (dialog, whichButton) -> onOkRunnable.run())
                .setNegativeButton(android.R.string.cancel, null).show();
    }
}
