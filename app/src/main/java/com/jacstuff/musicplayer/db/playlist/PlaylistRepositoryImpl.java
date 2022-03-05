package com.jacstuff.musicplayer.db.playlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.jacstuff.musicplayer.Track;
import com.jacstuff.musicplayer.db.DbHelper;
import com.jacstuff.musicplayer.db.DbUtils;
import static com.jacstuff.musicplayer.db.DbContract.PlaylistItemsEntry;
import static com.jacstuff.musicplayer.db.DbContract.PlaylistEntry;
import static com.jacstuff.musicplayer.db.DbContract.TracksEntry;


import java.util.ArrayList;
import java.util.List;

public class PlaylistRepositoryImpl implements PlaylistRepository {

    private final SQLiteDatabase db;
    private Cursor cursor;


    public PlaylistRepositoryImpl(Context context){
        DbHelper dbHelper = DbHelper.getInstance(context);
        db = dbHelper.getWritableDatabase();
    }


    @Override
    public void createPlaylist(String name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PlaylistEntry.COL_NAME, name);
        DbUtils.addValuesToTable(db,
                PlaylistEntry.TABLE_NAME,
                contentValues);
    }


    @Override
    public List<Track> getAllTracksFromPlaylist(Long playlistId){
      List<Track> tracks = new ArrayList<>();

      String query3 = "SELECT * FROM " + TracksEntry.TABLE_NAME
              + " INNER JOIN " + PlaylistItemsEntry.TABLE_NAME
              + " ON " + TracksEntry._ID + " = " + PlaylistItemsEntry.COL_TRACK_ID
                + " WHERE "  + PlaylistItemsEntry.COL_PLAYLIST_ID + " = " + playlistId + ";";

        gatherData(query3, ()-> DbUtils.addTrackTo(tracks, cursor));

        return tracks;
    }


    private void gatherData(String query, Runnable runnable){
        try {
            cursor = db.rawQuery(query, null);
            while(cursor.moveToNext()){
                runnable.run();
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        finally {
            cursor.close();
        }
    }


    @Override
    public void deletePlaylist(Long playlistId) {
        String query = "DELETE FROM "
                + PlaylistEntry.TABLE_NAME
                + " WHERE "
                + PlaylistEntry._ID + " = "  + playlistId
                + ";";
        execSql(query);
    }


    @Override
    public void addTrackToPlaylist(Long playlistId, Long trackId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PlaylistItemsEntry.COL_TRACK_ID, trackId);
        contentValues.put(PlaylistItemsEntry.COL_PLAYLIST_ID, playlistId);

        DbUtils.addValuesToTable(db,
                PlaylistItemsEntry.TABLE_NAME,
                contentValues);
    }


    @Override
    public void removeTrackFromPlaylist(Long playlistId, Long trackId) {
        String query = "DELETE FROM "
                + PlaylistItemsEntry.TABLE_NAME
                + " WHERE "
                + PlaylistItemsEntry.COL_TRACK_ID + " = "  + trackId
                + " AND"
                + PlaylistItemsEntry.COL_PLAYLIST_ID + " = "  + playlistId
                + ";";
       execSql(query);
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
    public void renamePlaylist(Long playlistId, String updatedName) {

    }


    @Override
    public List<Playlist> getAllPlaylists() {
        List<Playlist> playlists = new ArrayList<>();
        String query = "SELECT * FROM " + PlaylistEntry.TABLE_NAME + ";";
        try {
            cursor = db.rawQuery(query, null);
            while(cursor.moveToNext()){
                long id = getLong(PlaylistEntry._ID);
                String name = getString(PlaylistEntry.COL_NAME);
                playlists.add(new Playlist(id, name));
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        finally {
            cursor.close();
        }
        return  playlists;
    }


    private String getString(String name){
        return cursor.getString(cursor.getColumnIndexOrThrow(name));
    }


    private Long getLong(String name){
        return cursor.getLong(cursor.getColumnIndexOrThrow(name));
    }


}
