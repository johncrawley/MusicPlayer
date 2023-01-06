package com.jacstuff.musicplayer.db.album;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jacstuff.musicplayer.db.AbstractRepository;
import com.jacstuff.musicplayer.db.DbContract;
import com.jacstuff.musicplayer.db.artist.Artist;

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


    public List<Artist> getAll(){
        List<Artist> albums = new ArrayList<>(1000);
        cursor =  db.query(DbContract.AlbumsEntry.TABLE_NAME, null, null , null, null, null, null);
        while(cursor.moveToNext()){
            long id = getLong(DbContract.AlbumsEntry._ID);
            String  name= getString(DbContract.AlbumsEntry.COL_NAME);
            albums.add(new Artist(id, name));
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