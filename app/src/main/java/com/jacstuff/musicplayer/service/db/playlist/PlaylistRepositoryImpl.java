package com.jacstuff.musicplayer.service.db.playlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.jacstuff.musicplayer.service.db.DbHelper;
import com.jacstuff.musicplayer.service.db.DbUtils;
import com.jacstuff.musicplayer.service.db.DbContract;
import com.jacstuff.musicplayer.service.db.entities.Playlist;
import com.jacstuff.musicplayer.service.db.entities.PlaylistType;


import java.util.ArrayList;
import java.util.List;

public class PlaylistRepositoryImpl implements PlaylistRepository {

    private final SQLiteDatabase db;
    private final PlaylistItemRepository playlistItemRepository;
    public static String ALL_TRACKS_PLAYLIST = "All Tracks";
    private final Playlist allTracksPlaylist;


    public PlaylistRepositoryImpl(Context context){
        DbHelper dbHelper = DbHelper.getInstance(context);
        db = dbHelper.getWritableDatabase();
        playlistItemRepository = new PlaylistItemRepositoryImpl(context);
        allTracksPlaylist = new Playlist(ALL_TRACKS_PLAYLIST, PlaylistType.ALL_TRACKS);
    }


    @Override
    public void createPlaylist(String name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.PlaylistEntry.COL_NAME, name);
        DbUtils.addValuesToTable(db,
                DbContract.PlaylistEntry.TABLE_NAME,
                contentValues);
    }


    @Override
    public void deletePlaylist(Long playlistId) {
        String query = "DELETE FROM "
                + DbContract.PlaylistEntry.TABLE_NAME
                + " WHERE "
                + DbContract.PlaylistEntry._ID + " = "  + playlistId
                + ";";
        execSql(query);
        playlistItemRepository.deleteAllPlaylistItems(playlistId);
    }

    @Override
    public Playlist getAllTracksPlaylist(){
        return allTracksPlaylist;
    }


    private void execSql(String query){
        try {
            db.execSQL(query);
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }


    @Override
    public List<Playlist> getAllPlaylists() {
        List<Playlist> playlists = new ArrayList<>();
        playlists.add(allTracksPlaylist);
        playlists.addAll(getPlaylistsFromDB());
        return  playlists;
    }


    @Override
    public List<Playlist> getAllUserPlaylists() {
        return getPlaylistsFromDB();
    }


    public List<Playlist> getPlaylistsFromDB(){
        List<Playlist> playlists = new ArrayList<>();
        String query = "SELECT * FROM " + DbContract.PlaylistEntry.TABLE_NAME + ";";
        try(Cursor cursor = db.rawQuery(query, null)){
            while(cursor.moveToNext()){
                long id = getLong(cursor, DbContract.PlaylistEntry._ID);
                String name = getString(cursor, DbContract.PlaylistEntry.COL_NAME);
                playlists.add(new Playlist(id, name, true));
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return  playlists;
    }


    private String getString(Cursor cursor, String name){
        return cursor.getString(cursor.getColumnIndexOrThrow(name));
    }


    private Long getLong(Cursor cursor, String name){
        return cursor.getLong(cursor.getColumnIndexOrThrow(name));
    }


}
