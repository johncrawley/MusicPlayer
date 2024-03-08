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

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.view.utils.AnimatorHelper;
import com.jacstuff.musicplayer.view.viewmodel.MainViewModel;

public class AlbumArtHelper {

    private final MainActivity mainActivity;
    private final ImageView albumArtImageView, albumArtLargeImageView;
    private OnBackPressedCallback dismissAlbumArtLargeViewOnBackPressedCallback;
    private final View albumArtLargeView;
    private final MainViewModel viewModel;

    public AlbumArtHelper(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        viewModel = mainActivity.getViewModel();
        viewModel.currentAlbumArt = mainActivity.getMediaPlayerService().getAlbumArt();
        this.albumArtImageView = mainActivity.findViewById(R.id.albumArtImageView);
        this.albumArtLargeView = mainActivity.findViewById(R.id.albumArtLargeView);
        albumArtLargeImageView = mainActivity.findViewById(R.id.albumArtLargeImageView);
        assignCurrentArtToImageViews();
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


    private void assignCurrentArtToImageViews(){
        assignAlbumArt(albumArtImageView, viewModel.currentAlbumArt);
        assignAlbumArt(albumArtLargeImageView, viewModel.currentAlbumArt);
    }


    public void changeAlbumArtToBlank(){
        assignBlankAlbumArt(albumArtImageView);
        assignBlankAlbumArt(albumArtLargeImageView);
    }


    private void showLargeAlbumArt(){
        Animator animator = createShowAnimatorFor(albumArtLargeView, ()-> {});
        albumArtLargeView.setVisibility(View.VISIBLE);
        dismissAlbumArtLargeViewOnBackPressedCallback.setEnabled(true);
        animator.start();
    }


    private void hideLargeAlbumArt(){
        if(albumArtLargeView.getVisibility() != View.VISIBLE){
            return;
        }
        Animator animator = AnimatorHelper.createHideAnimatorFor(albumArtLargeView,
                ()-> albumArtLargeView.setVisibility(View.GONE));
        dismissAlbumArtLargeViewOnBackPressedCallback.setEnabled(false);
        animator.start();
    }


    public void changeAlbumArtTo(Bitmap updatedAlbumArt){
        mainActivity.runOnUiThread(()-> {
            if (isCurrentCoverTheSame(updatedAlbumArt)) {
                return;
            }
            boolean isLargeAlbumArtVisible = albumArtLargeView.getVisibility() == View.VISIBLE;
            changeImageTo(albumArtImageView, updatedAlbumArt, !isLargeAlbumArtVisible);
            changeImageTo(albumArtLargeImageView, updatedAlbumArt, isLargeAlbumArtVisible);
            viewModel.currentAlbumArt = updatedAlbumArt;
        });
    }


    private void setupAlbumViewClick(){
        albumArtImageView.setOnClickListener(v-> showLargeAlbumArt());
        albumArtLargeImageView.setOnClickListener(v -> hideLargeAlbumArt());
    }


    private boolean isCurrentCoverTheSame(Bitmap updatedAlbumArt){
        if(viewModel.currentAlbumArt == null && updatedAlbumArt == null){
            return true;
        }
        if(viewModel.currentAlbumArt == null || updatedAlbumArt == null){
            return false;
        }
        return viewModel.currentAlbumArt.sameAs(updatedAlbumArt);
    }


    private void changeImageTo(ImageView imageView, Bitmap updatedAlbumArt, boolean shouldAnimate){
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
        assignBlankAlbumArt(imageView);
    }


    public void assignBlankAlbumArt(ImageView imageView){
        imageView.setImageResource(R.drawable.album_art_empty);
    }


}
