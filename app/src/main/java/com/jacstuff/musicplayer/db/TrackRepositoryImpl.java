package com.jacstuff.musicplayer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import static com.jacstuff.musicplayer.db.DbContract.TracksEntry;

import com.jacstuff.musicplayer.Track;

import java.util.ArrayList;
import java.util.List;

public class TrackRepositoryImpl implements TrackRepository{

    private final SQLiteDatabase db;


    public TrackRepositoryImpl(Context context){
        DbHelper dbHelper = DbHelper.getInstance(context);
        db = dbHelper.getWritableDatabase();
    }


    @Override
    public void addTrack(Track track) {
        DbUtils.addValuesToTable(db, DbContract.TracksEntry.TABLE_NAME,
        createContentValuesFor(track));
    }


    @Override
    public void deleteTrack(Track track) {
        String deleteTrackQuery = "DELETE FROM "
                + DbContract.TracksEntry.TABLE_NAME
                + " WHERE " + TracksEntry.COL_PATH
                + " = \""  + track.getPathname()
                + "\";";
        try {
            db.execSQL(deleteTrackQuery);
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    private Cursor cursor;

    @Override
    public List<Track> getAllTracks() {
        List<Track> tracks = new ArrayList<>();
        String query = "SELECT * FROM " + TracksEntry.TABLE_NAME + ";";

        try {
            cursor = db.rawQuery(query, null);
            while(cursor.moveToNext()){
                tracks.add(new Track.Builder()
                        .createTrackWithPathname(getString(TracksEntry.COL_PATH))
                        .withName(getString(TracksEntry.COL_NAME))
                        .withArtist(getString(TracksEntry.COL_ARTIST))
                        .withAlbum(getString(TracksEntry.COL_ALBUM))
                        .build());
            }
        }
        catch(SQLException e){
            e.printStackTrace();
            return tracks;
        }
        cursor.close();
        return  tracks;
    }


    private ContentValues createContentValuesFor(Track track){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.TracksEntry.COL_NAME, track.getName());
        contentValues.put(DbContract.TracksEntry.COL_ARTIST, track.getArtist());
        contentValues.put(DbContract.TracksEntry.COL_ALBUM, track.getAlbum());
        contentValues.put(DbContract.TracksEntry.COL_PATH, track.getPathname());
        contentValues.put(DbContract.TracksEntry.COL_ALBUM, track.getAlbum());
        return contentValues;
    }




    private String getString(String name){
        return cursor.getString(cursor.getColumnIndexOrThrow(name));
    }


    private long getLong(String name){
        return cursor.getLong(cursor.getColumnIndexOrThrow(name));
    }

}
