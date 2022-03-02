package com.jacstuff.musicplayer.db.playlist;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jacstuff.musicplayer.db.DbHelper;

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

    }

    @Override
    public void deletePlaylist(Integer playlistId) {

    }

    @Override
    public void addTrackToPlaylist(Integer playlistId, Integer trackId) {

    }

    @Override
    public void removeTrackFromPlaylist(Integer playlistId, Integer trackId) {

    }

    @Override
    public void renamePlaylist(Integer playlistId, String updatedName) {

    }

    @Override
    public List<Playlist> getAllRepositories() {
        return null;
    }
}
