package com.jacstuff.musicplayer.playlist;

import android.content.Context;

import com.jacstuff.musicplayer.MediaPlayerView;
import com.jacstuff.musicplayer.db.track.Track;
import com.jacstuff.musicplayer.db.track.TrackRepository;
import com.jacstuff.musicplayer.db.track.TrackRepositoryImpl;
import com.jacstuff.musicplayer.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class PlaylistManagerImpl implements PlaylistManager {

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
        sdCardReader = new AudioInfoLoader(context, trackRepository, viewModel);
        initUnPlayedList();
        initTrackDetailsList();
        previousNumberOfTracks = viewModel.tracks.size();
    }


    private void initUnPlayedList(){
       if(viewModel.unplayedPathnameIndexes == null){
           viewModel.unplayedPathnameIndexes = new ArrayList<>();
       }
    }


    @Override
    public void addTracksFromStorage(){
        sdCardReader.loadAudioFiles();
        initTrackDetailsList();
        calculateAndPostNewTracksStats();
    }


    @Override
    public Track getNextRandomTrack(){
        return viewModel.tracks.isEmpty() ? null : viewModel.tracks.get(getNextRandomIndex(viewModel.tracks.size()));
    }



    private void calculateAndPostNewTracksStats(){
        int numberOfNewTracks = viewModel.tracks.size() - previousNumberOfTracks;
        if(numberOfNewTracks > 0){
            mediaPlayerView.displayPlaylistRefreshedMessage(numberOfNewTracks);
        }
        previousNumberOfTracks = viewModel.tracks.size();
    }


    public void init(){
        initTrackDetailsList();
    }


    private void initTrackDetailsList(){
        viewModel.tracks = trackRepository.getAllTracks();
        mediaPlayerView.updateTrackDetails();
    }


    public String getNext(){
        if(currentIndex == viewModel.tracks.size() -1){
            currentIndex = 0;
        }
        return viewModel.tracks.get(++currentIndex).getName();
    }


    private void setupUnplayedIndexes(){
        final int INITIAL_LIST_CAPACITY = 10_000;
        viewModel.unplayedPathnameIndexes = new ArrayList<>(INITIAL_LIST_CAPACITY);
        for(int i = 0; i< viewModel.tracks.size(); i++){
           viewModel.unplayedPathnameIndexes.add(i);
        }
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
        int freshSongsCount = viewModel.unplayedPathnameIndexes.size();


        int randomIndex = getNextRandomIndex(freshSongsCount);
        currentIndex = getAndRemoveSongIndex(randomIndex);
        attemptSetupOfIndexesIfEmpty();

        return viewModel.tracks.get(currentIndex);
    }


    private int getAndRemoveSongIndex(int index){
        int songIndex = viewModel.unplayedPathnameIndexes.get(index);
        viewModel.unplayedPathnameIndexes.remove(index);
        return songIndex;
    }


    private boolean attemptSetupOfIndexesIfEmpty(){
        if(viewModel.unplayedPathnameIndexes.isEmpty()){
            if(viewModel.tracks.isEmpty()){
                return false;
            }
            setupUnplayedIndexes();
        }
        return true;
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
        return listSize < 2 ? 0 : random.nextInt(listSize);
    }


    public List<Track> getTracks(){
        return viewModel.tracks;
    }


    public void savePlaylist(){}

}
