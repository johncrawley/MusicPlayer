package com.jacstuff.musicplayer.view;

import static com.jacstuff.musicplayer.search.AnimatorHelper.createShowAnimatorFor;

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
import com.jacstuff.musicplayer.search.AnimatorHelper;
import com.jacstuff.musicplayer.utils.FragmentHelper;

public class AlbumArtHelper {

    private final MainActivity mainActivity;
    private final ImageView albumArtImageView, albumArtLargeImageView;
    private Bitmap currentAlbumArt;
    private OnBackPressedCallback dismissAlbumArtLargeViewOnBackPressedCallback;
    private final View albumArtLargeView;


    public AlbumArtHelper(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        currentAlbumArt = mainActivity.getMediaPlayerService().getAlbumArt();
        this.albumArtImageView = mainActivity.findViewById(R.id.albumArtImageView);
        this.albumArtLargeView = mainActivity.findViewById(R.id.albumArtLargeView);
        albumArtLargeImageView = mainActivity.findViewById(R.id.albumArtLargeImageView);
        assignAlbumArt(albumArtImageView, currentAlbumArt);
        setupAlbumViewClick();
        setupDismissSearchOnBackPressed();
    }


    private void setupDismissSearchOnBackPressed(){
        dismissAlbumArtLargeViewOnBackPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                hideAlbumArtLargeView();
            }
        };
        mainActivity.getOnBackPressedDispatcher().addCallback(mainActivity, dismissAlbumArtLargeViewOnBackPressedCallback);
    }


    private void hideAlbumArtLargeView(){
        if(albumArtLargeView.getVisibility() != View.VISIBLE){
            return;
        }
        Animator animator = AnimatorHelper.createHideAnimatorFor(albumArtLargeView, ()-> albumArtLargeView.setVisibility(View.GONE));
        dismissAlbumArtLargeViewOnBackPressedCallback.setEnabled(false);
        animator.start();
    }


    private void showAlbumArtLargeView(){
        Animator animator = createShowAnimatorFor(albumArtLargeView, ()-> {});
        albumArtLargeView.setVisibility(View.VISIBLE);
        dismissAlbumArtLargeViewOnBackPressedCallback.setEnabled(true);
        animator.start();
    }


    public void changeAlbumArtTo(Bitmap updatedAlbumArt){
        mainActivity.runOnUiThread(()-> {
            if (isCurrentCoverTheSame(updatedAlbumArt)) {
                return;
            }
            changeImageTo(albumArtImageView, updatedAlbumArt);
            changeImageTo(albumArtLargeImageView, updatedAlbumArt);
        });
    }


    public void changeAlbumArtToCurrent(ImageView imageView){
        changeImageTo(imageView, currentAlbumArt);
    }


    public void changeAlbumArtTo(ImageView imageView, Bitmap updatedAlbumArt){
        if(updatedAlbumArt == null){
            return;
        }
        changeImageTo(imageView, updatedAlbumArt);
    }


    private void setupAlbumViewClick(){
        albumArtImageView.setOnClickListener(v-> showAlbumArtLargeView());
    }


    private void startAlbumArtDialog(){
        if(currentAlbumArt == null){
            return;
        }
        String tag = "show_large_album_art_dialog";
        FragmentTransaction fragmentTransaction = FragmentHelper.createTransaction(mainActivity, tag);
        AlbumArtDialog.newInstance().show(fragmentTransaction, tag);
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


    private void changeImageTo(ImageView imageView, Bitmap updatedAlbumArt){
        currentAlbumArt = updatedAlbumArt;
        Animation fadeOut = AnimationUtils.loadAnimation(mainActivity, R.anim.fade_out);
        imageView.startAnimation(fadeOut);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                assignAlbumArt(imageView, updatedAlbumArt);
                Animation fadeIn = AnimationUtils.loadAnimation(mainActivity, R.anim.fade_in);
                imageView.startAnimation(fadeIn);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
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
