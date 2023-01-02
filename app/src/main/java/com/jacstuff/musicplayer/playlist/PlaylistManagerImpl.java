package com.jacstuff.musicplayer.playlist;

import android.content.Context;

import com.jacstuff.musicplayer.db.artist.Artist;
import com.jacstuff.musicplayer.db.track.Track;
import com.jacstuff.musicplayer.db.track.TrackRepository;
import com.jacstuff.musicplayer.db.track.TrackRepositoryImpl;
import com.jacstuff.musicplayer.service.MediaPlayerService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


public class PlaylistManagerImpl implements PlaylistManager {

    private int currentIndex = 0;
    private final AudioInfoLoader sdCardReader;
    private final Random random;
    private final TrackRepository trackRepository;
    private int previousNumberOfTracks;
    private List<Track> tracks;
    private List<Integer> unPlayedPathnameIndexes;
    private final TrackHistory trackHistory;
    private MediaPlayerService mediaPlayerService;
    private boolean isShuffleEnabled = true;


    public PlaylistManagerImpl(Context context){
        trackRepository = new TrackRepositoryImpl(context);
        tracks = new ArrayList<>();
        unPlayedPathnameIndexes = new ArrayList<>();
        random = new Random(System.currentTimeMillis());
        sdCardReader = new AudioInfoLoader(context, trackRepository);
        initTrackList();
        previousNumberOfTracks = tracks.size();
        trackHistory = new TrackHistory();
    }


    @Override
    public void onDestroy(){
        mediaPlayerService = null;
    }


    public void enableShuffle(){
        isShuffleEnabled = true;
    }

    public void disableShuffle(){
        isShuffleEnabled = false;
    }


    @Override
    public void addTracksFromStorage(){
        sdCardReader.loadAudioFiles();
        initTrackList();
        calculateAndDisplayNewTracksStats();
    }


    private void calculateAndDisplayNewTracksStats(){
        int numberOfNewTracks = tracks.size() - previousNumberOfTracks;
        if(numberOfNewTracks > 0){
            mediaPlayerService.displayPlaylistRefreshedMessage(numberOfNewTracks);
        }
        previousNumberOfTracks = tracks.size();
    }


    public void init(){
        initTrackList();
    }


    private void initTrackList(){
        tracks = getSortedTracks(trackRepository.getAllTracks());
        assignIndexesToTracks();
    }


    private List getSortedTracks(List<Track> list){
        return list.stream().sorted(Comparator.comparing(Track::getOrderedString)).collect(Collectors.toList());
    }


    public void loadTracksFromArtist(Artist artist){
        tracks = getSortedTracks(trackRepository.getTracksForArtist(artist));
        assignIndexesToTracks();
        setupUnplayedIndexes();
        trackHistory.reset();
        currentIndex = -1;
    }


    private void assignIndexesToTracks(){
        for(int i=0; i< tracks.size(); i++){
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
        return position >= tracks.size() ? "" : tracks.get(position).getName();
    }


    public int getNumberOfTracks(){
        return tracks.size();
    }


    @Override
    public Track getNextTrack(){
        return isShuffleEnabled ? getNextRandomUnPlayedTrack() : getNextTrackOnList();
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
