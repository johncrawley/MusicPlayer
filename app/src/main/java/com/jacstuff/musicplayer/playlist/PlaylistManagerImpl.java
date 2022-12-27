package com.jacstuff.musicplayer.playlist;

import android.content.Context;

import com.jacstuff.musicplayer.db.playlist.TrackHistory;
import com.jacstuff.musicplayer.db.track.Track;
import com.jacstuff.musicplayer.db.track.TrackRepository;
import com.jacstuff.musicplayer.db.track.TrackRepositoryImpl;
import com.jacstuff.musicplayer.service.MediaPlayerService;

import java.util.ArrayList;
import java.util.Collections;
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
    private TrackHistory trackHistory;
    private MediaPlayerService mediaPlayerService;


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
        tracks = trackRepository.getAllTracks().stream().sorted(Comparator.comparing(Track::getOrderedString)).collect(Collectors.toList());
        log("initTrackList() got tracks: " + tracks.size());
        for(Track track : tracks){
            log("Got track : " + track.getArtist() + " : " + track.getName());
        }
        assignIndexesToTracks();
    }


    private void assignIndexesToTracks(){
        for(int i=0; i< tracks.size(); i++){
            tracks.get(i).setIndex(i);
        }
    }

    private void log(String msg){
        System.out.println("^^^ PlaylistManagerImpl: " + msg);
    }


    private void setupUnplayedIndexes(){
        final int INITIAL_LIST_CAPACITY = 10_000;
        unPlayedPathnameIndexes = new ArrayList<>(INITIAL_LIST_CAPACITY);
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
        return getNextRandomUnPlayedTrack();
    }


    @Override
    public Track getPreviousTrack(){
        return trackHistory.getPreviousTrack();
    }


    @Override
    public int getCurrentTrackIndex(){
        return this.currentIndex;
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
