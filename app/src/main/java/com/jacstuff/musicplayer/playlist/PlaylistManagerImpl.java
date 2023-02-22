package com.jacstuff.musicplayer.playlist;

import android.content.Context;

import com.jacstuff.musicplayer.db.album.Album;
import com.jacstuff.musicplayer.db.artist.Artist;
import com.jacstuff.musicplayer.db.playlist.Playlist;
import com.jacstuff.musicplayer.db.playlist.PlaylistItemRepository;
import com.jacstuff.musicplayer.db.playlist.PlaylistItemRepositoryImpl;
import com.jacstuff.musicplayer.db.track.Track;
import com.jacstuff.musicplayer.service.MediaPlayerService;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;


public class PlaylistManagerImpl implements PlaylistManager {

    private int currentIndex = 0;
    private final TrackLoader trackLoader;
    private final Random random;
    private final PlaylistItemRepository playlistItemRepository;
    private List<Track> tracks;
    private List<Track> unPlayedTracks;
    private List<Track> allTracks;
    private final TrackHistory trackHistory;
    private boolean isShuffleEnabled = true;
    public static String ALL_TRACKS_PLAYLIST = "All Tracks";
    public static long ALL_TRACKS_PLAYLIST_ID = -10L;
    public static long SOME_ALBUM_PLAYLIST_ID = -20L;
    public static long SOME_ARTIST_PLAYLIST_ID = -30L;
    private Playlist someArtistPlaylist, someAlbumPlaylist, allTracksPlaylist;
    private Set<Long> defaultPlaylistIds;
    private Playlist currentPlaylist;
    private boolean isInitialized;
    private final ArrayDeque<Track> queuedTracks;


    public PlaylistManagerImpl(Context context, TrackLoader trackLoader){
        playlistItemRepository = new PlaylistItemRepositoryImpl(context);
        tracks = new ArrayList<>(10_000);
        allTracks = new ArrayList<>(10_000);
        unPlayedTracks = new ArrayList<>();
        random = new Random(System.currentTimeMillis());
        this.trackLoader = trackLoader;
        initTrackList();
        setupDefaultPlaylists();
        trackHistory = new TrackHistory();
        queuedTracks = new ArrayDeque<>();
    }


    @Override
    public void onDestroy(){
    }


    public void enableShuffle(){
        isShuffleEnabled = true;
        unPlayedTracks = new ArrayList<>(tracks);
    }


    public boolean hasAnyTracks(){
        return tracks != null && tracks.size() > 0;
    }


    public void disableShuffle(){
        isShuffleEnabled = false;
    }


    @Override
    public void addTracksFromStorage(MediaPlayerService mediaPlayerService){
        allTracks = getSortedTracks(trackLoader.loadAudioFiles());
        initTrackList();
        calculateAndDisplayNewTracksStats(mediaPlayerService);
        loadAllTracksIfNoPlaylistLoaded();
    }


    private void log(String msg){
        System.out.println("^^^ PlaylistManagerImpl: " + msg);
    }


    private void loadAllTracksIfNoPlaylistLoaded(){
        if(currentPlaylist == null){
            loadAllTracksPlaylist();
        }
    }


    @Override
    public Set<String> getArtists(){
        return trackLoader.getArtistsSet();
    }


    @Override
    public Map<String, Album> getAlbums(){
        return trackLoader.getAlbums();
    }


    public ArrayList<String> getAlbumNames(){
        return trackLoader.getAlbumNames();
    }


    public ArrayList<String> getArtistNames(){
        return trackLoader.getArtistNames();
    }


    @Override
    public void deleteAll(){
        trackLoader.rebuildTables();
        initTrackList();
    }


    @Override
    public boolean hasTracksQueued(){
        return !queuedTracks.isEmpty();
    }


    @Override
    public boolean isUserPlaylistLoaded(){
        return currentPlaylist != null && !defaultPlaylistIds.contains(currentPlaylist.getId());
    }


    private void calculateAndDisplayNewTracksStats(MediaPlayerService mediaPlayerService){
        mediaPlayerService.displayPlaylistRefreshedMessage(0);
    }


    public void init(){
        initTrackList();
    }


    private void initTrackList(){
       // tracks = getSortedTracks(trackRepository.getAllTracks());
        tracks = allTracks;
        assignIndexesToTracks();
        isInitialized = true;
    }


    private void setupDefaultPlaylists(){
        defaultPlaylistIds = new HashSet<>();
        defaultPlaylistIds.add(SOME_ALBUM_PLAYLIST_ID);
        defaultPlaylistIds.add(SOME_ARTIST_PLAYLIST_ID);
        defaultPlaylistIds.add(ALL_TRACKS_PLAYLIST_ID);

        someAlbumPlaylist = new Playlist(SOME_ALBUM_PLAYLIST_ID, "Some Album");
        someArtistPlaylist = new Playlist(SOME_ARTIST_PLAYLIST_ID, "Some Artist");
        allTracksPlaylist = new Playlist(ALL_TRACKS_PLAYLIST_ID, ALL_TRACKS_PLAYLIST);
        currentPlaylist = allTracksPlaylist;
    }


    @Override
    public boolean hasBeenInitialized(){
        return isInitialized;
    }


    public void loadPlaylist(Playlist playlist){
        if(isAlreadyCurrentPlaylist(playlist)){
            return;
        }
        this.currentPlaylist = playlist;

        if(playlist.getId() == ALL_TRACKS_PLAYLIST_ID){
            loadAllTracksPlaylist();
        }
        else{
            tracks = playlistItemRepository.getTracksForPlaylistId(playlist.getId());
            assignIndexesToTracks();
        }
        setupUnPlayedIndexes();
        trackHistory.reset();
        currentIndex = -1;
    }


    private boolean isAlreadyCurrentPlaylist(Playlist playlist){
       return currentPlaylist != null && (Objects.equals(this.currentPlaylist.getId(), playlist.getId()));
    }


    @Override
    public void addTrackToCurrentPlaylist(Track track) {
        if(!isUserPlaylistLoaded()){
              return;
        }
        tracks.add(track);
        unPlayedTracks.add(track);
        track.setIndex(tracks.size()-1);
        playlistItemRepository.addPlaylistItem(track, currentPlaylist.getId());
    }


    @Override
    public void addTracksToCurrentPlaylist(List<Track> additionalTracks) {
        if(!isUserPlaylistLoaded()){
            return;
        }
        tracks.addAll(additionalTracks);
        unPlayedTracks.addAll(additionalTracks);
        assignIndexesToTracks();
        for(Track track : additionalTracks){
            playlistItemRepository.addPlaylistItem(track, currentPlaylist.getId());
        }
    }


    @Override
    public void removeTrackFromCurrentPlaylist(Track track) {
        if(!isUserPlaylistLoaded()){
            return;
        }
        tracks.remove(track.getIndex());
        unPlayedTracks.remove(track);
        assignIndexesToTracks();
        playlistItemRepository.deletePlaylistItem(track.getId());
    }


    @Override
    public void loadAllTracksPlaylist(){
        if(allTracks == null){
            initTrackList();
        }
        setupUnPlayedIndexes();
        trackHistory.reset();
        currentPlaylist = allTracksPlaylist;
        tracks = new ArrayList<>(allTracks);
    }



    private List<Track> getSortedTracks(List<Track> list){
        log("Entered getSortedTracks()");
        if(list == null){
            log("getSortedTracks() list is null, returning empty list");
            return Collections.emptyList();
        }
        return list.stream().sorted(Comparator.comparing(Track::getOrderedString)).collect(Collectors.toList());
    }


    public void loadTracksFromArtist(String artistName){
        log("Entered loadTracksFromArtist() name: " + artistName);
        Map <String, Artist> artists = trackLoader.getArtists();
        if(artists == null){
            return;
        }
        Artist savedArtist = artists.get(artistName);
        if(savedArtist == null){
            log("saved artist is null!");
            return;
        }

        tracks = getSortedTracks(trackLoader.getTracksForArtist(artistName));
        log("artist has " + tracks.size() + " number of tracks");
        assignIndexesToTracks();
        setupQueue();
        currentPlaylist = someArtistPlaylist;
    }


    @Override
    public void loadTracksFromAlbum(String albumName) {
        Map <String, Album> albums = trackLoader.getAlbums();
        if(albums == null){
            return;
        }
        Album savedAlbum = albums.get(albumName);
        if(savedAlbum == null){
            return;
        }
        tracks = getSortedTracks(savedAlbum.getAllTracks());
        assignIndexesToTracks();
        setupQueue();
        currentPlaylist = someAlbumPlaylist;
    }


    @Override
    public void addTracksFromArtistToCurrentPlaylist(String artistName) {
        List<Track> tracks = trackLoader.getTracksForArtist(artistName);
        addTracksToCurrentPlaylist(getSortedTracks(tracks));
    }


    @Override
    public void addTracksFromAlbumToCurrentPlaylist(String albumName) {
        List<Track> tracks = trackLoader.getTracksForAlbum(albumName);
        addTracksToCurrentPlaylist(getSortedTracks(tracks));
    }


    private void setupQueue(){
        setupUnPlayedIndexes();
        trackHistory.reset();
        currentIndex = -1;
    }


    private void assignIndexesToTracks(){
        for(int i = 0; i< tracks.size(); i++){
            tracks.get(i).setIndex(i);
        }
    }


    private void setupUnPlayedIndexes(){
        unPlayedTracks = new ArrayList<>(tracks);
    }


    public String getTrackNameAt(int position){
        return position >= tracks.size() ? "" : tracks.get(position).getTitle();
    }


    public int getNumberOfTracks(){
        return tracks.size();
    }


    @Override
    public Track getNextTrack(){
        if(!queuedTracks.isEmpty()){
            return queuedTracks.removeLast();
        }
        return isShuffleEnabled ? getNextRandomUnPlayedTrack() : getNextTrackOnList();
    }


    @Override
    public boolean isShuffleEnabled(){
        return isShuffleEnabled;
    }


    @Override
    public void addTrackToQueue(Track track){
        queuedTracks.push(track);
    }


    @Override
    public Track getPreviousTrack(){
        return isShuffleEnabled ? getPreviousTrackFromHistory() : getPreviousTrackOnList();
    }


    private Track getPreviousTrackFromHistory(){
        Track track = trackHistory.getPreviousTrack();
        currentIndex = track.getIndex();
        return track;
    }


    @Override
    public int getCurrentTrackIndex(){
        return this.currentIndex;
    }


    public Track getNextTrackOnList(){
        if(!attemptSetupOfIndexesIfEmpty()){
            return null;
        }
        currentIndex = currentIndex >= tracks.size() -1 ? 0 : currentIndex + 1;
        Track currentTrack = tracks.get(currentIndex);
        trackHistory.removeHistoriesAfterCurrent();
        trackHistory.add(currentTrack);
        return currentTrack;
    }


    private Track getPreviousTrackOnList(){
        currentIndex--;
        if(currentIndex < 0){
            currentIndex = tracks.size()-1;
        }
        return tracks.get(currentIndex);
    }


    @Override
    public Track getNextRandomUnPlayedTrack(){
        if(trackHistory.isHistoryIndexOld()){
            return trackHistory.getNextTrack();
        }
        if(!attemptSetupOfIndexesIfEmpty()){
            return null;
        }
        int randomIndex = getNextRandomIndex(unPlayedTracks.size());
        Track currentTrack = unPlayedTracks.remove(randomIndex);
        currentIndex = currentTrack.getIndex();
        attemptSetupOfIndexesIfEmpty();
        trackHistory.add(currentTrack);
        return currentTrack;
    }


    @Override
    public boolean areAllTracksLoaded(){
        return currentPlaylist == null || currentPlaylist.getId() == ALL_TRACKS_PLAYLIST_ID;
    }


    private boolean attemptSetupOfIndexesIfEmpty(){
        if(unPlayedTracks.isEmpty()){
            if(tracks.isEmpty()){
                return false;
            }
            setupUnPlayedIndexes();
        }
        return true;
    }


    public Track selectTrack(int index){
        if(index > tracks.size()){
            return null;
        }
        currentIndex = index;
        trackHistory.removeHistoriesAfterCurrent();
        Track track = tracks.get(index);
        trackHistory.add(track);
        return tracks.get(index);
    }


    private int getNextRandomIndex(int listSize){
        return listSize < 2 ? 0 : random.nextInt(listSize);
    }


    public List<Track> getTracks(){

        log("Entered getTracks() tracks size: " + tracks.size());
        return tracks;
    }


    public void savePlaylist(){}

}
