package com.jacstuff.musicplayer.view.utils;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.animation.Animator;
import android.content.Context;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;

import com.jacstuff.musicplayer.R;

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
        animator.addListener(createOnEndAnimatorListener(onFinish));
        return animator;
    }


    public static Animation createFadeInAnimation(Context context, Runnable onFinish){
        return createAnimation(context, onFinish, R.anim.fade_in);
    }

    public static Animation createFadeInAnimation(Context context){
        return createAnimation(context, () ->{}, R.anim.fade_in);
    }


    public static Animation createFadeOutAnimation(Context context, Runnable onFinish){
        return createAnimation(context, onFinish, R.anim.fade_out);
    }


    public static void switchViews(View viewToFadeOut, View viewToFadeIn, Context context){
        if(viewToFadeOut.getVisibility() == VISIBLE) {
            fadeOutFadeIn(viewToFadeOut, viewToFadeIn, context);
        }
        else{
            fadeIn(viewToFadeIn, context);
        }
    }


    public static void hideIfVisible(View layout, Context context){
        var fadeOutAnimation = createFadeOutAnimation(context, ()-> layout.setVisibility(INVISIBLE));
        if(layout.getVisibility() == VISIBLE){
            layout.clearAnimation();
            layout.setAnimation(fadeOutAnimation);
            layout.animate();
        }
    }


    private static void fadeIn(View view, Context context){
        var animation = createFadeInAnimation(context);
        view.clearAnimation();
        view.setVisibility(VISIBLE);
        view.setAnimation(animation);
        view.animate();
    }


    private static void fadeOutFadeIn(View viewToFadeOut, View viewToFadeIn, Context context){
        viewToFadeOut.clearAnimation();
        var switchAnimation = createFadeOutAnimation(context, () -> {
            viewToFadeOut.setVisibility(INVISIBLE);
            viewToFadeOut.clearAnimation();
            fadeIn(viewToFadeIn, context);
        });
        viewToFadeOut.setAnimation(switchAnimation);
        viewToFadeOut.animate();
    }


    public static Animation createFadeOutAnimation(Context context){
        return createAnimation(context, () -> {}, R.anim.fade_out);
    }


    private static Animation createAnimation(Context context, Runnable runnable, int resId){
        var animation = AnimationUtils.loadAnimation(context, resId);
        addOnEndAnimationListenerTo(animation, runnable);
        return animation;
    }


    private static void addOnEndAnimationListenerTo(Animation animation, Runnable onEnd){
        animation.setAnimationListener(createOnEndAnimationListener(onEnd));
    }


    private static Animation.AnimationListener createOnEndAnimationListener(Runnable runnable){
        return new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation) {
                runnable.run();
            }};
    }


    private static Animator.AnimatorListener createOnEndAnimatorListener(Runnable runnable) {
        return new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                runnable.run();
            }
        };
    }
}
