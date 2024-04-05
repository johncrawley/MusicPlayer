package com.jacstuff.musicplayer.view.utils;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.jacstuff.musicplayer.view.fragments.Message;
import com.jacstuff.musicplayer.view.fragments.MessageKey;

import java.util.ArrayList;


public class FragmentHelper {

    public static FragmentTransaction createTransaction(AppCompatActivity mainActivity, String tag){
        FragmentTransaction fragmentTransaction = mainActivity.getSupportFragmentManager().beginTransaction();
        removePreviousFragmentTransaction(mainActivity, tag, fragmentTransaction);
        return fragmentTransaction;
    }

    private static void removePreviousFragmentTransaction(AppCompatActivity mainActivity, String tag, FragmentTransaction fragmentTransaction){
        Fragment prev = mainActivity.getSupportFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
    }


    public static void sendArrayListToFragment(AppCompatActivity activity, String requestKey, String itemKey, ArrayList<String> arrayList){
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(itemKey, arrayList);
        activity.runOnUiThread(()-> activity.getSupportFragmentManager().setFragmentResult(requestKey, bundle));
    }


    public static void sendArrayListToFragment(AppCompatActivity activity, Message requestKey, MessageKey itemKey, ArrayList<String> arrayList){
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(itemKey.toString(), arrayList);
        activity.runOnUiThread(()-> activity.getSupportFragmentManager().setFragmentResult(requestKey.toString(), bundle));
    }

}
