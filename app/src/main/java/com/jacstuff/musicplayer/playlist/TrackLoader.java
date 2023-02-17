package com.jacstuff.musicplayer.playlist;


import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;


import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.db.album.Album;
import com.jacstuff.musicplayer.db.track.Track;
import com.jacstuff.musicplayer.db.track.TrackRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class TrackLoader {

    private final Context context;
    private final TrackRepository trackRepository;
    private List<Track> tracks;
    private Set<String> artists;
    private Map<String, Album> albums;

    public TrackLoader(Context context, TrackRepository trackRepository){
        this.context  = context;
        this.trackRepository = trackRepository;
        albums = new HashMap<>();
    }


    public List<Track> loadAudioFiles(){
        tracks = new ArrayList<>(10_000);
        albums = new HashMap<>(5000);
        artists = new HashSet<>(1000);
        rebuildTables();
        addTracksData();
        log("Load audio files, number of tracks: " + tracks.size());
        return tracks;
    }

    public Map<String, Album> getAlbums(){
        return albums;
    }


    public ArrayList<String> getAlbumNames(){
        if(albums == null){
            return new ArrayList<>();
        }
        return new ArrayList<>(albums.keySet());
    }

    public List<Track> getTracksForAlbum(String albumName){
        Album album =  albums.getOrDefault(albumName, new Album(-1, "null album!"));
        if(album == null){
            return Collections.emptyList();
        }
        return album.getTracks();
    }

    public Set<String> getArtists(){
        return artists;
    }


    private void log(String msg){
        System.out.println("^^^ AudioInfoLoader: " + msg);
    }


    private void addTracksData(){
        Cursor cursor = createCursorForFilesystemTracks();
        if(cursor != null){
            log("Starting addTracks() ~~~~~~~~~~~");
            long startTime = System.currentTimeMillis();
            setupColumnMap(cursor);
            while(cursor.moveToNext()){
                addTrack(cursor);
            }
            long duration = System.currentTimeMillis() - startTime;
            log("addTracksData() finished adding tracks, time taken: " + duration);
            cursor.close();
        }
    }

    private Map<String, Integer> columnMap;


    public void rebuildTables(){
        trackRepository.recreateTracksTables();
    }


    private Cursor createCursorForFilesystemTracks(){
        String[] projection = createProjection();
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER + " ASC";
        return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }


    private String[] createProjection() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return new String[]{
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.CD_TRACK_NUMBER,
                    MediaStore.Audio.Media.GENRE};
        }
        return new String[]{
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        };
    }


    private void setupColumnMap(Cursor cursor){
        columnMap = new HashMap<>();
        addToColumnMap(cursor, MediaStore.Audio.Media.DATA);
        addToColumnMap(cursor, MediaStore.Audio.Media.ARTIST);
        addToColumnMap(cursor, MediaStore.Audio.Media.ALBUM);
        addToColumnMap(cursor, MediaStore.Audio.Media.TITLE);
        addToColumnMap(cursor, MediaStore.Audio.Media.DURATION);
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            addToColumnMap(cursor, MediaStore.Audio.Media.CD_TRACK_NUMBER);
            addToColumnMap(cursor, MediaStore.Audio.Media.GENRE);
        }
    }


    private void addToColumnMap(Cursor cursor, String str){
        columnMap.put(str, cursor.getColumnIndexOrThrow(str));
    }

    private int albumCount;


    private void addTrack(Cursor cursor) {

        String path = getValueFrom(cursor, MediaStore.Audio.Media.DATA);
        if (!isContainingCorrectPath(path)) {
            return;
        }
        String genre = "";
        int trackNumber = -1;
        String albumName = getValueFrom(cursor, MediaStore.Audio.Media.ALBUM);


        String artist = getValueFrom(cursor, MediaStore.Audio.Media.ARTIST);
        artists.add(artist);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            genre = getValueFrom(cursor, MediaStore.Audio.Media.GENRE);
            trackNumber = getIntValueFrom(cursor, MediaStore.Audio.Media.CD_TRACK_NUMBER);
        }

        Track track = new Track(
                path,
                getValueFrom(cursor, MediaStore.Audio.Media.TITLE),
                artist,
                albumName,
                genre,
                getIntValueFrom(cursor, MediaStore.Audio.Media.DURATION),
                trackNumber
        );
        tracks.add(track);
        addToAlbum(track, albumName);
    }


    private void addToAlbum(Track track, String albumName){
        Album album = albums.putIfAbsent(albumName, new Album(albumCount++, albumName));
        if(album != null){
            album.addTrack(track);
        }
    }


    @SuppressWarnings("ConstantConditions")
    private String getValueFrom(Cursor cursor, String colName){
        return cursor.getString(columnMap.get(colName));
    }


    @SuppressWarnings("ConstantConditions")
    private int getIntValueFrom(Cursor cursor, String colName){
        return (int)cursor.getLong(columnMap.get(colName));
    }


    private boolean isContainingCorrectPath(String path){
        String expectedPath = context.getString(R.string.default_path);
        return path.contains(expectedPath);
    }

}