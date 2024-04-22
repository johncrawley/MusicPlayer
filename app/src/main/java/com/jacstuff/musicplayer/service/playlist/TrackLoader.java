package com.jacstuff.musicplayer.service.playlist;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.preference.PreferenceManager;

import com.jacstuff.musicplayer.service.db.entities.Album;
import com.jacstuff.musicplayer.service.db.entities.Artist;
import com.jacstuff.musicplayer.service.db.entities.Genre;
import com.jacstuff.musicplayer.service.db.entities.Track;
import com.jacstuff.musicplayer.service.helpers.PreferencesHelper;

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
    private Map<String, Album> albums;
    private Map<String, Artist> artists;
    private Map<String, Genre> genres;
    private ArrayList<String> allAlbumNames = new ArrayList<>();
    private long artistCount;
    private int albumCount;
    private int genreCount;
    private Map<String, Integer> columnMap;
    private final PreferencesHelper preferencesHelper;
    private final Set<String> existingAllTracksIdentifiers = new HashSet<>();


    public TrackLoader(Context context){
        this.context  = context;
        albums = new HashMap<>();
        preferencesHelper = new PreferencesHelper(context);
    }


    public List<Track> loadAudioFiles(){
        tracks = new ArrayList<>(10_000);
        albums = new ConcurrentHashMap<>(5000);
        artists = new ConcurrentHashMap<>(500);
        genres = new ConcurrentHashMap<>(100);
        addTracksData();
        return tracks;
    }


    public Map<String, Album> getAlbums(){
        return albums;
    }


    public Album getAlbum(String albumName){
        return albums.get(albumName);
    }


    public Map<String, Artist> getArtists(){
        return artists;
    }


    public Artist getArtist(String artistName){
        return artists.get(artistName);
    }


    public Map<String, Genre> getGenres(){
        return genres;
    }


    public Genre getGenre(String genreName){
        return genres.get(genreName);
    }


    public List<Track> getAllTracksContaining(String searchTerm){
        return tracks.parallelStream()
                .filter(track -> track.getSearchString().contains(searchTerm))
                .collect(Collectors.toList());
    }


    public ArrayList<String> getAllAlbumNames(){
        return albums == null ? new ArrayList<>() : allAlbumNames;
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
        return new ArrayList<>(album.getTracks());
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


    private void addTracksData(){
        Cursor cursor = createCursorForFilesystemTracks();
        boolean areDuplicatesIgnored = preferencesHelper.areDuplicateTracksIgnored();;
        existingAllTracksIdentifiers.clear();
        if(cursor != null){
            long startTime = System.currentTimeMillis();
            setupColumnMap(cursor);
            while(cursor.moveToNext()){
                addTrack(cursor, areDuplicatesIgnored);
            }
            initAllAlbumNames();
            long duration = System.currentTimeMillis() - startTime;
            log("tracks loaded in " + duration + "ms number of total tracks: " + cursor.getCount());
            cursor.close();
        }
    }


    private void log(String msg){
        System.out.println("^^^ TrackLoader: " + msg);
    }


    private void initAllAlbumNames(){
        allAlbumNames = new ArrayList<>(albums.keySet());
        Collections.sort(allAlbumNames);
    }


    private Cursor createCursorForFilesystemTracks(){
        String[] projection = createProjection();
        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER + " ASC";
        Uri collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        return context.getContentResolver().query(collection, projection, null, null, sortOrder);
    }


    private String[] createProjection() {
        return new String[]{
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.CD_TRACK_NUMBER,
                MediaStore.Audio.Media.GENRE,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.BITRATE,
                MediaStore.Audio.Media.DISC_NUMBER
        };
    }


    private void setupColumnMap(Cursor cursor){
        columnMap = new HashMap<>();
        addToColumnMap(cursor, MediaStore.Audio.Media.DATA);
        addToColumnMap(cursor, MediaStore.Audio.Media.ARTIST);
        addToColumnMap(cursor, MediaStore.Audio.Media.ALBUM);
        addToColumnMap(cursor, MediaStore.Audio.Media.TITLE);
        addToColumnMap(cursor, MediaStore.Audio.Media.DURATION);
        addToColumnMap(cursor, MediaStore.Audio.Media.CD_TRACK_NUMBER);
        addToColumnMap(cursor, MediaStore.Audio.Media.GENRE);
        addToColumnMap(cursor, MediaStore.Audio.Media.YEAR);
        addToColumnMap(cursor, MediaStore.Audio.Media.BITRATE);
        addToColumnMap(cursor, MediaStore.Audio.Media.DISC_NUMBER);
    }


    private void addToColumnMap(Cursor cursor, String str){
        columnMap.put(str, cursor.getColumnIndexOrThrow(str));
    }


    private void addTrack(Cursor cursor, boolean areDuplicatesIgnored) {
        String path = getValueFrom(cursor, MediaStore.Audio.Media.DATA);
        if (!isContainingCorrectPath(path)) {
            return;
        }
        String album = getValueFrom(cursor, MediaStore.Audio.Media.ALBUM);
        String artist = getValueFrom(cursor, MediaStore.Audio.Media.ARTIST);
        String genre = getGenre(cursor);
        String bitrate = getBitrate(cursor);

        Track track = new Track(
                path,
                getValueFrom(cursor, MediaStore.Audio.Media.TITLE),
                artist,
                album,
                getDiscNumber(cursor),
                genre,
                getValueFrom(cursor, MediaStore.Audio.Media.YEAR),
                getIntValueFrom(cursor, MediaStore.Audio.Media.DURATION),
                getTrackNumber(cursor),
                bitrate);

        addToTracks(track, areDuplicatesIgnored);
        addToAlbum(track, album, artist);
        addAlbumToArtist(album, artist);
        addToGenre(track, genre);
    }


    private void addToTracks(Track track, boolean areDuplicatesIgnored){
        if(shouldTrackBeAdded(track, areDuplicatesIgnored)){
            tracks.add(track);
            addToArtist(track, track.getArtist());
        }
    }


    private String getDiscNumber(Cursor cursor){
        return String.valueOf(getDiscNumberFrom(cursor));
    }


    boolean shouldTrackBeAdded(Track track, boolean areDuplicatesIgnored){
        String identifier = track.getDuplicateIdentifier();
        if(!areDuplicatesIgnored || !existingAllTracksIdentifiers.contains(identifier)){
            existingAllTracksIdentifiers.add(identifier);
            return true;
        }
        return false;
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
        genre = getValueFrom(cursor, MediaStore.Audio.Media.GENRE);
        return genre == null ? "" : genre;
    }



    private String getBitrate(Cursor cursor){
        int bitrate = getIntValueFrom(cursor, MediaStore.Audio.Media.BITRATE);
        if(bitrate > 1000){
            int kbps = bitrate / 1000;
            return kbps + "kbps";
        }
        return String.valueOf(bitrate);
    }


    private int getTrackNumber(Cursor cursor){
        return getIntValueFrom(cursor, MediaStore.Audio.Media.CD_TRACK_NUMBER);
    }


    @SuppressWarnings("ConstantConditions")
    private String getValueFrom(Cursor cursor, String colName){
        return cursor.getString(columnMap.get(colName));
    }


    @SuppressWarnings("ConstantConditions")
    private int getIntValueFrom(Cursor cursor, String colName){
        return (int)cursor.getLong(columnMap.get(colName));
    }


    private long getDiscNumberFrom(Cursor cursor){
        Integer name = columnMap.get(MediaStore.Audio.Media.DISC_NUMBER);
        if(name == null){
            return -1;
        }
        return Math.max(1, cursor.getLong(name));
    }


    private boolean isContainingCorrectPath(String path){
        String expectedPath = getMusicPathname();
        return path.contains(expectedPath);
    }

}