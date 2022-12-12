package com.jacstuff.musicplayer.playlist;

import android.content.Context;

import com.jacstuff.musicplayer.MediaPlayerView;
import com.jacstuff.musicplayer.db.track.Track;
import com.jacstuff.musicplayer.db.track.TrackRepository;
import com.jacstuff.musicplayer.db.track.TrackRepositoryImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class PlaylistManagerImpl implements PlaylistManager {

    private int currentIndex = 0;
    private final AudioInfoLoader sdCardReader;
    private final Random random;
    private final TrackRepository trackRepository;
    private int previousNumberOfTracks;
    private MediaPlayerView mediaPlayerView;
    private List<Track> tracks;
    private List<Integer> unPlayedPathnameIndexes;


    public PlaylistManagerImpl(Context context){
        trackRepository = new TrackRepositoryImpl(context);
        tracks = new ArrayList<>();
        unPlayedPathnameIndexes = new ArrayList<>();
        random = new Random(System.currentTimeMillis());
        sdCardReader = new AudioInfoLoader(context, trackRepository);
        initTrackDetailsList();
        previousNumberOfTracks = tracks.size();
        trackHistory = new ArrayList<>();
    }


    public void setMediaPlayerView(MediaPlayerView mediaPlayerView){
        this.mediaPlayerView = mediaPlayerView;
    }



    @Override
    public void addTracksFromStorage(){
        sdCardReader.loadAudioFiles();
        initTrackDetailsList();
        calculateAndPostNewTracksStats();
    }


    @Override
    public Track getNextRandomTrack(){
        return  tracks.isEmpty() ? null : tracks.get(getNextRandomIndex(tracks.size()));
    }


    private void calculateAndPostNewTracksStats(){
        int numberOfNewTracks = tracks.size() - previousNumberOfTracks;
        if(numberOfNewTracks > 0){
            mediaPlayerView.displayPlaylistRefreshedMessage(numberOfNewTracks);
        }
        previousNumberOfTracks = tracks.size();
    }


    public void init(){
        initTrackDetailsList();
    }


    private void initTrackDetailsList(){
        tracks = trackRepository.getAllTracks();
    }


    public String getNext(){
        if(currentIndex == tracks.size() -1){
            currentIndex = 0;
        }
        return tracks.get(++currentIndex).getName();
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

    private int currentHistoryIndex;
    private List<Track> trackHistory;


    @Override
    public Track getPreviousTrack(){
        printTrackHistory();
        currentHistoryIndex = Math.max(0, currentHistoryIndex-1);
        return trackHistory.get(currentHistoryIndex);
    }

    private void printTrackHistory(){
        StringBuilder str = new StringBuilder("Track History ==> ");
        for(Track track : trackHistory){
            str.append(" :: ");
            str.append(track.getName());
        }
        System.out.println(str);
    }

    public Track getNextRandomUnPlayedTrack(){
        if(isHistoryIndexOld()){
            currentHistoryIndex++;
            return trackHistory.get(currentHistoryIndex);
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
        currentHistoryIndex++;
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


    public int getCurrentTrackIndex(){
        return this.currentIndex;
    }


    public Track selectTrack(int index){
        if(index > tracks.size()){
            return null;
        }
        if(isHistoryIndexOld()){
            removeAllHistoryAfterCurrentIndex();
        }
        Track track = tracks.get(index);
        trackHistory.add(track);
        currentHistoryIndex++;
        return tracks.get(index);
    }

    private boolean isHistoryIndexOld(){
        return currentHistoryIndex < trackHistory.size() -1;
    }

    private void removeAllHistoryAfterCurrentIndex(){
        trackHistory = trackHistory.subList(0, currentHistoryIndex);

    }

    private int getNextRandomIndex(int listSize){
        return listSize < 2 ? 0 : random.nextInt(listSize);
    }


    public List<Track> getTracks(){
        return tracks;
    }


    public void savePlaylist(){}

}
