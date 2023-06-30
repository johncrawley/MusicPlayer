package com.jacstuff.musicplayer.service.playlist;

import android.content.Context;

import com.jacstuff.musicplayer.service.db.album.Album;
import com.jacstuff.musicplayer.service.db.artist.Artist;
import com.jacstuff.musicplayer.service.db.playlist.Playlist;
import com.jacstuff.musicplayer.service.db.playlist.PlaylistItemRepository;
import com.jacstuff.musicplayer.service.db.playlist.PlaylistItemRepositoryImpl;
import com.jacstuff.musicplayer.service.db.playlist.PlaylistRepository;
import com.jacstuff.musicplayer.service.db.playlist.PlaylistRepositoryImpl;
import com.jacstuff.musicplayer.service.db.track.Track;
import com.jacstuff.musicplayer.service.MediaPlayerService;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;


public class PlaylistManagerImpl implements PlaylistManager {

    private int currentIndex = 0;
    private final TrackLoader trackLoader;
    private final Random random;
    private final PlaylistRepository playlistRepository;
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
    private Artist currentArtist;
    private final ArrayDeque<Track> queuedTracks;
    private boolean shouldOnlyDisplayMainArtists = true;
    private Set<String> currentPlaylistFilenames;
    private Map<String, Integer> trackPathsToIndexesMap;
    private Map<String, Integer> allTracksPathsToIndexesMap;


    public PlaylistManagerImpl(Context context, TrackLoader trackLoader){
        playlistRepository = new PlaylistRepositoryImpl(context);
        playlistItemRepository = new PlaylistItemRepositoryImpl(context);
        tracks = new ArrayList<>(10_000);
        allTracks = new ArrayList<>(10_000);
        unPlayedTracks = new ArrayList<>();
        random = new Random(System.currentTimeMillis());
        this.trackLoader = trackLoader;
        setupDefaultPlaylists();
        initTrackList();
        trackHistory = new TrackHistory();
        queuedTracks = new ArrayDeque<>();
        currentPlaylistFilenames = new HashSet<>();
        trackPathsToIndexesMap = new HashMap<>();
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


    private void loadAllTracksIfNoPlaylistLoaded(){
        if(currentPlaylist == null){
            loadAllTracksPlaylist();
        }
    }


    @Override
    public List<Playlist> getAllPlaylists(){
        return playlistRepository.getAllPlaylists();
    }


    @Override
    public List<Playlist> getAllUserPlaylists(){
        return playlistRepository.getAllUserPlaylists();
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
        return currentArtist == null ?
                trackLoader.getAllAlbumNames() : currentArtist.getAlbumNames();
    }


    public ArrayList<String> getArtistNames(){
        return  shouldOnlyDisplayMainArtists ?
                trackLoader.getMainArtistNames()
                : trackLoader.getArtistNames();
    }


    @Override
    public void deleteAll(){
        initTrackList();
    }


    @Override
    public void onlyDisplayMainArtists(boolean shouldOnlyDisplayMainArtists){
        this.shouldOnlyDisplayMainArtists = shouldOnlyDisplayMainArtists;
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


    private void initTrackList(){
        tracks = allTracks;
        allTracksPlaylist.setTracks(allTracks);
        assignIndexesToTracks();
    }


    private void setupDefaultPlaylists(){
        defaultPlaylistIds = new HashSet<>();
        defaultPlaylistIds.add(SOME_ALBUM_PLAYLIST_ID);
        defaultPlaylistIds.add(SOME_ARTIST_PLAYLIST_ID);
        defaultPlaylistIds.add(ALL_TRACKS_PLAYLIST_ID);

        someAlbumPlaylist = new Playlist(SOME_ALBUM_PLAYLIST_ID, "Some Album", Playlist.PlaylistType.ALBUM, false);
        someArtistPlaylist = new Playlist(SOME_ARTIST_PLAYLIST_ID, "Some Artist", Playlist.PlaylistType.ARTIST, false);
        allTracksPlaylist = new Playlist(ALL_TRACKS_PLAYLIST_ID, ALL_TRACKS_PLAYLIST, false);
        currentPlaylist = allTracksPlaylist;
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
            currentPlaylist.setTracks(tracks);
            currentPlaylistFilenames = tracks.stream().map(Track::getPathname).collect(Collectors.toSet());
            assignIndexesToTracks();
        }
        setupUnPlayedIndexes();
        trackHistory.reset();
        currentIndex = -1;
        currentArtist = null;
    }


    private boolean isAlreadyCurrentPlaylist(Playlist playlist){
       return currentPlaylist != null && (Objects.equals(this.currentPlaylist.getId(), playlist.getId()));
    }


    @Override
    public void addTrackToCurrentPlaylist(Track track, PlaylistViewNotifier playlistViewNotifier) {
        if(!isUserPlaylistLoaded()){
            return;
        }
        if(currentPlaylistFilenames.contains(track.getPathname())){
            playlistViewNotifier.notifyViewOfTrackAlreadyInPlaylist();
            return;
        }
        tracks.add(track);
        unPlayedTracks.add(track);
        setIndexForAddedTrack(track);
        playlistItemRepository.addPlaylistItem(track, currentPlaylist.getId());
        playlistViewNotifier.notifyViewOfTrackAddedToPlaylist();
        currentPlaylistFilenames.add(track.getPathname());
    }


    private void setIndexForAddedTrack(Track track){
        int index = tracks.size() - 1;
        track.setIndex(index);
        trackPathsToIndexesMap.put(track.getPathname(), index);
    }


    @Override
    public void addTrackToPlaylist(Track track, Playlist playlist, PlaylistViewNotifier playlistViewNotifier) {
        if(playlist == null){
            return;
        }
        long playlistId = playlist.getId();
        if(playlistId == currentPlaylist.getId()){
            addTrackToCurrentPlaylist(track, playlistViewNotifier);
        }

        if(playlistItemRepository.isTrackAlreadyInPlaylist(track, playlistId)){
            playlistViewNotifier.notifyViewOfTrackAlreadyInPlaylist();
            return;
        }
        playlistItemRepository.addPlaylistItem(track, playlistId);
        playlistViewNotifier.notifyViewOfTrackAddedToPlaylist();
    }



    private void addTracksToCurrentPlaylist(List<Track> additionalTracks, PlaylistViewNotifier playlistViewNotifier) {
        if(!isUserPlaylistLoaded()){
            return;
        }
        int originalNumberOfTracks = tracks.size();
        additionalTracks.stream()
                .filter(this::isTrackNotInCurrentPlaylist)
                .forEach(this::addNewTrackToPlaylist);
        int numberOfNewTracks = tracks.size() - originalNumberOfTracks;
        if(numberOfNewTracks > 0) {
           assignIndexesToTracks();
        }
        playlistViewNotifier.notifyViewOfMultipleTracksAddedToPlaylist(numberOfNewTracks);
    }


    private void addNewTrackToPlaylist(Track track){
        tracks.add(track);
        unPlayedTracks.add(track);
        currentPlaylistFilenames.add(track.getPathname());
        playlistItemRepository.addPlaylistItem(track, currentPlaylist.getId());
    }


    private boolean isTrackNotInCurrentPlaylist(Track track){
        return !currentPlaylistFilenames.contains(track.getPathname());
    }


    @Override
    public void removeTrackFromCurrentPlaylist(Track track, PlaylistViewNotifier playlistViewNotifier) {
        if(!isUserPlaylistLoaded()){
            return;
        }
        int oldNumberOfTracks = tracks.size();
        tracks.remove(track.getIndex());
        unPlayedTracks.remove(track);
        currentPlaylistFilenames.remove(track.getPathname());
        assignIndexesToTracks();
        playlistItemRepository.deletePlaylistItem(track.getId());
        playlistViewNotifier.notifyViewOfTrackRemovedFromPlaylist(oldNumberOfTracks != tracks.size());
    }


    @Override
    public int getCurrentIndexOf(Track track) {
        Integer index = trackPathsToIndexesMap.getOrDefault(track.getPathname(), -1);
        return index == null ? -1 : index;
    }


    @Override
    public void loadAllTracksPlaylist(){
        initAllTracks();
        setAllTracksIndexes();
        setupUnPlayedIndexes();
        trackHistory.reset();
        currentPlaylist = allTracksPlaylist;
        tracks = new ArrayList<>(allTracks);
    }


    private void initAllTracks(){
        if(allTracks == null){
            initTrackList();
        }
    }


    private void setAllTracksIndexes(){
        if(allTracksPathsToIndexesMap == null){
            assignIndexesToAllTracks();
        }
        trackPathsToIndexesMap = allTracksPathsToIndexesMap;
    }


    public void loadTracksFromArtist(String artistName){
        Map <String, Artist> artists = trackLoader.getArtists();
        if(artists == null){
            return;
        }
        currentArtist = artists.get(artistName);
        if(currentArtist == null){
            return;
        }
        someArtistPlaylist.setTracks(currentArtist.getTracks());
        someArtistPlaylist.setName(artistName);
        tracks = currentArtist.getTracks();
        assignIndexesToTracks();
        setupQueue();
        currentPlaylist = someArtistPlaylist;
    }


    @Override
    public boolean loadTracksFromAlbum(String albumName) {
        Map <String, Album> albums = trackLoader.getAlbums();
        if(albums == null){
            return false;
        }
        Album savedAlbum = albums.get(albumName);
        if(savedAlbum == null){
            return false;
        }
        tracks = getSortedAlbumTracks(savedAlbum.getAllTracks());
        someAlbumPlaylist.setTracks(getSortedAlbumTracks(savedAlbum.getAllTracks()));
        someAlbumPlaylist.setName(albumName);
        assignIndexesToTracks();
        setupQueue();
        currentPlaylist = someAlbumPlaylist;
        return true;
    }


    @Override
    public void addTracksFromArtistToCurrentPlaylist(String artistName, PlaylistViewNotifier playlistViewNotifier) {
        List<Track> tracks = trackLoader.getTracksForArtist(artistName);
        addTracksToCurrentPlaylist(getSortedTracks(tracks), playlistViewNotifier);
    }


    @Override
    public void addTracksFromAlbumToCurrentPlaylist(String albumName, PlaylistViewNotifier playlistViewNotifier) {
        List<Track> tracks = trackLoader.getTracksForAlbum(albumName);
        addTracksToCurrentPlaylist(getSortedAlbumTracks(tracks), playlistViewNotifier);
    }


    private List<Track> getSortedTracks(List<Track> list){
        if(list == null){
            return Collections.emptyList();
        }
        return list.stream().sorted(Comparator.comparing(Track::getOrderedString)).collect(Collectors.toList());
    }


    private List<Track> getSortedAlbumTracks(List<Track> albumTracks){
        if(albumTracks == null){
            return Collections.emptyList();
        }
        return albumTracks.stream().sorted(Comparator.comparing(Track::getTrackNumber)).collect(Collectors.toList());
    }

    private void setupQueue(){
        setupUnPlayedIndexes();
        trackHistory.reset();
        currentIndex = -1;
    }


    private void assignIndexesToTracks(){
        trackPathsToIndexesMap = new HashMap<>();
        for(int i = 0; i< tracks.size(); i++){
            assignIndexToTrack(tracks.get(i), i);
        }
    }


    private void assignIndexesToAllTracks(){
        allTracksPathsToIndexesMap = new HashMap<>();
        for(int i = 0; i< tracks.size(); i++){
            allTracksPathsToIndexesMap.put(tracks.get(i).getPathname(), i);
        }
    }


    private void assignIndexToTrack(Track track, int index){
        track.setIndex(index);
        trackPathsToIndexesMap.put(track.getPathname(), index);
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
    public Optional<Track> getPreviousTrack(){
        return Optional.ofNullable(isShuffleEnabled ? getPreviousTrackFromHistory() : getPreviousTrackOnList());
    }


    private Track getPreviousTrackFromHistory(){
        Track track = trackHistory.getPreviousTrack();
        if(track == null){
            return null;
        }
        currentIndex = track.getIndex();
        return track;
    }


    private Track getNextTrackOnList(){
        if(!attemptSetupOfIndexesIfEmpty() || tracks.isEmpty()){
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


    private boolean attemptSetupOfIndexesIfEmpty(){
        if(unPlayedTracks.isEmpty()){
            if(tracks.isEmpty()){
                return false;
            }
            setupUnPlayedIndexes();
        }
        return true;
    }


    @Override
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


    @Override
    public void addToTrackHistory(Track track) {
        trackHistory.removeHistoriesAfterCurrent();
        trackHistory.add(track);
    }


    private int getNextRandomIndex(int listSize){
        return listSize < 2 ? 0 : random.nextInt(listSize);
    }


    @Override
    public Playlist getCurrentPlaylist(){return currentPlaylist;}

}
