package com.jacstuff.musicplayer.db.playlist;

import static android.provider.BaseColumns._ID;
import static com.jacstuff.musicplayer.db.DbContract.PlaylistItemsEntry.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;

import com.jacstuff.musicplayer.db.AbstractRepository;
import com.jacstuff.musicplayer.db.DbContract;
import com.jacstuff.musicplayer.db.track.Track;

import java.util.ArrayList;
import java.util.List;


public class PlaylistItemRepositoryImpl extends AbstractRepository implements PlaylistItemRepository {


    public PlaylistItemRepositoryImpl(Context context){
        super(context);
    }


    @Override
    public void addPlaylistItem(Track track, long playlistId) {
        addValuesToTable(db,
                DbContract.TracksEntry.TABLE_NAME,
                createTrackContentValuesFor(track, playlistId));
    }


    @Override
    public void deletePlaylistItem(long trackId) {
        String deleteTrackQuery = "DELETE FROM "
                + TABLE_NAME
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
                + TABLE_NAME
                + " WHERE " + COL_PLAYLIST_ID
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
    public List<Track> getTracksForPlaylistId(long playlistId){
        String query = "SELECT * FROM " + TABLE_NAME
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
                .createTrackWithPathname(getString(COL_PATH))
                .withId(getLong(_ID))
                .withName(getString(COL_TITLE))
                .withIndex(getLong(COL_ORDERING))
                .withTrackNumber(getLong(COL_TRACK_NUMBER))
                .withArtist(getString(COL_ARTIST))
                .withAlbum(getString(COL_ALBUM))
                .duration(getLong(COL_DURATION))
                .withGenre(COL_GENRE)
                .build();
    }


    private ContentValues createTrackContentValuesFor(Track track, long playlistId){
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_TITLE, track.getName());
        contentValues.put(COL_ARTIST, track.getArtist());
        contentValues.put(COL_ALBUM, track.getAlbum());
        contentValues.put(COL_ORDERING, track.getIndex());
        contentValues.put(COL_PLAYLIST_ID, playlistId);
        contentValues.put(COL_PATH, track.getPathname());
        contentValues.put(COL_ALBUM, track.getAlbum());
        contentValues.put(COL_DURATION, track.getDuration());
        contentValues.put(COL_GENRE, track.getGenre());
        contentValues.put(COL_TRACK_NUMBER, track.getTrackNumber());
        return contentValues;
    }


}