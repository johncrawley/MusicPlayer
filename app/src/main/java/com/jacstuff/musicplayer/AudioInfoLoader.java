package com.jacstuff.musicplayer;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AudioInfoLoader {

    private Context context;
    private List <TrackDetails> trackDetailsList;

    public AudioInfoLoader(Context context){
        this.context  = context;
        trackDetailsList = new ArrayList<>();
    }


    public List<TrackDetails> listAudioFiles(){

        String[] projection2 = new String[] {
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATA
                };

        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER + " ASC";
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection2, selection, selectionArgs, sortOrder);

        if(cursor == null){
            return Collections.emptyList();
        }

        while(cursor.moveToNext()){

            String displayName = getCol(cursor, MediaStore.Audio.Media.DISPLAY_NAME);
            String artist  = getCol(cursor, MediaStore.Audio.Media.ARTIST);
            String album  = getCol(cursor, MediaStore.Audio.Media.ALBUM);
            String title = getCol(cursor, MediaStore.Audio.Media.TITLE);
            String data = getCol(cursor, MediaStore.Audio.Media.DATA);

            TrackDetails trackDetails = new TrackDetails.Builder().createTrackWithPathname(data)
                    .withAlbum(album)
                    .withArtist(artist)
                    .withName(title)
                    .build();
            trackDetailsList.add(trackDetails);
        }
        cursor.close();
        return trackDetailsList;
    }


    private String getCol(Cursor cursor, String colName){
        int col = cursor.getColumnIndexOrThrow(colName);
        return cursor.getString(col);
    }

}