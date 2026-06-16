package com.jacstuff.musicplayer.view.fragments.dialog;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.view.fragments.MessageKey;

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


    public static void dismissIfServiceUnavailable(DialogFragment fragment){
        var mainActivity = (MainActivity)fragment.getActivity();
        if(mainActivity == null || mainActivity.getMediaPlayerService() == null){
            fragment.dismiss();
        }
    }


    public static void setTransparentBackground(DialogFragment fragment){
        var dialog = fragment.getDialog();
        if(dialog == null){
            return;
        }
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }


    private static void setupScrollViewHeight(Fragment fragment, View parentView, int scrollViewId, int layoutId){
        if(fragment.getView() == null){
            return;
        }
        ScrollView scrollView = parentView.findViewById(scrollViewId);
        LinearLayout contentsLayout = parentView.findViewById(layoutId);
        int height = Math.min(contentsLayout.getHeight(), getDisplayHeight(fragment) / 2);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, height));
    }


    private static int getDisplayHeight(Fragment fragment){
        if(fragment.getActivity() == null){
            return 700;
        }
        var windowMetrics = fragment.getActivity().getWindowManager().getCurrentWindowMetrics();
        return windowMetrics.getBounds().height();
    }


    public static Rect getWindowBounds(Fragment fragment){
        if(fragment.getActivity() == null){
            return new Rect(0,0,700, 700);
        }
        var windowMetrics = fragment.getActivity().getWindowManager().getCurrentWindowMetrics();
        return windowMetrics.getBounds();
    }


    public static void runThenDismissAfterDelay(DialogFragment dialogFragment, Consumer<MainActivity> consumer){
        var mainActivity = (MainActivity) dialogFragment.getActivity();
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
