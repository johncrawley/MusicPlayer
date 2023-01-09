package com.jacstuff.musicplayer.search;

import android.animation.Animator;
import android.view.View;
import android.view.ViewAnimationUtils;

import androidx.annotation.NonNull;

public class AnimatorHelper {


    public static Animator createShowAnimatorFor(View view, Runnable onFinish){
        int cx = view.getWidth() / 2;
        int cy = view.getHeight() / 2;
        float finalRadius = (float) Math.hypot(cx, cy);
        Animator animator = ViewAnimationUtils.createCircularReveal(view, cx, cy, 1f, finalRadius);
        animator.setDuration(300);
        animator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(@NonNull Animator animator) {}
            @Override public void onAnimationCancel(@NonNull Animator animator) {}
            @Override public void onAnimationRepeat(@NonNull Animator animator) {}
            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                onFinish.run();
            }
        });
        return animator;
    }

    public static Animator createHideAnimatorFor(View view){
        int cx = view.getWidth() / 2;
        int cy = view.getHeight() / 2;
        float finalRadius = (float) Math.hypot(cx, cy);
        Animator animator = ViewAnimationUtils.createCircularReveal(view, cx, cy, finalRadius, 1f);
        animator.setDuration(300);
        animator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(@NonNull Animator animator) {}
            @Override public void onAnimationCancel(@NonNull Animator animator) {}
            @Override public void onAnimationRepeat(@NonNull Animator animator) {}
            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                view.setVisibility(View.GONE);
            }
        });
        return animator;
    }
}
