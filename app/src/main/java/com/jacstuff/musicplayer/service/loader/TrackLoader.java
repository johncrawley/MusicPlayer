package com.jacstuff.musicplayer.service.loader;


import android.content.Context;
import android.database.Cursor;

import com.jacstuff.musicplayer.service.db.entities.Album;
import com.jacstuff.musicplayer.service.db.entities.Artist;
import com.jacstuff.musicplayer.service.db.entities.Genre;
import com.jacstuff.musicplayer.service.db.entities.PlaylistType;
import com.jacstuff.musicplayer.service.db.entities.Track;
import com.jacstuff.musicplayer.service.helpers.preferences.PreferencesHelperImpl;
import com.jacstuff.musicplayer.service.loader.store.AlbumStore;
import com.jacstuff.musicplayer.service.loader.store.ArtistStore;
import com.jacstuff.musicplayer.service.loader.store.GenreStore;
import com.jacstuff.musicplayer.service.loader.store.TrackStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class TrackLoader {

    private final Context context;
    private final TrackStore trackStore = new TrackStore();
    private final AlbumStore albumStore = new AlbumStore();
    private final ArtistStore artistStore = new ArtistStore();
    private final GenreStore genreStore = new GenreStore();
    private final StatLogger statLogger = new StatLogger();
    private final PreferencesHelperImpl preferencesHelper;
    private TrackParser trackParser;


    public TrackLoader(Context context){
        this.context  = context;
        preferencesHelper = new PreferencesHelperImpl(context);
        trackStore.setPreferencesHelper(preferencesHelper);
    }


    public List<Track> loadAudioFiles(){
        trackStore.init();
        albumStore.init();
        artistStore.init();
        genreStore.init();
        addTracksData();
        return trackStore.getTracks();
    }


    public Album getAlbum(String albumName){
        return albumStore.get(albumName);
    }


    public Map<String, Artist> getArtists(){
        return artistStore.getAll();
    }


    public Artist getArtist(String artistName){
        return artistStore.get(artistName);
    }


    public Genre getGenre(String genreName){
        return genreStore.get(genreName);
    }


    public List<Track> getAllTracksContaining(String searchTerm){
       return trackStore.getAllTracksContaining(searchTerm);
    }

    public ArrayList<String> getAllAlbumNames(){ return albumStore.getAllAlbumNames(); }


    public ArrayList<String> getMainArtistNames(){
       return artistStore.getMainArtistNames(context);
    }


    public ArrayList<String> getAllGenreNames(){
       return genreStore.getAllGenreNames();
    }


    public List<Track> getTracksFor(PlaylistType playlistType, String name){
       return switch (playlistType){
            case ALBUM -> getTracksForAlbum(name);
            case GENRE -> getGenre(name).getTracks();
            case ARTIST -> getTracksForArtist(name);
            case ALL_TRACKS -> trackStore.getTracks();
           default -> new ArrayList<>();
        };
    }


    public List<Track> getTracksForAlbum(String albumName){
        return albumStore.getTracksOf(albumName);
    }


    public List<Track> getTracksForArtist(String artistName){
        return artistStore.getTracksOf(artistName);
    }


    private void addTracksData(){
        Cursor cursor = new CursorCreator().createCursor(preferencesHelper, context);
        trackStore.clear();
        if(cursor != null){
            statLogger.start();
            trackParser = new TrackParser(cursor);
            buildTracksFrom(cursor);
            albumStore.initAllAlbumNames();
            statLogger.logLoadingStats(cursor);
            cursor.close();
        }
    }


    private void buildTracksFrom(Cursor cursor){
        while(cursor.moveToNext()){
            retrieveTrackDataFrom(cursor);
        }
    }


    private void retrieveTrackDataFrom(Cursor cursor){
        Track track = trackParser.parseTrackFrom(cursor);
        addToTracksAndArtist(track);
        albumStore.add(track);
        genreStore.add(track);
    }


    private void addToTracksAndArtist(Track track){
        boolean wasAdded = trackStore.add(track);
        if(wasAdded){
            artistStore.add(track);
        }
    }



}