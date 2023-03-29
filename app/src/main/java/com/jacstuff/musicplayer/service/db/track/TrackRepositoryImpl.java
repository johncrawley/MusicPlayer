package com.jacstuff.musicplayer.service.db.track;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.provider.BaseColumns;

import com.jacstuff.musicplayer.service.db.AbstractRepository;
import com.jacstuff.musicplayer.service.db.DbContract;
import com.jacstuff.musicplayer.service.db.DbHelper;
import com.jacstuff.musicplayer.service.db.album.Album;
import com.jacstuff.musicplayer.service.db.album.AlbumRepository;
import com.jacstuff.musicplayer.service.db.artist.Artist;
import com.jacstuff.musicplayer.service.db.artist.ArtistRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackRepositoryImpl extends AbstractRepository implements TrackRepository {

    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final DbHelper dbHelper;
    private final Map<String, Long> artists;
    private final  Map<String, Long> albums;


    public TrackRepositoryImpl(Context context){
        super(context);
        artistRepository = new ArtistRepository(context);
        albumRepository = new AlbumRepository(context);
        dbHelper = DbHelper.getInstance(context);
        artists = new HashMap<>();
        albums = new HashMap<>();
    }


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
                + DbContract.TracksEntry.TABLE_NAME
                + " WHERE " + BaseColumns._ID
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
        String query = createGetQuery() + " WHERE " + DbContract.ArtistsEntry.COL_NAME + " = '" + artist.getName() + "';";
        return getTracksUsingQuery(query);
    }


    @Override
    public List<Track> getTracksForAlbum(Album album){
        String query = createGetQuery() + " WHERE " + DbContract.AlbumsEntry.COL_NAME + " = '" + album.getName() + "';";
        return getTracksUsingQuery(query);
    }


    private String createGetQuery(){
        return  "SELECT * FROM " + DbContract.TracksEntry.TABLE_NAME
                + " INNER JOIN " + DbContract.ArtistsEntry.TABLE_NAME
                + " ON " + DbContract.TracksEntry.TABLE_NAME + "." + DbContract.TracksEntry.COL_ARTIST_ID
                + " = "  + DbContract.ArtistsEntry.TABLE_NAME + "." + DbContract.ArtistsEntry._ID
                + " INNER JOIN " + DbContract.AlbumsEntry.TABLE_NAME
                + " ON " + DbContract.TracksEntry.TABLE_NAME + "." + DbContract.TracksEntry.COL_ALBUM_ID
                + " = " + DbContract.AlbumsEntry.TABLE_NAME + "." + DbContract.AlbumsEntry._ID;
    }


    @Override
    public List<Track> getAllTracksContaining(String prefix){
        String query = "SELECT * FROM " + DbContract.TracksEntry.TABLE_NAME
                + " WHERE " + DbContract.TracksEntry.COL_TITLE + beginsWith(prefix)
                + " OR " + DbContract.TracksEntry.COL_ARTIST + beginsWith(prefix)
                + " OR " + DbContract.TracksEntry.COL_ALBUM + beginsWith(prefix) + ";";

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
                .createTrackWithPathname(getString(DbContract.TracksEntry.COL_PATH))
                .withId(getLong(BaseColumns._ID))
                .withTitle(getString(DbContract.TracksEntry.COL_TITLE))
                .withTrackNumber(getLong(DbContract.TracksEntry.COL_TRACK_NUMBER))
                .withArtist(getString(DbContract.TracksEntry.COL_ARTIST))
                .withAlbum(getString(DbContract.TracksEntry.COL_ALBUM))
                .duration(getLong(DbContract.TracksEntry.COL_DURATION))
                .withGenre(DbContract.TracksEntry.COL_GENRE)
                .build();
    }


    private ContentValues createTrackContentValuesFor(Track track, long artistId, long albumId){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.TracksEntry.COL_TITLE, track.getTitle());
        contentValues.put(DbContract.TracksEntry.COL_ARTIST, track.getArtist());
        contentValues.put(DbContract.TracksEntry.COL_ALBUM, track.getAlbum());
        contentValues.put(DbContract.TracksEntry.COL_PATH, track.getPathname());
        contentValues.put(DbContract.TracksEntry.COL_ARTIST_ID, artistId);
        contentValues.put(DbContract.TracksEntry.COL_ALBUM_ID, albumId);
        contentValues.put(DbContract.TracksEntry.COL_DURATION, track.getDuration());
        contentValues.put(DbContract.TracksEntry.COL_GENRE, track.getGenre());
        contentValues.put(DbContract.TracksEntry.COL_TRACK_NUMBER, track.getTrackNumber());
        return contentValues;
    }


}
