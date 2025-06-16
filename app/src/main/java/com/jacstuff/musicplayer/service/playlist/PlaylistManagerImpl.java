package com.jacstuff.musicplayer.service.playlist;

import android.content.Context;

import com.jacstuff.musicplayer.service.db.entities.PlaylistStore;
import com.jacstuff.musicplayer.service.db.entities.Artist;
import com.jacstuff.musicplayer.service.db.entities.Playlist;
import com.jacstuff.musicplayer.service.db.playlist.PlaylistItemRepository;
import com.jacstuff.musicplayer.service.db.playlist.PlaylistItemRepositoryImpl;
import com.jacstuff.musicplayer.service.db.playlist.PlaylistRepository;
import com.jacstuff.musicplayer.service.db.playlist.PlaylistRepositoryImpl;
import com.jacstuff.musicplayer.service.db.entities.PlaylistType;
import com.jacstuff.musicplayer.service.db.entities.Track;
import com.jacstuff.musicplayer.service.MediaPlayerService;
import com.jacstuff.musicplayer.service.helpers.preferences.PrefKey;
import com.jacstuff.musicplayer.service.helpers.preferences.PreferencesHelperImpl;
import com.jacstuff.musicplayer.service.loader.TrackLoader;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
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
    private Playlist currentPlaylist;
    private Artist currentArtist;
    private final ArrayDeque<Track> queuedTracks;
    private Set<String> currentPlaylistFilenames;
    private String currentPlaylistName = "";
    private String currentArtistName = "";
    private final PreferencesHelperImpl preferencesHelper;
    private final IndexManager indexManager;
    private final RandomTrackAppender randomTrackAppender;


    public PlaylistManagerImpl(Context context, TrackLoader trackLoader, IndexManager indexManager){
        playlistRepository = new PlaylistRepositoryImpl(context);
        playlistItemRepository = new PlaylistItemRepositoryImpl(context);
        this.indexManager = indexManager;
        tracks = new ArrayList<>(10_000);
        allTracks = new ArrayList<>(10_000);
        unPlayedTracks = new ArrayList<>();
        random = new Random(System.currentTimeMillis());
        this.trackLoader = trackLoader;
        initTrackList();
        trackHistory = new TrackHistory();
        queuedTracks = new ArrayDeque<>();
        currentPlaylistFilenames = new HashSet<>();
        preferencesHelper = new PreferencesHelperImpl(context);
        randomTrackAppender = new RandomTrackAppender(preferencesHelper);
    }


    @Override
    public void setShuffleState(boolean isEnabled){
        isShuffleEnabled = isEnabled;
        if(isEnabled && unPlayedTracks.size() != tracks.size()){
            unPlayedTracks = new ArrayList<>(tracks);
        }
    }


    public void enableShuffle(){
        isShuffleEnabled = true;
        unPlayedTracks = new ArrayList<>(tracks);
    }


    public boolean hasAnyTracks(){
        return tracks != null && !tracks.isEmpty();
    }


    public void disableShuffle(){
        isShuffleEnabled = false;
    }


    @Override
    public void addTracksFromStorage(MediaPlayerService mediaPlayerService){
        allTracks = getSortedTracks(trackLoader.loadAudioFiles());
        indexManager.assignIndexesToAllTracks(allTracks);
        initTrackList();
        calculateAndDisplayNewTracksStats(mediaPlayerService);
        loadAllTracksIfNoPlaylistLoaded();
        reloadCurrentPlaylistAfterRefresh();
    }


    private void loadAllTracksIfNoPlaylistLoaded(){
        if(currentPlaylist == null){
            loadAllTracksPlaylist();
        }
    }


    private void reloadCurrentPlaylistAfterRefresh(){
        if(currentPlaylistName.isBlank()){
            return;
        }
        switch (currentPlaylist.getType()){
            case ARTIST -> loadTracksFromArtist(currentPlaylistName);
            case ALBUM -> loadTracksFromAlbum(currentPlaylistName);
            case GENRE -> loadTracksFromGenre(currentPlaylistName);
            default -> {}
        }
    }


    @Override
    public List<Playlist> getAllUserPlaylists(){
        return playlistRepository.getAllUserPlaylists();
    }


    @Override
    public List<Playlist> getAllPlaylists(){
        return playlistRepository.getAllPlaylists();
    }


    @Override
    public void deletePlaylist(Playlist playlist){
        playlistRepository.deletePlaylist(playlist.getId());
    }


    @Override
    public ArrayList<String> getAlbumNames(){
        return currentArtist == null ?
                trackLoader.getAllAlbumNames() : currentArtist.getAlbumNames();
    }


    @Override
    public ArrayList<String> getAllAlbumNamesAndClearCurrentArtist(){
        currentArtist = null;
        resetCurrentArtistName();
        return trackLoader.getAllAlbumNames();
    }


    @Override
    public Optional<String> getCurrentArtistName(){
        return Optional.ofNullable(currentArtistName);
    }


    @Override
    public ArrayList<String> getArtistNames(){
       return trackLoader.getMainArtistNames();
    }


    @Override
    public ArrayList<String> getGenreNames(){
        return trackLoader.getAllGenreNames();
    }


    @Override
    public boolean isUserPlaylistLoaded(){
        return currentPlaylist != null && currentPlaylist.isUserPlaylist();
    }


    private void calculateAndDisplayNewTracksStats(MediaPlayerService mediaPlayerService){
        mediaPlayerService.displayPlaylistRefreshedMessage(0);
    }


    public void loadPlaylist(Playlist playlist){
        if(isCurrentPlaylist(playlist)){
            return;
        }
        if(playlist.getType() == PlaylistType.ALL_TRACKS){
            loadAllTracksPlaylist();
        }
        else{
           loadUserPlaylist(playlist);
        }
        setInitialPlaylistState();
    }


    @Override
    public void loadAllTracksPlaylist(){
        resetCurrentArtistName();
        initAllTracks();
        indexManager.setAllTracksIndexes();
        setupUnPlayedIndexes();
        trackHistory.reset();
        currentPlaylist = playlistRepository.getAllTracksPlaylist();
        tracks = allTracks;
    }


    private void loadUserPlaylist(Playlist playlist){
        resetCurrentArtistName();
        currentPlaylist = playlist;
        tracks = playlistItemRepository.getTracksForPlaylistId(playlist.getId());
        currentPlaylist.setTracks(tracks);
        currentPlaylistFilenames = tracks.stream().map(Track::getPathname).collect(Collectors.toSet());
        assignIndexesToTracks();
    }


    private void setInitialPlaylistState(){
        setupUnPlayedIndexes();
        trackHistory.reset();
        currentIndex = -1;
        currentArtist = null;
    }


    private boolean isCurrentPlaylist(Playlist playlist){
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
        indexManager.setIndexForAddedTrack(track);
        playlistItemRepository.addPlaylistItem(track, currentPlaylist.getId());
        playlistViewNotifier.notifyViewOfTrackAddedToPlaylist();
        currentPlaylistFilenames.add(track.getPathname());
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
        return indexManager.getIndexOf(track);
    }


    public int getCurrentIndex(){
        return currentIndex;
    }


    private void initAllTracks(){
        if(allTracks == null){
            initTrackList();
        }
    }


    private void initTrackList(){
        tracks = allTracks;
        currentPlaylist = playlistRepository.getAllTracksPlaylist();
        currentPlaylist.setTracks(allTracks);
        assignIndexesToTracks();
    }


    @Override
    public void loadTracksFromArtist(String artistName){
        loadTracksFrom(artistName, trackLoader::getArtist, this::getSortedTracks);
        currentArtist = trackLoader.getArtists().get(artistName);
        currentArtistName = currentArtist != null ? currentArtist.getName() : "";
    }


    @Override
    public boolean loadTracksFromAlbum(String albumName) {
        return loadTracksFrom(albumName, trackLoader::getAlbum, this::getSortedArtistAlbumTracks);
    }


    @Override
    public boolean loadAllTracksFromAlbum(String albumName) {
        return loadTracksFrom(albumName, trackLoader::getAlbum, this::getSortedAlbumTracks);
    }


    @Override
    public boolean loadTracksFromGenre(String genreName) {
        resetCurrentArtistName();
        return loadTracksFrom(genreName, trackLoader::getGenre, this::getSortedTracks);
    }


    private void resetCurrentArtistName(){
        currentArtistName = "";
    }


    private boolean loadTracksFrom(String name, Function<String, PlaylistStore> function, Function<List<Track>, List<Track>> sortingConsumer){
        PlaylistStore  playlistStore = function.apply(name);
        if(playlistStore == null){
            return false;
        }
        currentPlaylistName = name;
        tracks = sortingConsumer.apply(playlistStore.getTracks());
        assignIndexesToTracks();
        setupQueue();
        currentPlaylist = playlistStore.getPlaylist();
        currentPlaylist.setTracks(tracks);
        return true;
    }


    private void assignIndexesToTracks(){
        indexManager.assignIndexesToTracks(tracks);
    }


    @Override
    public void addTracksFromArtistToCurrentPlaylist(String artistName, PlaylistViewNotifier playlistViewNotifier) {
        List<Track> tracks = trackLoader.getTracksForArtist(artistName);
        addTracksToCurrentPlaylist(getSortedTracks(tracks), playlistViewNotifier);
    }


    @Override
    public void addRandomTracksFromArtistToCurrentPlaylist(String name, PlaylistViewNotifier playlistViewNotifier){
        addRandomTracksToPlaylist(()-> trackLoader.getTracksForArtist(name), getDefaultNumberOfRandomTracksToCopy(), playlistViewNotifier );
    }


    @Override
    public void addTracksFromAlbumToCurrentPlaylist(String albumName, PlaylistViewNotifier playlistViewNotifier) {
        List<Track> tracks = trackLoader.getTracksForAlbum(albumName);
        addTracksToCurrentPlaylist(getSortedAlbumTracks(tracks), playlistViewNotifier);
    }


    @Override
    public void addRandomTracksFromAlbumToCurrentPlaylist(String name, PlaylistViewNotifier playlistViewNotifier){
        addRandomTracksToPlaylist(()-> trackLoader.getTracksForAlbum(name), getDefaultNumberOfRandomTracksToCopy(), playlistViewNotifier );
    }


    @Override
    public void addRandomTracksToCurrentPlaylist(PlaylistType playlistType, List<String> names, int numberOfTracks, PlaylistViewNotifier playlistViewNotifier){
        addRandomTracksToPlaylist(()-> getTracksFor(playlistType, names), numberOfTracks, playlistViewNotifier );
    }


    private List<Track> getTracksFor(PlaylistType playlistType, List<String> names){
        if(playlistType == PlaylistType.ALL_TRACKS){
            return this.tracks;
        }
        var trackList = new ArrayList<Track>();
        for(var name : names){
            trackList.addAll(trackLoader.getTracksFor(playlistType, name));
        }
        return trackList;
    }


    private void addRandomTracksToPlaylist(Supplier<List<Track>> tracksSupplier, int numberToAdd, PlaylistViewNotifier playlistViewNotifier){
        var randomTracks = randomTrackAppender.getUniqueRandomTracksFrom(tracksSupplier.get(), tracks, numberToAdd);
        addTracksToCurrentPlaylist(randomTracks, playlistViewNotifier, false);
    }


    private int getDefaultNumberOfRandomTracksToCopy(){
        return Math.max(1, preferencesHelper.getInt(PrefKey.NUMBER_OF_RANDOM_TRACKS_TO_ADD));
    }


    private void addTracksToCurrentPlaylist(List<Track> additionalTracks, PlaylistViewNotifier playlistViewNotifier) {
        addTracksToCurrentPlaylist(additionalTracks, playlistViewNotifier, true);
    }


    private void addTracksToCurrentPlaylist(List<Track> additionalTracks, PlaylistViewNotifier playlistViewNotifier, boolean isCheckPerformed) {
        if(!isUserPlaylistLoaded()){
            return;
        }
        int originalNumberOfTracks = tracks.size();
        addTracksToPlaylist(additionalTracks, isCheckPerformed);
        int numberOfNewTracks = tracks.size() - originalNumberOfTracks;
        if(numberOfNewTracks > 0) {
            assignIndexesToTracks();
        }
        playlistViewNotifier.notifyViewOfMultipleTracksAddedToPlaylist(numberOfNewTracks);
    }



    public void addRandomTracksToPlaylist(RandomTrackConfig config){
        var playlist = config.playlistName();


    }


    private void addTracksToPlaylist(List<Track> additionalTracks, boolean isExistingCheckPerformed){
        Predicate <Track> predicate = isExistingCheckPerformed ? this::isTrackNotInCurrentPlaylist : (Track)-> true;
        additionalTracks.stream()
                .filter(predicate)
                .forEach(this::addNewTrackToPlaylist);
    }


    private List<Track> getSortedTracks(List<Track> tracks){
        if(tracks == null){
            return Collections.emptyList();
        }
        return tracks.stream().sorted(Comparator.comparing(Track::getOrderedString)).collect(Collectors.toList());
    }


    private List<Track> getSortedArtistAlbumTracks(List<Track> tracks){
        return tracks == null ? Collections.emptyList() :
                tracks.stream()
                .filter(this::filterAlbumTrack)
                .sorted(Comparator.comparing(Track::getCdAndTrackNumber))
                .collect(Collectors.toList());
    }


    private List<Track> getSortedAlbumTracks(List<Track> tracks){
        return tracks == null ? Collections.emptyList() :
                tracks.stream()
                .sorted(Comparator.comparing(Track::getCdAndTrackNumber))
                .collect(Collectors.toList());
    }


    private boolean filterAlbumTrack(Track track){
        return !preferencesHelper.getBoolean(PrefKey.ARE_ONLY_ALBUM_TRACKS_FROM_SELECTED_ARTIST_SHOWN)
                || currentArtistName.isBlank()
                || track.getArtist().equals(currentArtistName);
    }


    private void setupQueue(){
        setupUnPlayedIndexes();
        trackHistory.reset();
        currentIndex = -1;
    }


    public int getNumberOfTracks(){
        return tracks.size();
    }


    @Override
    public Optional<Track> getNextTrack(){
        return Optional.ofNullable(getTrack(this::getNextTrackOnList));
    }


    @Override
    public Optional<Track> getFirstTrack(){
       return Optional.ofNullable(getTrack(this::getFirstTrackOnList));
    }


    private Track getTrack(Supplier<Track> supplier){
        if(!queuedTracks.isEmpty()){
            Track track = queuedTracks.removeLast();
            trackHistory.add(track);
            return track;
        }
        return isShuffleEnabled ? getNextRandomUnPlayedTrack() : supplier.get();
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


    @Override
    public void assignCurrentIndexIfApplicable(Track track){
        int trackIndex = track.getIndex();
        if(trackIndex > -1 && trackIndex < tracks.size()){
            if(tracks.get(trackIndex).getPathname().equals(track.getPathname())){
                currentIndex = trackIndex;
            }
        }
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
        incrementCurrentIndex();
        Track currentTrack = tracks.get(currentIndex);
        trackHistory.removeHistoriesAfterCurrent();
        trackHistory.add(currentTrack);
        return currentTrack;
    }


    private Track getFirstTrackOnList(){
        currentIndex = -1;
        return getNextTrackOnList();
    }


    private Track getPreviousTrackOnList(){
        decrementCurrentIndex();
        return tracks.get(currentIndex);
    }


    private void incrementCurrentIndex(){
        currentIndex = currentIndex >= tracks.size() -1 ? 0 : currentIndex + 1;
    }


    private void decrementCurrentIndex(){
        currentIndex = currentIndex <= 0 ? tracks.size() -1 : currentIndex - 1;
    }


    private Track getNextRandomUnPlayedTrack(){
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


    private void setupUnPlayedIndexes(){
        unPlayedTracks = new ArrayList<>(tracks);
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
