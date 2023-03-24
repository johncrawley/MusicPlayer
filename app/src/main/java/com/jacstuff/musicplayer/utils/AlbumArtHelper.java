package com.jacstuff.musicplayer.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.fragment.app.FragmentTransaction;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.fragments.albumart.AlbumArtDialog;

public class AlbumArtHelper {

    private final MainActivity mainActivity;
    private final ImageView albumArtImageView;
    private Bitmap currentAlbumArt;

    public AlbumArtHelper(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        currentAlbumArt = mainActivity.getMediaPlayerService().getAlbumArt();
        this.albumArtImageView = mainActivity.findViewById(R.id.albumArtImageView);
        assignAlbumArt(albumArtImageView, currentAlbumArt);
        setupAlbumViewClick();
    }


    public void changeAlbumArtTo(Bitmap updatedAlbumArt){
        mainActivity.runOnUiThread(()-> {
            if (isCurrentCoverTheSame(updatedAlbumArt)) {
                return;
            }
            changeImageTo(albumArtImageView, updatedAlbumArt);
        });
    }


    public void changeAlbumArtToCurrent(ImageView imageView){
        if(currentAlbumArt == null){
            return;
        }
        changeImageTo(imageView, currentAlbumArt);
    }


    public void changeAlbumArtTo(ImageView imageView, Bitmap updatedAlbumArt){
        if(updatedAlbumArt == null){
            return;
        }
        changeImageTo(imageView, updatedAlbumArt);
    }


    private void setupAlbumViewClick(){
        albumArtImageView.setOnClickListener(v->startAlbumArtDialog());
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
