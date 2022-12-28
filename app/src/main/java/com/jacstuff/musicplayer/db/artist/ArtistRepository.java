package com.jacstuff.musicplayer.db.artist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jacstuff.musicplayer.db.AbstractRepository;
import com.jacstuff.musicplayer.db.DbContract;

public class ArtistRepository extends AbstractRepository {


    public ArtistRepository(Context context){
        super(context);
    }


    public long addOrGetArtist(String artist){
        cursor = searchForArtist(artist);
        if(cursor.getCount() == 0){
            cursor.close();
            return addArtist(artist);
        }
        cursor.moveToNext();
        long id = -1;
        try{
            id = getLong(DbContract.ArtistsEntry._ID);
        }catch (RuntimeException e){
            e.printStackTrace();
        }
        cursor.close();
        return id;
    }



    public Cursor searchForArtist(String artist){
        String[] args = { artist.trim() };
        String x = "=?";
        String selection = DbContract.ArtistsEntry.COL_NAME + x;
        return db.query(DbContract.ArtistsEntry.TABLE_NAME, null, selection , args, null, null, null);
    }



    private long addArtist(String artist){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.ArtistsEntry.COL_NAME, artist);
        return addValuesToTable(db, DbContract.ArtistsEntry.TABLE_NAME, contentValues);
    }



}
