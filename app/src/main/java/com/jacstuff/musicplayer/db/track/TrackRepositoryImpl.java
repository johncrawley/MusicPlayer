package com.jacstuff.musicplayer.db.track;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import static com.jacstuff.musicplayer.db.DbContract.TracksEntry;

import com.jacstuff.musicplayer.db.DbContract;
import com.jacstuff.musicplayer.db.DbHelper;
import com.jacstuff.musicplayer.db.DbUtils;

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
                TracksEntry.TABLE_NAME,
                createContentValuesFor(track));
    }


    @Override
    public void deleteTrack(Track track) {
        String deleteTrackQuery = "DELETE FROM "
                + DbContract.TracksEntry.TABLE_NAME
                + " WHERE " + TracksEntry._ID
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
        return  getTracksUsingQuery("SELECT * FROM " + TracksEntry.TABLE_NAME + ";");
    }


    public List<Track> getAllTracksStartingWith(String prefix){
        String query = "SELECT * FROM " + TracksEntry.TABLE_NAME
                + " WHERE " + TracksEntry.COL_NAME + " GLOB '" + prefix + " *';";
        return getTracksUsingQuery( query);
    }


    private List<Track> getTracksUsingQuery(String query){
        List<Track> tracks = new ArrayList<>();
        try {
            cursor = db.rawQuery(query, null);
            tracks = getTracksFromCursor(cursor);
        }
        catch(SQLException e){
            e.printStackTrace();
            return tracks;
        }
        cursor.close();
        return  tracks;
    }


    private List<Track> getTracksFromCursor(Cursor cursor){
        List<Track> tracks = new ArrayList<>();
        while(cursor.moveToNext()){
            tracks.add(new Track.Builder()
                    .createTrackWithPathname(getString(TracksEntry.COL_PATH))
                    .withId(getLong(TracksEntry._ID))
                    .withName(getString(TracksEntry.COL_NAME))
                    .withTrackNumber(getLong(TracksEntry.COL_TRACK_NUMBER))
                    .withArtist(getString(TracksEntry.COL_ARTIST))
                    .withAlbum(getString(TracksEntry.COL_ALBUM))
                    .build());
        }
        return tracks;
    }



    public List<Track> searchForTracks(String searchTerms){
        String query = "SELECT * FROM " + TracksEntry.TABLE_NAME + " WHERE " +
                TracksEntry.COL_ARTIST + " LIKE ?;";
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
        return new Track.Builder()
                .createTrackWithPathname(getString(TracksEntry.COL_PATH))
                .withId(getLong(TracksEntry._ID))
                .withName(getString(TracksEntry.COL_NAME))
                .withTrackNumber(-1)
                .withArtist(getString(TracksEntry.COL_ARTIST))
                .withAlbum(getString(TracksEntry.COL_ALBUM))
                .build();
    }


    private ContentValues createContentValuesFor(Track track){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.TracksEntry.COL_NAME, track.getName());
        contentValues.put(DbContract.TracksEntry.COL_ARTIST, track.getArtist());
        contentValues.put(DbContract.TracksEntry.COL_ALBUM, track.getAlbum());
        contentValues.put(DbContract.TracksEntry.COL_PATH, track.getPathname());
        contentValues.put(DbContract.TracksEntry.COL_ALBUM, track.getAlbum());
        contentValues.put(TracksEntry.COL_TRACK_NUMBER, track.getTrackNumber());
        return contentValues;
    }


    private String getString(String name){
        return cursor.getString(cursor.getColumnIndexOrThrow(name));
    }


    private long getLong(String name){
        return cursor.getLong(cursor.getColumnIndexOrThrow(name));
    }

}
