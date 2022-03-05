package com.jacstuff.musicplayer.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.jacstuff.musicplayer.Track;

import java.util.List;

public class DbUtils {


    public static long addValuesToTable(SQLiteDatabase db, String tableName, ContentValues contentValues){
        long id = -1;
        db.beginTransaction();
        try {
            id = db.insertOrThrow(tableName, null, contentValues);
            db.setTransactionSuccessful();
        }catch (android.database.sqlite.SQLiteConstraintException e){
            //do nothing
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        db.endTransaction();
        return id;
    }


    public static void addTrackTo(List<Track> tracks, Cursor cursor){
        tracks.add(new Track.Builder()
                .createTrackWithPathname(getString(cursor, DbContract.TracksEntry.COL_PATH))
                .withId(getLong(cursor, DbContract.TracksEntry._ID))
                .withName(getString(cursor, DbContract.TracksEntry.COL_NAME))
                .withTrackNumber(-1)
                .withArtist(getString(cursor, DbContract.TracksEntry.COL_ARTIST))
                .withAlbum(getString(cursor, DbContract.TracksEntry.COL_ALBUM))
                .build());
    }


    private static String getString(Cursor cursor, String name){
        return cursor.getString(cursor.getColumnIndexOrThrow(name));
    }


    private static long getLong(Cursor cursor, String name){
        return cursor.getLong(cursor.getColumnIndexOrThrow(name));
    }

}
