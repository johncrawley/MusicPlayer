package com.jacstuff.musicplayer.playlist;


import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;


import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.db.track.Track;
import com.jacstuff.musicplayer.db.track.TrackRepository;

import java.util.Arrays;
import java.util.List;


public class AudioInfoLoader {

    private final Context context;
    private final TrackRepository trackRepository;

    public AudioInfoLoader(Context context, TrackRepository trackRepository){
        this.context  = context;
        this.trackRepository = trackRepository;
    }


    public void loadAudioFiles(){
        Cursor cursor = createCursorForFilesystemTracks();
        if(cursor != null){
            while(cursor.moveToNext()){
                addTrack(cursor);
            }
            cursor.close();
        }
    }


    private Cursor createCursorForFilesystemTracks(){
        String[] projection = createProjection0();
        System.out.flush();
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER + " ASC";
        return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }


    private String[] createProjection(){
        List<String> projectionFields = Arrays.asList(
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        );

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            projectionFields.add(MediaStore.Audio.Media.CD_TRACK_NUMBER);
        }
        String[] projection = new String[projectionFields.size()];
        for(int i=0;i< projectionFields.size();i++){
            projection[i] = projectionFields.get(i);
        }
        return projection;
    }


    private String[] createProjection0() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return new String[]{
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.CD_TRACK_NUMBER,
                    MediaStore.Audio.Media.GENRE};
        }
        return new String[]{
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        };
    }


    private void addTrack(Cursor cursor){
        String data = getCol(cursor, MediaStore.Audio.Media.DATA);
        if(!isContainingCorrectPath(data)){
            return;
        }
        String artist  = getCol(cursor, MediaStore.Audio.Media.ARTIST);
        String album  = getCol(cursor, MediaStore.Audio.Media.ALBUM);
        String title = getCol(cursor, MediaStore.Audio.Media.TITLE);
        long duration = getLong(cursor, MediaStore.Audio.Media.DURATION);
        long trackNumber = 0L;
        String genre = "";

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            trackNumber = getLong(cursor, MediaStore.Audio.Media.CD_TRACK_NUMBER);
            genre = getCol(cursor, MediaStore.Audio.Media.GENRE);
        }

        Track track = new Track.Builder().createTrackWithPathname(data)
                .withAlbum(album)
                .withArtist(artist)
                .withName(title)
                .duration(duration)
                .withTrackNumber(trackNumber)
                .withGenre(genre)
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


    private Long getLong(Cursor cursor, String colName){
        int col = cursor.getColumnIndexOrThrow(colName);
        return cursor.getLong(col);
    }

}