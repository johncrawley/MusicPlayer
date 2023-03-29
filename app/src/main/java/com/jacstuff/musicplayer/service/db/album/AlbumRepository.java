package com.jacstuff.musicplayer.service.db.album;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jacstuff.musicplayer.service.db.AbstractRepository;
import com.jacstuff.musicplayer.service.db.DbContract;

import java.util.ArrayList;
import java.util.List;

public class AlbumRepository extends AbstractRepository {


    public AlbumRepository(Context context){
        super(context);
    }


    public long addOrGet(String album){
        cursor = searchFor(album);
        if(cursor.getCount() == 0){
            cursor.close();
            return add(album);
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


    public List<Album> getAll(){
        List<Album> albums = new ArrayList<>(1000);
        cursor =  db.query(DbContract.AlbumsEntry.TABLE_NAME, null, null , null, null, null, null);
        while(cursor.moveToNext()){
            long id = getLong(DbContract.AlbumsEntry._ID);
            String  name= getString(DbContract.AlbumsEntry.COL_NAME);
            albums.add(new Album(id, name));
        }
        return albums;
    }


    public Cursor searchFor(String album){
        String[] args = { album.trim() };
        String x = "=?";
        String selection = DbContract.AlbumsEntry.COL_NAME + x;
        return db.query(DbContract.AlbumsEntry.TABLE_NAME, null, selection , args, null, null, null);
    }


    private long add(String artist){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.AlbumsEntry.COL_NAME, artist);
        return addValuesToTable(db, DbContract.AlbumsEntry.TABLE_NAME, contentValues);
    }



}