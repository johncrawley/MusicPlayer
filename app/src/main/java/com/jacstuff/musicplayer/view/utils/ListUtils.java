package com.jacstuff.musicplayer.view.utils;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListUtils {


    public static void setVisibilityOnNoItemsFoundText(List<?> tracks, RecyclerView recyclerView, TextView noItemsFoundTextview){
        boolean isEmpty = tracks == null || tracks.isEmpty();
        recyclerView.setVisibility(isEmpty? View.GONE : View.VISIBLE);
        noItemsFoundTextview.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }


    public static void setVisibilityOnNoItemsFoundText(List<?> tracks, RecyclerView recyclerView, TextView noItemsFoundTextview, String text){
        var isEmpty = tracks == null || tracks.isEmpty();
        System.out.println("^^^ ----->>> ListUtils.setVisibilityOnNoItemsFoundText() : isEmpty: " + isEmpty + " is text blank: " + text.isBlank());
        if(isEmpty && !text.isBlank()){
            noItemsFoundTextview.setText(text);
        }
        recyclerView.setVisibility(isEmpty? View.GONE : View.VISIBLE);
        noItemsFoundTextview.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }


}
