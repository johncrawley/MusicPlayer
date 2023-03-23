package com.jacstuff.musicplayer.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;

public class AlbumArtHelper {

    private final MainActivity mainActivity;
    private final ImageView albumArtImageView;
    private Bitmap currentAlbumArt;

    public AlbumArtHelper(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        this.albumArtImageView = mainActivity.findViewById(R.id.albumArtImageView);
    }


    public void changeAlbumArtTo(Bitmap updatedAlbumArt){
        mainActivity.runOnUiThread(()-> {
            if (isCurrentCoverTheSame(updatedAlbumArt)) {
                return;
            }
            changeImageTo(updatedAlbumArt);
        });
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


    private void changeImageTo(Bitmap updatedAlbumArt){
        currentAlbumArt = updatedAlbumArt;
        Animation fadeOut = AnimationUtils.loadAnimation(mainActivity, R.anim.fade_out);
        albumArtImageView.startAnimation(fadeOut);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                assignAlbumArt(updatedAlbumArt);
                Animation fadeIn = AnimationUtils.loadAnimation(mainActivity, R.anim.fade_in);
                albumArtImageView.startAnimation(fadeIn);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }


    private void assignAlbumArt(Bitmap updatedAlbumArt){
        if (updatedAlbumArt != null) {
            albumArtImageView.setImageDrawable(new BitmapDrawable(mainActivity.getResources(), updatedAlbumArt));
            return;
        }
        albumArtImageView.setImageResource(R.drawable.album_art_empty);

    }





}
