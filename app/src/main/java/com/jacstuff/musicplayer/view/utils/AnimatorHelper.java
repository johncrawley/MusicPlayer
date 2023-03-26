package com.jacstuff.musicplayer.view.utils;

import android.animation.Animator;
import android.view.View;
import android.view.ViewAnimationUtils;

import androidx.annotation.NonNull;

public class AnimatorHelper {


    public static Animator createShowAnimatorFor(View view, Runnable onFinish){
        return createAnimator(view, onFinish, false);
    }


    public static Animator createHideAnimatorFor(View view, Runnable onFinish){
        return createAnimator(view, onFinish, true);
    }


    private static Animator createAnimator(View view, Runnable onFinish, boolean isHidingAnimation){
        int cx = view.getWidth() / 2;
        int cy = view.getHeight() / 2;
        float fullRadius = (float) Math.hypot(cx, cy);
        float startRadius = isHidingAnimation ? fullRadius : 1f;
        float endRadius = isHidingAnimation ? 1f : fullRadius;
        Animator animator = ViewAnimationUtils.createCircularReveal(view, cx, cy, startRadius, endRadius);
        animator.setDuration(300);
        animator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(@NonNull Animator animator) {}
            @Override public void onAnimationCancel(@NonNull Animator animator) {}
            @Override public void onAnimationRepeat(@NonNull Animator animator) {}
            @Override public void onAnimationEnd(@NonNull Animator animator) { onFinish.run();}
        });
        return animator;

    }
}
