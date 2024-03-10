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
    private Bitmap blankAlbumArt;


    public AlbumArtRetriever(MediaPlayerService mediaPlayerService, Context context){
        this.mediaPlayerService = mediaPlayerService;
        this.context = context;
        loadBlankAlbumArt();
    }


    public void assignAlbumArt(Track track) throws IOException, IllegalArgumentException{
        try {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(track.getPathname());
            currentAlbumArt = retrieveAlbumArt(mediaMetadataRetriever);
            mediaPlayerService.setAlbumArtOnMainView(currentAlbumArt);
            mediaMetadataRetriever.close();
        }catch(IOException | IllegalArgumentException e){
            currentAlbumArt = blankAlbumArt;
            throw e;
        }
    }


    private Bitmap retrieveAlbumArt(MediaMetadataRetriever mediaMetadataRetriever){
        byte[] coverArt = mediaMetadataRetriever.getEmbeddedPicture();
        if (coverArt != null) {
            return BitmapFactory.decodeByteArray(coverArt, 0, coverArt.length);
        }
        return null;
    }


    public Bitmap getAlbumArtForNotification(){
        if (currentAlbumArt != null){
            return createScaledBitmapForLargeIcon(currentAlbumArt);
        }
        if(context == null){
            return null;
        }
        currentAlbumArt = blankAlbumArt;
        return blankAlbumArt;
    }


    private void loadBlankAlbumArt(){
        try{
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.album_art_empty);
            if(bitmap == null){
               return;
            }
            blankAlbumArt = createScaledBitmapForLargeIcon(bitmap);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public Bitmap getCurrentAlbumArt(){
        return currentAlbumArt;
    }


    private Bitmap createScaledBitmapForLargeIcon(Bitmap bitmap){
        return Bitmap.createScaledBitmap(bitmap, 128, 128, false);
    }
}
