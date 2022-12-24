package com.jacstuff.musicplayer.db.artist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jacstuff.musicplayer.db.AbstractRepository;
import com.jacstuff.musicplayer.db.DbContract;
import com.jacstuff.musicplayer.db.DbHelper;
import com.jacstuff.musicplayer.db.DbUtils;

public class ArtistRepository extends AbstractRepository {


    public ArtistRepository(Context context){
        super(context);
    }


    public long addOrGetArtist(String artist){
        cursor = searchForArtist(artist);
        log("addOrGetArtist() returned cursor from searchForArtist()");
        if(cursor.getCount() == 0){
            cursor.close();
            log("addOrGetArtist() about to add Artist");
            return addArtist(artist);
        }
        String[] colNames = cursor.getColumnNames();
        cursor.moveToNext();
        long id = -1;
        try{
            id = getLong(DbContract.ArtistsEntry._ID);
        }catch (RuntimeException e){
            e.printStackTrace();
        }

        log("id from cursor: " + id);
        cursor.close();
        return id;
    }


    private void log(String msg){
        System.out.println("^^^ ArtistRepository: " + msg);
    }


    public Cursor searchForArtist(String artist){
        log("Entered searchForArist()");
        String[] args = { artist.trim() };
        String x = "=?";
        String selection = DbContract.ArtistsEntry.COL_NAME + x;
        return db.query(DbContract.ArtistsEntry.TABLE_NAME, null, selection , args, null, null, null);
    }



    private long addArtist(String artist){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.ArtistsEntry.COL_NAME, artist);
        return DbUtils.addValuesToTable(db, DbContract.ArtistsEntry.TABLE_NAME, contentValues);
    }



}
