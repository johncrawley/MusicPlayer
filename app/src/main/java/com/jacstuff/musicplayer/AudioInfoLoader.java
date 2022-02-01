package com.jacstuff.musicplayer;


import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.jacstuff.musicplayer.db.TrackRepository;
import com.jacstuff.musicplayer.viewmodel.MainViewModel;


public class AudioInfoLoader {

    private final Context context;
    private final TrackRepository trackRepository;
    private final MainViewModel viewModel;

    public AudioInfoLoader(Context context, TrackRepository trackRepository, MainViewModel viewModel){
        this.context  = context;
        this.trackRepository = trackRepository;
        this.viewModel = viewModel;
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

        if(cursor != null){
            while(cursor.moveToNext()){
                addTrack(cursor);
            }
            cursor.close();
        }

    }


    private void addTrack(Cursor cursor){
        String data = getCol(cursor, MediaStore.Audio.Media.DATA);
        if(!isContainingCorrectPath(data)){
            return;
        }
        // NB Genre and TrackNumber require Android Version N
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


    private boolean isContainingCorrectPath(String path){
        String expectedPath = context.getString(R.string.default_path);
        return path.contains(expectedPath);
    }


    private String getCol(Cursor cursor, String colName){
        int col = cursor.getColumnIndexOrThrow(colName);
        return cursor.getString(col);
    }

}