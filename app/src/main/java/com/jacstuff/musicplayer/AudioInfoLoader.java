package com.jacstuff.musicplayer;


import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AudioInfoLoader {

    private Context context;
    private List <Track> trackDetailsList;

    public AudioInfoLoader(Context context){
        this.context  = context;
        trackDetailsList = new ArrayList<>();
    }


    public List<Track> listAudioFiles(){

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

            Track trackDetails = new Track.Builder().createTrackWithPathname(data)
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