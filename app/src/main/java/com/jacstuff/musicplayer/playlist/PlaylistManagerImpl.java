package com.jacstuff.musicplayer.playlist;

import android.content.Context;
import android.util.Log;

import com.jacstuff.musicplayer.AudioInfoLoader;
import com.jacstuff.musicplayer.MediaPlayerView;
import com.jacstuff.musicplayer.Track;
import com.jacstuff.musicplayer.db.TrackRepository;
import com.jacstuff.musicplayer.db.TrackRepositoryImpl;
import com.jacstuff.musicplayer.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class PlaylistManagerImpl implements PlaylistManager {

    private List<Integer> unplayedPathnameIndexes;
    private int currentIndex = 0;
    private final AudioInfoLoader sdCardReader;
    private final Random random;
    private final TrackRepository trackRepository;
    private int previousNumberOfTracks;
    private final MediaPlayerView mediaPlayerView;
    private final MainViewModel viewModel;


    public PlaylistManagerImpl(Context context, MediaPlayerView mediaPlayerView, MainViewModel viewModel){
        trackRepository = new TrackRepositoryImpl(context);
        this.mediaPlayerView = mediaPlayerView;
        this.viewModel = viewModel;
        random = new Random(System.currentTimeMillis());
        unplayedPathnameIndexes = new ArrayList<>();
        sdCardReader = new AudioInfoLoader(context, trackRepository, viewModel);

        initTrackDetailsList();
        previousNumberOfTracks = viewModel.tracks.size();
    }


    @Override
    public void addTracksFromStorage(){
        sdCardReader.loadAudioFiles();
        initTrackDetailsList();
        calculateAndPostNewTracksStats();
    }


    private void initTrackDetailsList(){
        viewModel.tracks = trackRepository.getAllTracks();
    }


    private void calculateAndPostNewTracksStats(){
        int numberOfNewTracks = viewModel.tracks.size() - previousNumberOfTracks;
        if(numberOfNewTracks > 0){
            mediaPlayerView.displayPlaylistRefreshedMessage(numberOfNewTracks);
        }
        previousNumberOfTracks = viewModel.tracks.size();
    }


    private void setupUnplayedIndexes(){
        final int INITIAL_LIST_CAPACITY = 10_000;
        unplayedPathnameIndexes = new ArrayList<>(INITIAL_LIST_CAPACITY);
        for(int i = 0; i< viewModel.tracks.size(); i++){
            unplayedPathnameIndexes.add(i);
        }
    }


    public void init(){
        initTrackDetailsList();
    }


    public String getNext(){
        if(currentIndex == viewModel.tracks.size() -1){
            currentIndex = 0;
        }
        return viewModel.tracks.get(++currentIndex).getName();
    }

    @Override
    public Track getNextRandomTrack(){
        return viewModel.tracks.isEmpty() ? null : viewModel.tracks.get(getNextRandomIndex(viewModel.tracks.size()));
    }


    private boolean attemptSetupOfIndexesIfEmpty(){
        if(unplayedPathnameIndexes.isEmpty()){
            if(viewModel.tracks.isEmpty()){
                return false;
            }
            setupUnplayedIndexes();
        }
        return true;
    }


    public String getTrackNameAt(int position){
        return position >= viewModel.tracks.size() ? "" : viewModel.tracks.get(position).getName();
    }


    public int getNumberOfTracks(){
        return viewModel.tracks.size();
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
        return viewModel.tracks.get(currentIndex);
    }


    public int getCurrentTrackIndex(){
        return this.currentIndex;
    }


    public Track getTrackDetails(int index){
        if(index > viewModel.tracks.size()){
            return null;
        }
        return viewModel.tracks.get(index);
    }

    private int getNextRandomIndex(int listSize){
        return listSize < 1 ? 0 : random.nextInt(listSize -1);
    }


    public List<Track> getTracks(){
        return viewModel.tracks;
    }


    public void savePlaylist(){}


    private void loadPlaylist(){}


    private void log(String msg){
        Log.i("PlayListMngImpl", msg);
    }
}
