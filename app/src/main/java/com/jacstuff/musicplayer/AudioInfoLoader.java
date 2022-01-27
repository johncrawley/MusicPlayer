package com.jacstuff.musicplayer;


import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.jacstuff.musicplayer.db.TrackRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AudioInfoLoader {

    private final Context context;
    private final List <Track> trackList;
    private final TrackRepository trackRepository;

    public AudioInfoLoader(Context context, TrackRepository trackRepository){
        this.context  = context;
        trackList = new ArrayList<>();
        this.trackRepository = trackRepository;
    }



    public List<Track> loadAudioFiles(){

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
            return Collections.emptyList();
        }

        while(cursor.moveToNext()){

            String displayName = getCol(cursor, MediaStore.Audio.Media.DISPLAY_NAME);
            String artist  = getCol(cursor, MediaStore.Audio.Media.ARTIST);
            String album  = getCol(cursor, MediaStore.Audio.Media.ALBUM);
            String title = getCol(cursor, MediaStore.Audio.Media.TITLE);
            String data = getCol(cursor, MediaStore.Audio.Media.DATA);

           // String trackNumber = getCol(cursor, MediaStore.Audio.Media.CD_TRACK_NUMBER);
            //String genre = getCol(cursor, MediaStore.Audio.Media.GENRE);

            Track track = new Track.Builder().createTrackWithPathname(data)
                    .withAlbum(album)
                    .withArtist(artist)
                    .withName(title)
                    //.withGenre(genre)
                    .build();
            trackList.add(track);
            trackRepository.addTrack(track);
        }
        cursor.close();
        return trackList;
    }


    private String getCol(Cursor cursor, String colName){
        int col = cursor.getColumnIndexOrThrow(colName);
        return cursor.getString(col);
    }

}