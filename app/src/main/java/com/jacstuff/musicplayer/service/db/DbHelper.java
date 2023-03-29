package com.jacstuff.musicplayer.service.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    private static DbHelper instance;


    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "MusicPlayer.db";

    private static final String OPENING_BRACKET = " (";
    private static final String CLOSING_BRACKET = " );";
    private static final String INTEGER = " INTEGER";
    private static final String TEXT = " TEXT";
    private static final String BLOB = " BLOB";
    private static final String COMMA = ",";
    public static final String UNIQUE = " UNIQUE ";
    private static final String PRIMARY_KEY = " PRIMARY KEY";
    private static final String CREATE_TABLE_IF_NOT_EXISTS = "CREATE TABLE IF NOT EXISTS ";


    private static final String SQL_CREATE_TRACKS_TABLE =
            CREATE_TABLE_IF_NOT_EXISTS
                    + DbContract.TracksEntry.TABLE_NAME
                    + OPENING_BRACKET
                    + DbContract.TracksEntry._ID + INTEGER + PRIMARY_KEY + COMMA
                    + DbContract.TracksEntry.COL_TITLE + TEXT + COMMA
                    + DbContract.TracksEntry.COL_PATH + TEXT + COMMA
                    + DbContract.TracksEntry.COL_ALBUM + TEXT + COMMA
                    + DbContract.TracksEntry.COL_ALBUM_ID + TEXT + COMMA
                    + DbContract.TracksEntry.COL_ARTIST + TEXT + COMMA
                    + DbContract.TracksEntry.COL_ARTIST_ID + INTEGER + COMMA
                    + DbContract.TracksEntry.COL_TRACK_NUMBER + INTEGER + COMMA
                    + DbContract.TracksEntry.COL_DURATION + INTEGER + COMMA
                    + DbContract.TracksEntry.COL_GENRE + TEXT
                    + CLOSING_BRACKET;


    private static final String SQL_CREATE_PLAYLIST_TABLE =
            CREATE_TABLE_IF_NOT_EXISTS
                    + DbContract.PlaylistEntry.TABLE_NAME
                    + OPENING_BRACKET
                    + DbContract.PlaylistEntry._ID + INTEGER + PRIMARY_KEY + COMMA
                    + DbContract.PlaylistEntry.COL_NAME + TEXT
                    + CLOSING_BRACKET;


    private static final String SQL_CREATE_ARTISTS_TABLE =
            CREATE_TABLE_IF_NOT_EXISTS
                    + DbContract.ArtistsEntry.TABLE_NAME
                    + OPENING_BRACKET
                    + DbContract.ArtistsEntry._ID + INTEGER + PRIMARY_KEY + COMMA
                    + DbContract.ArtistsEntry.COL_NAME + TEXT
                    + CLOSING_BRACKET;

    private static final String SQL_CREATE_ALBUMS_TABLE =
            CREATE_TABLE_IF_NOT_EXISTS
                    + DbContract.AlbumsEntry.TABLE_NAME
                    + OPENING_BRACKET
                    + DbContract.AlbumsEntry._ID + INTEGER + PRIMARY_KEY + COMMA
                    + DbContract.AlbumsEntry.COL_NAME + TEXT
                    + CLOSING_BRACKET;

    private static final String PLAYLISTS_TABLE_PRIMARY_KEY = DbContract.PlaylistEntry.TABLE_NAME
            + "."
            + DbContract.PlaylistEntry._ID;


    private static final String SQL_CREATE_PLAYLIST_ITEMS_TABLE =
            CREATE_TABLE_IF_NOT_EXISTS
                    + DbContract.PlaylistItemsEntry.TABLE_NAME
                    + OPENING_BRACKET
                    + DbContract.PlaylistItemsEntry._ID + INTEGER + PRIMARY_KEY + COMMA
                    + DbContract.PlaylistItemsEntry.COL_PLAYLIST_ID + INTEGER
                        + " REFERENCES " + DbContract.PlaylistEntry.TABLE_NAME + "(" + DbContract.PlaylistEntry._ID + ") ON DELETE CASCADE" + COMMA
                    + DbContract.PlaylistItemsEntry.COL_INDEX + INTEGER + COMMA
                    + DbContract.PlaylistItemsEntry.COL_PATH + TEXT + COMMA
                    + DbContract.PlaylistItemsEntry.COL_TITLE + TEXT + COMMA
                    + DbContract.PlaylistItemsEntry.COL_ALBUM + TEXT + COMMA
                    + DbContract.PlaylistItemsEntry.COL_ALBUM_ID + INTEGER + COMMA
                    + DbContract.PlaylistItemsEntry.COL_ARTIST + TEXT + COMMA
                    + DbContract.PlaylistItemsEntry.COL_ARTIST_ID + INTEGER + COMMA
                    + DbContract.PlaylistItemsEntry.COL_TRACK_NUMBER + INTEGER + COMMA
                    + DbContract.PlaylistItemsEntry.COL_GENRE + TEXT + COMMA
                    + DbContract.PlaylistItemsEntry.COL_DURATION + INTEGER
                    + CLOSING_BRACKET;


/*
   + " CONSTRAINT fk_playlists "
                    + "     FOREIGN KEY (" + PlaylistItemsEntry.COL_PLAYLIST_ID + ")"
                    + "     REFERENCES " + PlaylistEntry.TABLE_NAME + "(" + PlaylistEntry._ID + ")"
                    + "     ON DELETE CASCADE "
 */

    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DbHelper(context);
        }
        return instance;
    }


    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TRACKS_TABLE);
        db.execSQL(SQL_CREATE_PLAYLIST_TABLE);
        db.execSQL(SQL_CREATE_PLAYLIST_ITEMS_TABLE);
        db.execSQL(SQL_CREATE_ARTISTS_TABLE);
        db.execSQL(SQL_CREATE_ALBUMS_TABLE);
    }


    public void dropAndRecreateTracksArtistsAndAlbumsTables(SQLiteDatabase db){
        dropTable(DbContract.TracksEntry.TABLE_NAME, db);
        dropTable(DbContract.ArtistsEntry.TABLE_NAME, db);
        dropTable(DbContract.AlbumsEntry.TABLE_NAME, db);
        db.execSQL(SQL_CREATE_TRACKS_TABLE);
        db.execSQL(SQL_CREATE_ARTISTS_TABLE);
        db.execSQL(SQL_CREATE_ALBUMS_TABLE);
    }


    private void dropTable(String tableName, SQLiteDatabase db){
        db.execSQL("DROP TABLE "  + tableName + ";");
    }


    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }


    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}