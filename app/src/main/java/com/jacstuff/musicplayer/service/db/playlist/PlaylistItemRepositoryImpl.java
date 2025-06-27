package com.jacstuff.musicplayer.service.db.playlist;

import static android.provider.BaseColumns._ID;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.jacstuff.musicplayer.service.db.AbstractRepository;
import com.jacstuff.musicplayer.service.db.DbContract;
import com.jacstuff.musicplayer.service.db.entities.Track;

import java.util.ArrayList;
import java.util.List;


public class PlaylistItemRepositoryImpl extends AbstractRepository implements PlaylistItemRepository {


    public PlaylistItemRepositoryImpl(Context context){
        super(context);
    }


    @Override
    public boolean addPlaylistItem(Track track, long playlistId) {

       return -1 != addValuesToTable(db,
                DbContract.PlaylistItemsEntry.TABLE_NAME,
                createTrackContentValuesFor(track, playlistId));
    }


    @Override
    public boolean isTrackAlreadyInPlaylist(Track track, long playlistId) {
        String sql = "SELECT EXISTS (SELECT NULL FROM " + DbContract.PlaylistItemsEntry.TABLE_NAME
                + " WHERE " + DbContract.PlaylistItemsEntry.COL_PATH + " = " + inQuotes(track.getPathname())
                + " AND  " + DbContract.PlaylistItemsEntry.COL_PLAYLIST_ID + " = "  + playlistId
                +  " LIMIT 1);";

        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        int result = cursor.getInt(0);
        cursor.close();
        return result == 1;
    }


    public String inQuotes(String str){
        return "'" + str + "'";
    }


    @Override
    public void deletePlaylistItem(long trackId) {
        String deleteTrackQuery = "DELETE FROM "
                + DbContract.PlaylistItemsEntry.TABLE_NAME
                + " WHERE " + _ID
                + " = "  + trackId
                + ";";
        try {
            db.execSQL(deleteTrackQuery);
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }


    @Override
    public void deleteAllPlaylistItems(long playlistId) {
        String deleteTrackQuery = "DELETE FROM "
                + DbContract.PlaylistItemsEntry.TABLE_NAME
                + " WHERE " + DbContract.PlaylistItemsEntry.COL_PLAYLIST_ID
                + " = "  + playlistId
                + ";";
        try {
            db.execSQL(deleteTrackQuery);
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }


    @Override
    public int getNumberOfTracksOf(long playlistId){
        String query = "SELECT COUNT (*) FROM "
                + DbContract.PlaylistItemsEntry.TABLE_NAME
                + " WHERE " + DbContract.PlaylistItemsEntry.COL_PLAYLIST_ID
                + " = "  + playlistId
                + ";";

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int result = cursor.getInt(0);
        cursor.close();
        System.out.println("^^^ PlaylistItemRepositoryImpl: getNumberOfTracksInPlaylist() return value : " + result);
        return result;
    }


    @Override
    public List<Track> getTracksForPlaylistId(long playlistId){
        String query = "SELECT * FROM " + DbContract.PlaylistItemsEntry.TABLE_NAME
                + " WHERE " + DbContract.PlaylistItemsEntry.COL_PLAYLIST_ID
                + " = " +  playlistId
                + ";";
        return getTracksUsingQuery(query);
    }


    private List<Track> getTracksUsingQuery(String query){
        List<Track> tracks = new ArrayList<>();
        try {
            cursor = db.rawQuery(query, null);
            tracks = getTracksFromCursor();
        }
        catch(SQLException e){
            e.printStackTrace();
            return tracks;
        }
        cursor.close();
        return  tracks;
    }


    private List<Track> getTracksFromCursor(){
        List<Track> tracks = new ArrayList<>();
        while(cursor.moveToNext()){
            tracks.add(createTrackFromCursor());
        }
        return tracks;
    }


    private Track createTrackFromCursor(){
        return new Track.Builder()
                .createTrackWithPathname(getString(DbContract.PlaylistItemsEntry.COL_PATH))
                .withId(getLong(_ID))
                .withTitle(getString(DbContract.PlaylistItemsEntry.COL_TITLE))
                .withIndex(getLong(DbContract.PlaylistItemsEntry.COL_INDEX))
                .withTrackNumber(getLong(DbContract.PlaylistItemsEntry.COL_TRACK_NUMBER))
                .withArtist(getString(DbContract.PlaylistItemsEntry.COL_ARTIST))
                .withAlbum(getString(DbContract.PlaylistItemsEntry.COL_ALBUM))
                .duration(getLong(DbContract.PlaylistItemsEntry.COL_DURATION))
                .withGenre(getString(DbContract.PlaylistItemsEntry.COL_GENRE))
                .withYear(getString(DbContract.PlaylistItemsEntry.COL_YEAR))
                .withDisc(getString(DbContract.PlaylistItemsEntry.COL_DISC))
                .withBitrate(getString(DbContract.PlaylistItemsEntry.COL_BITRATE))
                .build();
    }


    private ContentValues createTrackContentValuesFor(Track track, long playlistId){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.PlaylistItemsEntry.COL_TITLE, track.getTitle());
        contentValues.put(DbContract.PlaylistItemsEntry.COL_ARTIST, track.getArtist());
        contentValues.put(DbContract.PlaylistItemsEntry.COL_ALBUM, track.getAlbum());
        contentValues.put(DbContract.PlaylistItemsEntry.COL_INDEX, track.getIndex());
        contentValues.put(DbContract.PlaylistItemsEntry.COL_PLAYLIST_ID, playlistId);
        contentValues.put(DbContract.PlaylistItemsEntry.COL_PATH, track.getPathname());
        contentValues.put(DbContract.PlaylistItemsEntry.COL_ALBUM, track.getAlbum());
        contentValues.put(DbContract.PlaylistItemsEntry.COL_DURATION, track.getDuration());
        contentValues.put(DbContract.PlaylistItemsEntry.COL_GENRE, track.getGenre());
        contentValues.put(DbContract.PlaylistItemsEntry.COL_YEAR, track.getYear());
        contentValues.put(DbContract.PlaylistItemsEntry.COL_BITRATE, track.getBitrate());
        contentValues.put(DbContract.PlaylistItemsEntry.COL_DISC, track.getDisc());
        contentValues.put(DbContract.PlaylistItemsEntry.COL_TRACK_NUMBER, track.getTrackNumber());
        return contentValues;
    }


}