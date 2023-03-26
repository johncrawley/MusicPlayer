package com.jacstuff.musicplayer.view.art;

import static com.jacstuff.musicplayer.view.utils.AnimatorHelper.createShowAnimatorFor;

import android.animation.Animator;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.FragmentTransaction;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.fragments.albumart.AlbumArtDialog;
import com.jacstuff.musicplayer.view.utils.AnimatorHelper;
import com.jacstuff.musicplayer.view.utils.FragmentHelper;

public class AlbumArtHelper {

    private final MainActivity mainActivity;
    private final ImageView albumArtImageView, albumArtLargeImageView;
    private Bitmap currentAlbumArt;
    private OnBackPressedCallback dismissAlbumArtLargeViewOnBackPressedCallback;
    private final View albumArtLargeView;
    private boolean isLargeAlbumArtVisible;


    public AlbumArtHelper(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        currentAlbumArt = mainActivity.getMediaPlayerService().getAlbumArt();
        this.albumArtImageView = mainActivity.findViewById(R.id.albumArtImageView);
        this.albumArtLargeView = mainActivity.findViewById(R.id.albumArtLargeView);
        albumArtLargeImageView = mainActivity.findViewById(R.id.albumArtLargeImageView);
        assignAlbumArt(albumArtImageView, currentAlbumArt);
        setupAlbumViewClick();
        setupDismissLargeAlbumArtOnBackPressed();
    }


    private void setupDismissLargeAlbumArtOnBackPressed(){
        dismissAlbumArtLargeViewOnBackPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                hideLargeAlbumArt();
            }
        };
        mainActivity.getOnBackPressedDispatcher().addCallback(mainActivity, dismissAlbumArtLargeViewOnBackPressedCallback);
    }


    private void showLargeAlbumArt(){
        Animator animator = createShowAnimatorFor(albumArtLargeView, ()-> {isLargeAlbumArtVisible = true;});
        albumArtLargeView.setVisibility(View.VISIBLE);
        dismissAlbumArtLargeViewOnBackPressedCallback.setEnabled(true);
        animator.start();
    }


    private void hideLargeAlbumArt(){
        if(albumArtLargeView.getVisibility() != View.VISIBLE){
            return;
        }
        Animator animator = AnimatorHelper.createHideAnimatorFor(albumArtLargeView, ()-> {
            albumArtLargeView.setVisibility(View.GONE);
            isLargeAlbumArtVisible = false;
        });
        dismissAlbumArtLargeViewOnBackPressedCallback.setEnabled(false);
        animator.start();
    }



    public void changeAlbumArtTo(Bitmap updatedAlbumArt){
        mainActivity.runOnUiThread(()-> {
            if (isCurrentCoverTheSame(updatedAlbumArt)) {
                return;
            }
            changeImageTo(albumArtImageView, updatedAlbumArt, !isLargeAlbumArtVisible);
            changeImageTo(albumArtLargeImageView, updatedAlbumArt, isLargeAlbumArtVisible);
        });
    }


    public void changeAlbumArtToCurrent(ImageView imageView){
        changeImageTo(imageView, currentAlbumArt, true);
    }


    private void setupAlbumViewClick(){
        albumArtImageView.setOnClickListener(v-> showLargeAlbumArt());
    }


    private boolean isCurrentCoverTheSame(Bitmap updatedAlbumArt){
        if(currentAlbumArt == null && updatedAlbumArt == null){
            return true;
        }
        if(currentAlbumArt == null){
            return false;
        }
        if(updatedAlbumArt == null){
            return false;
        }
        return currentAlbumArt.sameAs(updatedAlbumArt);
    }


    private void changeImageTo(ImageView imageView, Bitmap updatedAlbumArt, boolean shouldAnimate){
        currentAlbumArt = updatedAlbumArt;
        if(shouldAnimate){
            fadeToNewArt(imageView, updatedAlbumArt);
            return;
        }
        assignAlbumArt(imageView, updatedAlbumArt);
    }


    private void fadeToNewArt(ImageView imageView, Bitmap updatedAlbumArt){
        Animation fadeOut = AnimationUtils.loadAnimation(mainActivity, R.anim.fade_out);
        imageView.startAnimation(fadeOut);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override  public void onAnimationStart(Animation animation) {}
            @Override  public void onAnimationRepeat(Animation animation) {}
            @Override  public void onAnimationEnd(Animation animation) {
                assignAlbumArt(imageView, updatedAlbumArt);
                Animation fadeIn = AnimationUtils.loadAnimation(mainActivity, R.anim.fade_in);
                imageView.startAnimation(fadeIn);
            }
        });
    }


    private void assignAlbumArt(ImageView imageView, Bitmap updatedAlbumArt){
        if (updatedAlbumArt != null) {
            imageView.setImageDrawable(new BitmapDrawable(mainActivity.getResources(), updatedAlbumArt));
            return;
        }
        albumArtImageView.setImageResource(R.drawable.album_art_empty);
    }






}
