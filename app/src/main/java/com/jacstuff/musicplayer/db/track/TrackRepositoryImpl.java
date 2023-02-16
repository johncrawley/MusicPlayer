package com.jacstuff.musicplayer.db.track;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;

import com.jacstuff.musicplayer.db.AbstractRepository;
import com.jacstuff.musicplayer.db.DbContract;
import com.jacstuff.musicplayer.db.DbContract.ArtistsEntry;
import com.jacstuff.musicplayer.db.DbContract.AlbumsEntry;
import com.jacstuff.musicplayer.db.DbHelper;
import com.jacstuff.musicplayer.db.album.Album;
import com.jacstuff.musicplayer.db.album.AlbumRepository;
import com.jacstuff.musicplayer.db.artist.Artist;
import com.jacstuff.musicplayer.db.artist.ArtistRepository;

import static com.jacstuff.musicplayer.db.DbContract.TracksEntry.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TrackRepositoryImpl extends AbstractRepository implements TrackRepository {

    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final DbHelper dbHelper;

    public TrackRepositoryImpl(Context context){
        super(context);
        artistRepository = new ArtistRepository(context);
        albumRepository = new AlbumRepository(context);
        dbHelper = DbHelper.getInstance(context);
        artists = new HashMap<>();
        albums = new HashMap<>();
    }

    private Map<String, Long> artists;
    private Map<String, Long> albums;


    @Override
    public void addTrack(Track track) {


        long artistId = getArtistId(track);
        long albumId = getAlbumId(track);

        addValuesToTable(db,
                DbContract.TracksEntry.TABLE_NAME,
                createTrackContentValuesFor(track, artistId, albumId));
    }


    private long getArtistId(Track track){
        String artist = track.getArtist();
        Long id = artists.putIfAbsent(artist, artistRepository.addOrGetArtist(artist));
        if(id == null){
            id = -1L;
        }
        return id;
    }


    private long getAlbumId(Track track){
        String album = track.getAlbum();

        Long id = albums.putIfAbsent(album, albumRepository.addOrGet(album));
        if(id == null){
            id = -1L;
        }
        return id;
    }


    @Override
    public void recreateTracksTables(){
        dbHelper.dropAndRecreateTracksArtistsAndAlbumsTables(db);
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


    @Override
    public List<Track> getTracksForAlbum(Album album){
        String query = createGetQuery() + " WHERE " + DbContract.AlbumsEntry.COL_NAME + " = '" + album.getName() + "';";
        return getTracksUsingQuery(query);
    }


    private String createGetQuery(){
        return  "SELECT * FROM " + TABLE_NAME
                + " INNER JOIN " + ArtistsEntry.TABLE_NAME
                + " ON " + TABLE_NAME + "." + COL_ARTIST_ID
                + " = "  + ArtistsEntry.TABLE_NAME + "." + ArtistsEntry._ID
                + " INNER JOIN " + AlbumsEntry.TABLE_NAME
                + " ON " + TABLE_NAME + "." + COL_ALBUM_ID
                + " = " + AlbumsEntry.TABLE_NAME + "." + AlbumsEntry._ID;
    }


    @Override
    public List<Track> getAllTracksContaining(String prefix){
        String query = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + COL_TITLE + beginsWith(prefix)
                + " OR " + COL_ARTIST + beginsWith(prefix)
                + " OR " + COL_ALBUM + beginsWith(prefix) + ";";

        return getTracksUsingQuery(query);
    }


    private String beginsWith(String str){
        return " LIKE '%" + str + "%'";
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
                .withTitle(getString(COL_TITLE))
                .withTrackNumber(getLong(COL_TRACK_NUMBER))
                .withArtist(getString(COL_ARTIST))
                .withAlbum(getString(COL_ALBUM))
                .duration(getLong(COL_DURATION))
                .withGenre(COL_GENRE)
                .build();
    }


    private ContentValues createTrackContentValuesFor(Track track, long artistId, long albumId){
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_TITLE, track.getTitle());
        contentValues.put(COL_ARTIST, track.getArtist());
        contentValues.put(COL_ALBUM, track.getAlbum());
        contentValues.put(COL_PATH, track.getPathname());
        contentValues.put(COL_ARTIST_ID, artistId);
        contentValues.put(COL_ALBUM_ID, albumId);
        contentValues.put(COL_DURATION, track.getDuration());
        contentValues.put(COL_GENRE, track.getGenre());
        contentValues.put(COL_TRACK_NUMBER, track.getTrackNumber());
        return contentValues;
    }


}
