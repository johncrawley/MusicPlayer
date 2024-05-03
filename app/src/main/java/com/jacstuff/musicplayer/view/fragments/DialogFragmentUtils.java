package com.jacstuff.musicplayer.view.fragments;

import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowMetrics;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.fragment.app.Fragment;

public class DialogFragmentUtils {

    public static void setScrollViewHeight(Fragment fragment, View parentView, int scrollViewId, int layoutId){
        parentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                parentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setupScrollViewHeight(fragment, parentView, scrollViewId, layoutId);
            }
        });
    }


    private static void setupScrollViewHeight(Fragment fragment, View parentView, int scrollViewId, int layoutId){
        ScrollView scrollView = parentView.findViewById(scrollViewId);
        LinearLayout contentsLayout = parentView.findViewById(layoutId);

        if(fragment.getView() == null){
            return;
        }
        int height = Math.min(contentsLayout.getHeight(), getDisplayHeight(fragment) / 2);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, height));
    }


    private static int getDisplayHeight(Fragment fragment){
        if(fragment.getActivity() == null){
            return 700;
        }
        WindowMetrics windowMetrics = fragment.getActivity().getWindowManager().getCurrentWindowMetrics();
        return windowMetrics.getBounds().height();
    }

}
