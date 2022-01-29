package com.jacstuff.musicplayer;


import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.jacstuff.musicplayer.db.TrackRepository;


public class AudioInfoLoader {

    private final Context context;
    private final TrackRepository trackRepository;

    public AudioInfoLoader(Context context, TrackRepository trackRepository){
        this.context  = context;
        this.trackRepository = trackRepository;
    }



    public void loadAudioFiles(){

        String[] projection2 = new String[] {
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATA
                //MediaStore.Audio.Media.CD_TRACK_NUMBER,
                //MediaStore.Audio.Media.GENRE
                };

        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER + " ASC";
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection2, selection, selectionArgs, sortOrder);

        if(cursor == null){
            return;
        }

        while(cursor.moveToNext()){

            // NB Genre and TrackNumber require Android Version N
            String data = getCol(cursor, MediaStore.Audio.Media.DATA);
            String artist  = getCol(cursor, MediaStore.Audio.Media.ARTIST);
            String album  = getCol(cursor, MediaStore.Audio.Media.ALBUM);
            String title = getCol(cursor, MediaStore.Audio.Media.TITLE);


            Track track = new Track.Builder().createTrackWithPathname(data)
                    .withAlbum(album)
                    .withArtist(artist)
                    .withName(title)
                    //.withGenre(genre)
                    .build();
            trackRepository.addTrack(track);
        }
        cursor.close();
    }


    private String getCol(Cursor cursor, String colName){
        int col = cursor.getColumnIndexOrThrow(colName);
        return cursor.getString(col);
    }

}