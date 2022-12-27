package com.jacstuff.musicplayer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
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

    public long addValuesToTable(SQLiteDatabase db, String tableName, ContentValues contentValues){
        log("Entered addValuesToTable()");
        long id = -1;
        db.beginTransaction();
        try {
            id = db.insertOrThrow(tableName, null, contentValues);
            db.setTransactionSuccessful();
        } catch (SQLException e){
            e.printStackTrace();
            //do nothing
        }
        db.endTransaction();
        return id;
    }

    private void log(String msg){
        System.out.println("^^^AbstractRepository: " + msg);
    }
}
