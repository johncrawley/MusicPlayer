package com.jacstuff.musicplayer.service.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.MediaPlayerService;
import com.jacstuff.musicplayer.service.db.track.Track;

import java.io.IOException;

public class AlbumArtRetriever {

    private final MediaPlayerService mediaPlayerService;
    private final Context context;
    private Bitmap currentAlbumArt;

    public AlbumArtRetriever(MediaPlayerService mediaPlayerService, Context context){
        this.mediaPlayerService = mediaPlayerService;
        this.context = context;
    }


    public void assignAlbumArt(Track track) throws IOException, IllegalArgumentException{
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(track.getPathname());
        currentAlbumArt = retrieveAlbumArt(mediaMetadataRetriever);
        mediaPlayerService.setAlbumArtOnMainView(currentAlbumArt);
        mediaMetadataRetriever.close();
    }


    private Bitmap retrieveAlbumArt(MediaMetadataRetriever mediaMetadataRetriever){
        byte[] coverArt = mediaMetadataRetriever.getEmbeddedPicture();
        if (coverArt != null) {
            return BitmapFactory.decodeByteArray(coverArt, 0, coverArt.length);
        }
        return null;
    }


    public Bitmap getAlbumArtForNotification(){
        if (mediaPlayerService.getAlbumArt() != null){
            return createScaledBitmapForLargeIcon(mediaPlayerService.getAlbumArt());
        }
        if(context == null){
            return null;
        }
        try{
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.album_art_empty);
            if(bitmap == null){
                return null;
            }
            return createScaledBitmapForLargeIcon(bitmap);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    public Bitmap getCurrentAlbumArt(){
        return currentAlbumArt;
    }


    private Bitmap createScaledBitmapForLargeIcon(Bitmap bitmap){
        return Bitmap.createScaledBitmap(bitmap, 128, 128, false);
    }
}
