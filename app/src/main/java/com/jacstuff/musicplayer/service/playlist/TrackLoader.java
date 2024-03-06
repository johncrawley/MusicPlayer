package com.jacstuff.musicplayer.service.playlist;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;


import androidx.preference.PreferenceManager;

import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.db.album.Album;
import com.jacstuff.musicplayer.service.db.artist.Artist;
import com.jacstuff.musicplayer.service.db.genre.Genre;
import com.jacstuff.musicplayer.service.db.track.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class TrackLoader {

    private final Context context;
    private List<Track> tracks;
    private Set<String> artistsSet;
    private Map<String, Album> albums;
    private Map<String, Artist> artists;
    private Map<String, Genre> genres;
    private long artistCount;
    private int albumCount;
    private int genreCount;
    private Map<String, Integer> columnMap;


    public TrackLoader(Context context){
        this.context  = context;
        albums = new HashMap<>();
    }


    public List<Track> loadAudioFiles(){
        tracks = new ArrayList<>(10_000);
        albums = new ConcurrentHashMap<>(5000);
        artists = new ConcurrentHashMap<>(500);
        genres = new ConcurrentHashMap<>(100);
        artistsSet = new HashSet<>(1000);
        log("Entered loadAudioFiles()");
        addTracksData();
        return tracks;
    }


    public Map<String, Album> getAlbums(){
        return albums;
    }


    public Map<String, Artist> getArtists(){
        return artists;
    }


    public Map<String, Genre> getGenres(){
        return genres;
    }


    public List<Track> getAllTracksContaining(String searchTerm){
        return tracks.parallelStream()
                .filter(track -> track.getSearchString().contains(searchTerm))
                .collect(Collectors.toList());
    }


    public ArrayList<String> getAllAlbumNames(){
        if(albums == null){
            return new ArrayList<>();
        }
        ArrayList<String> names = new ArrayList<>(albums.keySet());
        Collections.sort(names);
        return names;
    }


    public ArrayList<String> getMainArtistNames(){
        if(artists == null){
            return new ArrayList<>();
        }
        ArrayList<String> names = new ArrayList<>();
        int minNumberOfTracks = getMinimumNumberOfTracksForMainArtist();
        for(String key : artists.keySet()){
            Artist artist = artists.get(key);
            if(artist != null && artist.getTracks().size() > minNumberOfTracks){
                names.add(key);
            }
        }
        Collections.sort(names);
        return new ArrayList<>(names);
    }


    public ArrayList<String> getAllGenreNames(){
        if(albums == null){
            return new ArrayList<>();
        }
        ArrayList<String> names = new ArrayList<>(genres.keySet());
        Collections.sort(names);
        return names;
    }


    private int getMinimumNumberOfTracksForMainArtist(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(prefs.getString("minimumNumberOfTracksForMainArtist", "1"));
    }


    private String getMusicPathname(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("tracksPathnameString", "/Music");
    }


    public List<Track> getTracksForAlbum(String albumName){
        Album album =  albums.getOrDefault(albumName, new Album(-1, "null album!"));
        if(album == null){
            return Collections.emptyList();
        }
        return album.getTracks();
    }


    public List<Track> getTracksForArtist(String artistName){
        if(artists == null){
            artists = new HashMap<>(500);
        }
        Artist artist =  artists.getOrDefault(artistName, new Artist(-1, "null album!"));
        if(artist == null){
            return Collections.emptyList();
        }
        return artist.getTracks();
    }


    public Set<String> getArtistsSet(){
        return artistsSet;
    }


    private void log(String msg){
        System.out.println("^^^ AudioInfoLoader: " + msg);
    }


    private void addTracksData(){
        Cursor cursor = createCursorForFilesystemTracks();
        if(cursor != null){
            long startTime = System.currentTimeMillis();
            setupColumnMap(cursor);
            while(cursor.moveToNext()){
                addTrack(cursor);
            }
            long duration = System.currentTimeMillis() - startTime;
            log("tracks loaded in " + duration + "ms");
            cursor.close();
        }
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


    private void addTrack(Cursor cursor) {
        String path = getValueFrom(cursor, MediaStore.Audio.Media.DATA);
        if (!isContainingCorrectPath(path)) {
            return;
        }
        String albumName = getValueFrom(cursor, MediaStore.Audio.Media.ALBUM);
        String artistName = getValueFrom(cursor, MediaStore.Audio.Media.ARTIST);
        String genreName = getGenre(cursor);
        Track track = new Track(
                path,
                getValueFrom(cursor, MediaStore.Audio.Media.TITLE),
                artistName,
                albumName,
                genreName,
                getIntValueFrom(cursor, MediaStore.Audio.Media.DURATION),
                getTrackNumber(cursor)
        );
        tracks.add(track);
        addToAlbum(track, albumName, artistName);
        addToArtist(track, artistName);
        addAlbumToArtist(albumName, artistName);
        addToGenre(track, genreName);
    }


    private void addToArtist(Track track, String artistName){
        if(artists.containsKey(artistName)){
            Artist savedArtist = artists.get(artistName);
            if(savedArtist !=null){
                savedArtist.addTrack(track);
                return;
            }
        }
        Artist artist = new Artist(artistCount++, artistName);
        artist.addTrack(track);
        artists.put(artistName, artist);
    }


    private void addAlbumToArtist(String albumName, String artistName){
       Artist artist = artists.get(artistName);
       if(artist == null){
           return;
       }
       artist.addAlbumName(albumName);
    }


    private void addToAlbum(Track track, String albumName, String artistName){
        if(albums.containsKey(albumName)){
            Album savedAlbum= albums.get(albumName);
            if(savedAlbum !=null){
                savedAlbum.addTrack(track);
                savedAlbum.addArtist(artistName);
                return;
            }
        }
        Album album = new Album(albumCount++, albumName);
        album.addTrack(track);
        albums.put(albumName, album);
    }


    private void addToGenre(Track track, String genreName){
        if(genreName.trim().isEmpty()){
            return;
        }
        genres.computeIfAbsent(genreName, k -> new Genre(genreCount++, k)).addTrack(track);
    }


    private String getGenre(Cursor cursor){
        String genre = "";
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            genre = getValueFrom(cursor, MediaStore.Audio.Media.GENRE);
        }
        return genre == null ? "" : genre;
    }


    private int getTrackNumber(Cursor cursor){
        int trackNumber = 1000;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            trackNumber = getIntValueFrom(cursor, MediaStore.Audio.Media.CD_TRACK_NUMBER);
        }
        return trackNumber;
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
        String expectedPath = getMusicPathname();
        return path.contains(expectedPath);
    }

}