package com.jacstuff.musicplayer.view.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowMetrics;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.jacstuff.musicplayer.MainActivity;

import java.util.function.Consumer;

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


    public static void setTransparentBackground(DialogFragment fragment){
        Dialog dialog = fragment.getDialog();
        if(dialog == null){
            return;
        }
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
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


    public static Rect getWindowBounds(Fragment fragment){
        if(fragment.getActivity() == null){
            return new Rect(0,0,700, 700);
        }
        WindowMetrics windowMetrics = fragment.getActivity().getWindowManager().getCurrentWindowMetrics();
        return windowMetrics.getBounds();
    }


    public static void runThenDismissAfterDelay(DialogFragment dialogFragment, Consumer<MainActivity> consumer){
        MainActivity mainActivity = (MainActivity) dialogFragment.getActivity();
        if(mainActivity != null) {
            consumer.accept(mainActivity);
        }
        dismissAfterDelay(dialogFragment);
    }


    private static void dismissAfterDelay(DialogFragment dialogFragment){
        new Handler(Looper.getMainLooper()).postDelayed(dialogFragment::dismiss, 200);
    }


    public static void addStrTo(Bundle bundle, MessageKey messageKey, String contents){
        bundle.putString(messageKey.name(), contents);
    }


    public static String getBundleStr(Bundle bundle, MessageKey messageKey){
        return bundle.getString(messageKey.name());
    }
}
