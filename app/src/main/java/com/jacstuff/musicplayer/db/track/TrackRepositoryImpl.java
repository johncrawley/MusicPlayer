package com.jacstuff.musicplayer.db.track;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.jacstuff.musicplayer.db.DbHelper;
import com.jacstuff.musicplayer.db.DbUtils;
import static com.jacstuff.musicplayer.db.DbContract.TracksEntry.*;

import java.util.ArrayList;
import java.util.List;

public class TrackRepositoryImpl implements TrackRepository{

    private final SQLiteDatabase db;
    private Cursor cursor;


    public TrackRepositoryImpl(Context context){
        DbHelper dbHelper = DbHelper.getInstance(context);
        db = dbHelper.getWritableDatabase();
    }


    @Override
    public void addTrack(Track track) {
        DbUtils.addValuesToTable(db,
                TABLE_NAME,
                createContentValuesFor(track));
    }


    @Override
    public void deleteTrack(Track track) {
        String deleteTrackQuery = "DELETE FROM "
                + TABLE_NAME
                + " WHERE " + _ID
                + " = "  + track.getId()
                + ";";
        try {
            db.execSQL(deleteTrackQuery);
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }


    @Override
    public List<Track> getAllTracks() {
        return  getTracksUsingQuery("SELECT * FROM " + TABLE_NAME + ";");
    }


    public List<Track> getAllTracksStartingWith(String prefix){
        String query = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + COL_NAME + " GLOB '" + prefix + " *';";
        return getTracksUsingQuery( query);
    }


    private List<Track> getTracksUsingQuery(String query){
        List<Track> tracks = new ArrayList<>();
        try {
            cursor = db.rawQuery(query, null);
            tracks = getTracksFromCursor();
        }
        catch(SQLException e){
            e.printStackTrace();
            return tracks;
        }
        cursor.close();
        return  tracks;
    }


    private List<Track> getTracksFromCursor(){
        List<Track> tracks = new ArrayList<>();
        while(cursor.moveToNext()){
            tracks.add(createTrackFromCursor());
        }
        return tracks;
    }


    public List<Track> searchForTracks(String searchTerms){
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " +
                COL_ARTIST + " LIKE ?;";
        String[] selectionArgs = new String[] { "searchTerms"};
        return getTracks(query, selectionArgs);
    }


    private List<Track> getTracks(String query, String[] selectionArgs){
        List<Track> tracks = new ArrayList<>();
        try {
            cursor = db.rawQuery(query, selectionArgs);
            while(cursor.moveToNext()){
                tracks.add(createTrackFromCursor());
            }
        }
        catch(SQLException e){
            e.printStackTrace();
            return tracks;
        }
        cursor.close();
        return  tracks;
    }


    private Track createTrackFromCursor(){
        long duration = getLong(COL_DURATION);
        return new Track.Builder()
                .createTrackWithPathname(getString(COL_PATH))
                .withId(getLong(_ID))
                .withName(getString(COL_NAME))
                .withTrackNumber(getLong(COL_TRACK_NUMBER))
                .withArtist(getString(COL_ARTIST))
                .withAlbum(getString(COL_ALBUM))
                .duration(duration)
                .withGenre(COL_GENRE)
                .build();
    }


    private ContentValues createContentValuesFor(Track track){
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NAME, track.getName());
        contentValues.put(COL_ARTIST, track.getArtist());
        contentValues.put(COL_ALBUM, track.getAlbum());
        contentValues.put(COL_PATH, track.getPathname());
        contentValues.put(COL_ALBUM, track.getAlbum());
        contentValues.put(COL_DURATION, track.getDuration());
        contentValues.put(COL_GENRE, track.getGenre());
        contentValues.put(COL_TRACK_NUMBER, track.getTrackNumber());
        return contentValues;
    }


    private String getString(String name){
        return cursor.getString(cursor.getColumnIndexOrThrow(name));
    }


    private long getLong(String name){
        return cursor.getLong(cursor.getColumnIndexOrThrow(name));
    }

}
