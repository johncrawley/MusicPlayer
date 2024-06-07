package com.jacstuff.musicplayer.service.loader;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
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


    public Album getAlbum(String albumName){
        return albums.get(albumName);
    }


    public Map<String, Artist> getArtists(){
        return artists;
    }


    public Artist getArtist(String artistName){
        return artists.get(artistName);
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
        log("Entered addTracksData()");
        Cursor cursor = new CursorCreator().createCursor(preferencesHelper, context);
        log("Created cursor");
        existingAllTracksIdentifiers.clear();
        if(cursor != null){
            long startTime = System.currentTimeMillis();
            setupColumnMap(cursor);
            buildTracksFrom(cursor);
            initAllAlbumNames();
            logLoadingStats(cursor, startTime);
            cursor.close();
            return;
        }
        log("Cursor was null, no tracks found");
    }


    private void buildTracksFrom(Cursor cursor){
        while(cursor.moveToNext()){
            retrieveTrackDataFrom(cursor);
        }
    }


    private void retrieveTrackDataFrom(Cursor cursor){
        Track track = parseTrackFrom(cursor);
        addToTracks(track);
        addToAlbum(track);
        addAlbumToArtist(track);
        addToGenre(track);
    }


    private void logLoadingStats(Cursor cursor, long startTime){
        long duration = System.currentTimeMillis() - startTime;
        log("tracks loaded in " + duration + "ms number of total tracks: " + cursor.getCount());
    }


    private void log(String msg){
        System.out.println("^^^ TrackLoader: " + msg);
    }


    private void initAllAlbumNames(){
        allAlbumNames = new ArrayList<>(albums.keySet());
        Collections.sort(allAlbumNames);
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
        addToColumnMap(cursor, MediaStore.Audio.Media.RELATIVE_PATH);
    }


    private void addToColumnMap(Cursor cursor, String str){
        columnMap.put(str, cursor.getColumnIndexOrThrow(str));
    }


    private Track parseTrackFrom(Cursor cursor) {
        return new Track(
                getStr(cursor, MediaStore.Audio.Media.DATA),
                getStr(cursor, MediaStore.Audio.Media.TITLE),
                getStr(cursor, MediaStore.Audio.Media.ARTIST),
                getStr(cursor, MediaStore.Audio.Media.ALBUM),
                getDiscNumber(cursor),
                getGenre(cursor),
                getStr(cursor, MediaStore.Audio.Media.YEAR),
                getIntValueFrom(cursor, MediaStore.Audio.Media.DURATION),
                getTrackNumber(cursor),
                getBitrate(cursor));
    }


    private void addToTracks(Track track){
        if(shouldTrackBeAdded(track)){
            tracks.add(track);
            addToArtist(track, track.getArtist());
        }
    }


    private String getDiscNumber(Cursor cursor){
        return String.valueOf(getDiscNumberFrom(cursor));
    }


    boolean shouldTrackBeAdded(Track track){
        String identifier = track.getDuplicateIdentifier();
        if(preferencesHelper.areDuplicateTracksIgnored() && existingAllTracksIdentifiers.contains(identifier)){
            return false;
        }
        existingAllTracksIdentifiers.add(identifier);
        return true;
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


    private void addAlbumToArtist(Track track){
       Artist artist = artists.get(track.getArtist());
       if(artist == null){
           return;
       }
       artist.addAlbumName(track.getAlbum());
    }


    private void addToAlbum(Track track){
        String albumName = track.getAlbum();
        if(albums.containsKey(albumName)){
            Album savedAlbum= albums.get(albumName);
            if(savedAlbum !=null){
                savedAlbum.addTrack(track);
                savedAlbum.addArtist(track.getArtist());
                return;
            }
        }
        Album album = new Album(albumCount++, albumName);
        album.addTrack(track);
        albums.put(albumName, album);
    }


    private void addToGenre(Track track){
        String genreName = track.getGenre();
        if(genreName.trim().isEmpty()){
            return;
        }
        genres.computeIfAbsent(genreName, k -> new Genre(genreCount++, k)).addTrack(track);
    }


    private String getGenre(Cursor cursor){
        String genre = "";
        genre = getStr(cursor, MediaStore.Audio.Media.GENRE);
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
    private String getStr(Cursor cursor, String colName){
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


}