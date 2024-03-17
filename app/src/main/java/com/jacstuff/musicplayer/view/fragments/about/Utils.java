package com.jacstuff.musicplayer.view.fragments.about;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.jacstuff.musicplayer.view.fragments.Message;
import com.jacstuff.musicplayer.view.fragments.MessageKey;

public class Utils {

    public static int getInt(Bundle bundle, MessageKey key){
        return bundle.getInt(key.toString(), 0);
    }

    public static boolean getBoolean(Bundle bundle, MessageKey key){
        return bundle.getBoolean(key.toString(), false);
    }

    public static void putInt(Bundle bundle, MessageKey messageKey, int value){
        bundle.putInt(messageKey.toString(), value);
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
