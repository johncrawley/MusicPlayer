package com.jacstuff.musicplayer.db.artist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jacstuff.musicplayer.db.AbstractRepository;
import com.jacstuff.musicplayer.db.DbContract;

import java.util.ArrayList;
import java.util.List;

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


    public List<Artist> getAllArtists(){
        List<Artist> artists = new ArrayList<>(100);
        cursor =  db.query(DbContract.ArtistsEntry.TABLE_NAME, null, null , null, null, null, null);
        while(cursor.moveToNext()){
            long id = getLong(DbContract.ArtistsEntry._ID);
            String  name= getString(DbContract.ArtistsEntry.COL_NAME);
            artists.add(new Artist(id, name));
        }
        return artists;
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
