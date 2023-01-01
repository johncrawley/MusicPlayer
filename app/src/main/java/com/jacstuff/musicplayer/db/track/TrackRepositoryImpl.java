package com.jacstuff.musicplayer.db.track;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;

import com.jacstuff.musicplayer.db.AbstractRepository;
import com.jacstuff.musicplayer.db.DbContract;
import com.jacstuff.musicplayer.db.DbContract.ArtistsEntry;
import com.jacstuff.musicplayer.db.artist.Artist;
import com.jacstuff.musicplayer.db.artist.ArtistRepository;

import static com.jacstuff.musicplayer.db.DbContract.TracksEntry.*;

import java.util.ArrayList;
import java.util.List;

public class TrackRepositoryImpl extends AbstractRepository implements TrackRepository {

    private ArtistRepository artistRepository;

    public TrackRepositoryImpl(Context context){
        super(context);
        artistRepository = new ArtistRepository(context);
    }


    @Override
    public void addTrack(Track track) {
        long artistId = artistRepository.addOrGetArtist(track.getArtist());
        addValuesToTable(db,
                DbContract.TracksEntry.TABLE_NAME,
                createTrackContentValuesFor(track, artistId));
    }


    @Override
    public void deleteTrack(Track track) {
        String deleteTrackQuery = "DELETE FROM "
                + TABLE_NAME
                + " WHERE " + _ID
                + " = "  + track.getId()
                + ";";
        try {
            db.execSQL(deleteTrackQuery);
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }


    @Override
    public List<Track> getAllTracks() {
        String query = createGetQuery() + ";";
        return  getTracksUsingQuery(query);
    }

    @Override
    public List<Track> getTracksForArtist(Artist artist){
        String query = createGetQuery() + " WHERE " + ArtistsEntry.COL_NAME + " = '" + artist.getName() + "';";
        return getTracksUsingQuery(query);
    }


    private String createGetQuery(){
        return  "SELECT * FROM " + TABLE_NAME
                + " INNER JOIN " + DbContract.ArtistsEntry.TABLE_NAME
                + " ON " + TABLE_NAME + "." + COL_ARTIST_ID
                + " = "
                + ArtistsEntry.TABLE_NAME + "." + ArtistsEntry._ID;
    }




    public List<Track> getAllTracksStartingWith(String prefix){
        String query = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + COL_TITLE + " GLOB '" + prefix + " *';";
        return getTracksUsingQuery( query);
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


    public List<Track> searchForTracks(String searchTerms){
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " +
                COL_ARTIST + " LIKE ?;";
        String[] selectionArgs = new String[] { "searchTerms"};
        return getTracks(query, selectionArgs);
    }


    private List<Track> getTracks(String query, String[] selectionArgs){
        List<Track> tracks = new ArrayList<>();
        try {
            cursor = db.rawQuery(query, selectionArgs);
            while(cursor.moveToNext()){
                tracks.add(createTrackFromCursor());
            }
        }
        catch(SQLException e){
            e.printStackTrace();
            return tracks;
        }
        cursor.close();
        return  tracks;
    }



    private Track createTrackFromCursor(){
        return new Track.Builder()
                .createTrackWithPathname(getString(COL_PATH))
                .withId(getLong(_ID))
                .withName(getString(COL_TITLE))
                .withTrackNumber(getLong(COL_TRACK_NUMBER))
                .withArtist(getString(COL_ARTIST))
                .withAlbum(getString(COL_ALBUM))
                .duration(getLong(COL_DURATION))
                .withGenre(COL_GENRE)
                .build();
    }


    private ContentValues createTrackContentValuesFor(Track track, long artistId){
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_TITLE, track.getName());
        contentValues.put(COL_ARTIST, track.getArtist());
        contentValues.put(COL_ALBUM, track.getAlbum());
        contentValues.put(COL_PATH, track.getPathname());
        contentValues.put(COL_ARTIST_ID, artistId);
        contentValues.put(COL_ALBUM, track.getAlbum());
        contentValues.put(COL_DURATION, track.getDuration());
        contentValues.put(COL_GENRE, track.getGenre());
        contentValues.put(COL_TRACK_NUMBER, track.getTrackNumber());
        return contentValues;
    }


}
