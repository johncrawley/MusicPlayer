package com.jacstuff.musicplayer.playlist;

import android.content.Context;
import android.util.Log;

import com.jacstuff.musicplayer.AudioInfoLoader;
import com.jacstuff.musicplayer.MediaPlayerView;
import com.jacstuff.musicplayer.Track;
import com.jacstuff.musicplayer.db.TrackRepository;
import com.jacstuff.musicplayer.db.TrackRepositoryImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class PlaylistManagerImpl implements PlaylistManager {

    private List<Integer> unplayedPathnameIndexes;
    private int currentIndex = 0;
    private final AudioInfoLoader sdCardReader;
    private List<Track> tracks;
    private final Random random;
    private final TrackRepository trackRepository;
    private int previousNumberOfTracks;
    private MediaPlayerView mediaPlayerView;


    public PlaylistManagerImpl(Context context, MediaPlayerView mediaPlayerView){
        trackRepository = new TrackRepositoryImpl(context);
        random = new Random(System.currentTimeMillis());
        unplayedPathnameIndexes = new ArrayList<>();
        sdCardReader = new AudioInfoLoader(context, trackRepository);
        initTrackDetailsList();
        previousNumberOfTracks = tracks.size();
    }


    @Override
    public void addTracksFromStorage(){
        sdCardReader.loadAudioFiles();
        initTrackDetailsList();
        calculateAndPostNewTracksStats();
    }


    private void initTrackDetailsList(){
        tracks = trackRepository.getAllTracks();
    }


    private void calculateAndPostNewTracksStats(){
        int numberOfNewTracks = tracks.size() - previousNumberOfTracks;
        if(numberOfNewTracks > 0){
            mediaPlayerView.displayPlaylistRefreshedMessage(numberOfNewTracks);
        }
        previousNumberOfTracks = numberOfNewTracks;
    }


    private void setupUnplayedIndexes(){
        final int INITIAL_LIST_CAPACITY = 10_000;
        unplayedPathnameIndexes = new ArrayList<>(INITIAL_LIST_CAPACITY);
        for(int i = 0; i< tracks.size(); i++){
            unplayedPathnameIndexes.add(i);
        }
    }


    public void init(){
        initTrackDetailsList();
    }


    public String getNext(){
        if(currentIndex == tracks.size() -1){
            currentIndex = 0;
        }
        return tracks.get(++currentIndex).getName();
    }

    @Override
    public Track getNextRandomTrack(){
        return tracks.isEmpty() ? null : tracks.get(getNextRandomIndex(tracks.size()));
    }


    private boolean attemptSetupOfIndexesIfEmpty(){
        if(unplayedPathnameIndexes.isEmpty()){
            if(tracks.isEmpty()){
                return false;
            }
            setupUnplayedIndexes();
        }
        return true;
    }


    public String getTrackNameAt(int position){
        return position >= tracks.size() ? "" : tracks.get(position).getName();
    }


    public int getNumberOfTracks(){
        return tracks.size();
    }


    public Track getNextRandomUnplayedTrack(){
        if(!attemptSetupOfIndexesIfEmpty()){
            return null;
        }

        if(unplayedPathnameIndexes.size() == 1){
            currentIndex = unplayedPathnameIndexes.get(0);
            unplayedPathnameIndexes.remove(0);
            attemptSetupOfIndexesIfEmpty();
        }
        else{
            int randomIndex = getNextRandomIndex(unplayedPathnameIndexes.size());
            currentIndex = unplayedPathnameIndexes.get(randomIndex);
            unplayedPathnameIndexes.remove(randomIndex);
        }
        return tracks.get(currentIndex);
    }


    public int getCurrentTrackIndex(){
        return this.currentIndex;
    }


    public Track getTrackDetails(int index){
        if(index > tracks.size()){
            return null;
        }
        return tracks.get(index);
    }

    private int getNextRandomIndex(int listSize){
        return listSize < 1 ? 0 : random.nextInt(listSize -1);
    }


    public List<Track> getTracks(){
        return this.tracks;
    }


    public void savePlaylist(){}


    private void loadPlaylist(){}


    private void log(String msg){
        Log.i("PlayListMngImpl", msg);
    }
}
