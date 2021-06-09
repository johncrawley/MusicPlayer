package com.jacstuff.musicplayer.playlist;

import android.content.Context;
import android.util.Log;

import com.jacstuff.musicplayer.SDCardReader;
import com.jacstuff.musicplayer.TrackDetails;
import com.jacstuff.musicplayer.TrackDetailsParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class PlaylistManagerImpl implements PlaylistManager {

    private final File playlistFile;
    private final String playlistFilename = "playlist.dat";
    private List<String> pathnames;
    private List<Integer> unplayedPathnameIndexes;
    private int currentIndex = 0;
    private SDCardReader sdCardReader;
    private boolean doesPlaylistFileExist;
    private TrackDetailsParser trackDetailsParser;
    private final int INITIAL_LIST_CAPACITY = 10_000;
    private Context context;
    private Map<String, List<String>> thumbnailPathsMap;
    private String topDir = "/storage";
    private String musicDir = "/Music";
    private String currentTrackDirectory; //used for assigning the current thumbnails playlist
    private List<TrackDetails> trackDetailsList;


    public PlaylistManagerImpl(Context context){

        this.context = context;
        playlistFile = new File(context.getFilesDir().getAbsolutePath() + playlistFilename);
        initPlaylistFile();
        pathnames = new ArrayList<>(INITIAL_LIST_CAPACITY);
        unplayedPathnameIndexes = new ArrayList<>();
        sdCardReader = new SDCardReader();
        trackDetailsParser = new TrackDetailsParser();
        thumbnailPathsMap = sdCardReader.createThumbnailLists( topDir, musicDir,".jpg");
        initTrackDetailsList();
    }

    private void initTrackDetailsList(){
        trackDetailsList = new ArrayList<>();
        Log.i("PlaylistMngrImpl", "pathnames count : " + pathnames.size());
        for(String pathname : pathnames){
            trackDetailsList.add(trackDetailsParser.parse(pathname));
        }
    }

    private void initPlaylistFile(){
        try{
            if(!playlistFile.exists()){
                playlistFile.createNewFile();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void setupUnplayedIndexes(){
        unplayedPathnameIndexes = new ArrayList<>(INITIAL_LIST_CAPACITY);
        for(int i=0; i< pathnames.size(); i++){
            unplayedPathnameIndexes.add(i);
        }
    }


    public void init(){
    log("Entering init()");
        if(!playlistFile.exists()) {
            log("Playlist file doesn't exist.");
            createPlaylistFileIfItDoesntExist();
            setupPlaylists();
            savePlaylist();
            initTrackDetailsList();
            return;
        }
        loadPlaylist();
        if(pathnames.isEmpty()){
            setupPlaylists();
            savePlaylist();
            initTrackDetailsList();
        }
    }

    public void refreshPlaylist(){
       createPlaylistFileIfItDoesntExist();
       setupPlaylists();
       savePlaylist();
       initTrackDetailsList();
       log("playlist refreshed, found  " + pathnames.size() + " tracks");
    }

    private void createPlaylistFileIfItDoesntExist(){

        if(!playlistFile.exists()) {
            try {
                doesPlaylistFileExist = playlistFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                log("Couldn't create the playlist file");
            }
        }
    }


    private void setupPlaylists(){
        log("Entered setupPlaylists()");
        pathnames = sdCardReader.listAllMusicFilesInPaths(topDir, musicDir, ".mp3");
        setupUnplayedIndexes();
    }


    public String getNext(){
        if(currentIndex == pathnames.size() -1){
            currentIndex = 0;
        }
        return pathnames.get(++currentIndex);
    }

    public String getNextRandom(){
        if(pathnames.isEmpty()){
            log("No pathnames!");
            return null;
        }
        currentIndex =  getNextRandomIndex(pathnames.size());

        return pathnames.get(currentIndex);

    }

    private boolean attemptSetupOfIndexesIfEmpty(){
        if(unplayedPathnameIndexes.isEmpty()){
            if(pathnames.isEmpty()){
                log("no pathnames to be found.");
                return false;
            }
            log("Unplayed index is empty, setting up again...");
            setupUnplayedIndexes();
            log("Unplayed index has been set up, current size : " + unplayedPathnameIndexes.size());
        }
        else{
            log("unplayed index is still not empty, current size: " + unplayedPathnameIndexes.size());
        }

        return true;
    }

    public String getTrackNameAt(int position){

        if(position >= pathnames.size()){
            return "";
        }
        return getTrack(pathnames.get(position)).getName();

    }


    public int getNumberOfTracks(){
        return pathnames.size();
    }

    private TrackDetails getTrack(String pathname){
        return trackDetailsParser.parse(pathname);
    }

    private String getNextRandomUnplayed(){
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
        //logUnplayedIndexes();
        return pathnames.get(currentIndex);
    }

    public int getCurrentTrackIndex(){
        return this.currentIndex;
    }

    public List<String> getThumbnailPathsForCurrentTrack(){

        return thumbnailPathsMap.get(currentTrackDirectory);
    }

    public TrackDetails getTrackDetails(int index){
        if(index > pathnames.size()){
            return null;
        }
        return trackDetailsParser.parse(pathnames.get(index));
    }


    private void logUnplayedIndexes(){

        String unplayedIndexes = "size:" + unplayedPathnameIndexes.size();
        StringBuilder str = new StringBuilder(unplayedIndexes);

        for(int i=0; i < unplayedPathnameIndexes.size();i++){
            str.append(" ");
            str.append(i);
            str.append(":");
            str.append(unplayedPathnameIndexes.get(i));
            str.append(",");
        }
        log(str.toString());

    }

    private int getNextRandomIndex(int listSize){
        if(listSize < 1){
            return 0;
        }
        return ThreadLocalRandom.current().nextInt(listSize -1);
    }

    public TrackDetails getNextRandomUnplayedTrack(){
        String pathname = getNextRandomUnplayed();
        TrackDetails trackDetails = trackDetailsParser.parse(pathname);
        if(trackDetails == null){
            log("getNextRandomUnplayedTrack() , pathname was : " + pathname + " but track details object is null.");
            return  null;
        }
        currentTrackDirectory = trackDetails.getDirectory();
        return trackDetails;
    }

    public List<TrackDetails> getTrackDetailsList(){

        return this.trackDetailsList;

    }

    public TrackDetails getNextRandomTrack(){
            String pathname = getNextRandom();
        return trackDetailsParser.parse(pathname);
    }


    public void savePlaylist(){

        if(!doesPlaylistFileExist){
            return;
        }
        try( OutputStream outputStream = context.openFileOutput(playlistFilename, Context.MODE_PRIVATE);){

            PrintWriter p = new PrintWriter(outputStream);
            for (String pathname : pathnames) {
                p.write(pathname);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPlaylist() {
        log("Entering loadPlaylist()");
        if(!doesPlaylistFileExist){
            return;
        }
        int linesRead = 0;

        try(InputStream inputStream = context.openFileInput(playlistFilename); ) {
          InputStreamReader streamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(streamReader);
            String line;
            line = reader.readLine();
            while (line != null) {
                pathnames.add(line);
                line = reader.readLine();
                linesRead++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log("loadPlaylistFromFile() - lines read: " + linesRead);
    }


    private void log(String msg){
        Log.i("PlayListMngImpl", msg);
    }
}
