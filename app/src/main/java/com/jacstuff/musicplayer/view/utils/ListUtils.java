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


}
