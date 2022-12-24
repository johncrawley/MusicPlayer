package com.jacstuff.musicplayer.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AbstractRepository {

    protected final SQLiteDatabase db;
    protected Cursor cursor;


    public AbstractRepository(Context context){
        DbHelper dbHelper = DbHelper.getInstance(context);
        db = dbHelper.getWritableDatabase();
    }

    protected String getString(String name){
        return cursor.getString(cursor.getColumnIndexOrThrow(name));
    }


    protected long getLong(String name){
        return cursor.getLong(cursor.getColumnIndexOrThrow(name));
    }
}
