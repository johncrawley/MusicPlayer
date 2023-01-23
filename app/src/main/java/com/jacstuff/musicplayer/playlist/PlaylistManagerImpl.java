package com.jacstuff.musicplayer.playlist;

import android.content.Context;

import com.jacstuff.musicplayer.db.album.Album;
import com.jacstuff.musicplayer.db.artist.Artist;
import com.jacstuff.musicplayer.db.playlist.Playlist;
import com.jacstuff.musicplayer.db.playlist.PlaylistItemRepository;
import com.jacstuff.musicplayer.db.playlist.PlaylistItemRepositoryImpl;
import com.jacstuff.musicplayer.db.track.Track;
import com.jacstuff.musicplayer.db.track.TrackRepository;
import com.jacstuff.musicplayer.db.track.TrackRepositoryImpl;
import com.jacstuff.musicplayer.service.MediaPlayerService;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;


public class PlaylistManagerImpl implements PlaylistManager {

    private int currentIndex = 0;
    private final AudioInfoLoader sdCardReader;
    private final Random random;
    private final TrackRepository trackRepository;
    private final PlaylistItemRepository playlistItemRepository;
    private int previousNumberOfTracks;
    private List<Track> tracks;
    private List<Track> allTracks;
    private List<Integer> unPlayedPathnameIndexes;
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
    private ArrayDeque<Track> queuedTracks;


    public PlaylistManagerImpl(Context context){
        trackRepository = new TrackRepositoryImpl(context);
        playlistItemRepository = new PlaylistItemRepositoryImpl(context);
        tracks = new ArrayList<>();
        unPlayedPathnameIndexes = new ArrayList<>();
        random = new Random(System.currentTimeMillis());
        sdCardReader = new AudioInfoLoader(context, trackRepository);
        initTrackList();
        setupDefaultPlaylists();
        previousNumberOfTracks = tracks.size();
        trackHistory = new TrackHistory();
        queuedTracks = new ArrayDeque<>();
    }


    @Override
    public void onDestroy(){
    }


    public void enableShuffle(){
        isShuffleEnabled = true;
    }

    public void disableShuffle(){
        isShuffleEnabled = false;
    }


    @Override
    public void addTracksFromStorage(MediaPlayerService mediaPlayerService){
        sdCardReader.loadAudioFiles();
        initTrackList();
        calculateAndDisplayNewTracksStats(mediaPlayerService);
    }


    @Override
    public boolean hasTracksQueued(){
        boolean hasTracksQueued = !queuedTracks.isEmpty();
        return hasTracksQueued;
    }


    @Override
    public boolean isUserPlaylistLoaded(){
        return currentPlaylist != null && !defaultPlaylistIds.contains(currentPlaylist.getId());
    }


    private void calculateAndDisplayNewTracksStats(MediaPlayerService mediaPlayerService){
        //int numberOfNewTracks = tracks.size() - previousNumberOfTracks;
        mediaPlayerService.displayPlaylistRefreshedMessage(0);
       // previousNumberOfTracks = tracks.size();
    }


    public void init(){
        initTrackList();
    }


    private void initTrackList(){
        tracks = getSortedTracks(trackRepository.getAllTracks());
        assignIndexesToTracks();
        allTracks = new ArrayList<>(tracks);
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
        if(currentPlaylist != null && (this.currentPlaylist.getId() == playlist.getId())){
            return;
        }
        this.currentPlaylist = playlist;

        if(playlist.getId() == ALL_TRACKS_PLAYLIST_ID){
            loadAllTracksPlaylist();
            tracks = new ArrayList<>(allTracks);
        }
        else{
            tracks = playlistItemRepository.getTracksForPlaylistId(playlist.getId());
            assignIndexesToTracks();
        }
        setupUnplayedIndexes();
        trackHistory.reset();
        currentIndex = -1;
    }


    @Override
    public void addTrackToCurrentPlaylist(Track track) {
        if(!isUserPlaylistLoaded()){
              return;
        }
        tracks.add(track);
        playlistItemRepository.addPlaylistItem(track, currentPlaylist.getId());
    }


    @Override
    public void addTracksToCurrentPlaylist(List<Track> tracks) {
        if(!isUserPlaylistLoaded()){
            return;
        }
        this.tracks.addAll(tracks);
        for(Track track : tracks){
            playlistItemRepository.addPlaylistItem(track, currentPlaylist.getId());
        }
    }


    @Override
    public void removeTrackToCurrentPlaylist(Track track) {
        if(!isUserPlaylistLoaded()){
            return;
        }
        tracks.remove(track);
        playlistItemRepository.deletePlaylistItem(track.getId());
    }


    private void loadAllTracksPlaylist(){
        if(allTracks == null){
            initTrackList();
        }
        setupUnplayedIndexes();
        trackHistory.reset();
        currentPlaylist = allTracksPlaylist;
    }


    private List<Track> getSortedTracks(List<Track> list){
        return list.stream().sorted(Comparator.comparing(Track::getOrderedString)).collect(Collectors.toList());
    }


    public void loadTracksFromArtist(Artist artist){
        tracks = getSortedTracks(trackRepository.getTracksForArtist(artist));
        assignIndexesToTracks();
        setupQueue();
        currentPlaylist = someArtistPlaylist;
    }


    @Override
    public void loadTracksFromAlbum(Album album) {
        tracks = getSortedTracks(trackRepository.getTracksForAlbum(album));
        assignIndexesToTracks();
        setupQueue();
        currentPlaylist = someAlbumPlaylist;
    }


    private void setupQueue(){
        setupUnplayedIndexes();
        trackHistory.reset();
        currentIndex = -1;
    }


    private void assignIndexesToTracks(){
        for(int i = 0; i< tracks.size(); i++){
            tracks.get(i).setIndex(i);
        }
    }


    private void setupUnplayedIndexes(){
        unPlayedPathnameIndexes = new ArrayList<>(tracks.size());
        for(int i = 0; i< tracks.size(); i++){
           unPlayedPathnameIndexes.add(i);
        }
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
        int freshSongsCount = unPlayedPathnameIndexes.size();
        int randomIndex = getNextRandomIndex(freshSongsCount);
        currentIndex = getAndRemoveSongIndex(randomIndex);
        attemptSetupOfIndexesIfEmpty();
        Track currentTrack = tracks.get(currentIndex);
        trackHistory.add(currentTrack);
        return currentTrack;
    }


    @Override
    public boolean areAllTracksLoaded(){
        return currentPlaylist == null || currentPlaylist.getId() == ALL_TRACKS_PLAYLIST_ID;
    }


    private int getAndRemoveSongIndex(int index){
        int songIndex = unPlayedPathnameIndexes.get(index);
        unPlayedPathnameIndexes.remove(index);
        return songIndex;
    }


    private boolean attemptSetupOfIndexesIfEmpty(){
        if(unPlayedPathnameIndexes.isEmpty()){
            if(tracks.isEmpty()){
                return false;
            }
            setupUnplayedIndexes();
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
        return tracks;
    }


    public void savePlaylist(){}

}
