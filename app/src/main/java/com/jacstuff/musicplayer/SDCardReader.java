package com.jacstuff.musicplayer;


import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SDCardReader {

    private Context context;

    public SDCardReader(Context context){
        this.context  = context;
    }


    public List<String> listAllMusicFilesInPaths(String topDirPath, String musicDirectoryPath, String fileSuffix) {

        List<String> musicDirPaths = getAllMusicDirPaths(topDirPath, musicDirectoryPath);
        List<String> musicFilePaths = new ArrayList<>();

        for (String musicPath : musicDirPaths) {
            musicFilePaths.addAll(getFilePaths(new File(musicPath), fileSuffix));
        }
        listAudioFiles();
        return musicFilePaths;
    }


    public void listAudioFiles(){
        String[] projection1 = new String[] { MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.ALBUM_ART,
                MediaStore.Audio.Albums.NUMBER_OF_SONGS };

        String[] projection2 = new String[] {
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM};

        String selection = null;
        String[] selectionArgs = null;
        String sortOrder1 = MediaStore.Audio.Media.ALBUM + " ASC";
        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER + " ASC";
        //Cursor cursor1 = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection1, selection, selectionArgs, sortOrder1);
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection2, selection, selectionArgs, sortOrder);
        log("listAudioFiles() *************************** about to parse!");
        if(cursor == null){
            log("listAudioFiles() ************** cursor is null!");
            return;
        }

        while(cursor.moveToNext()){

            String displayName = getCol(cursor, MediaStore.Audio.Media.DISPLAY_NAME);
            String artist  = getCol(cursor, MediaStore.Audio.Media.ARTIST);
            String album  = getCol(cursor, MediaStore.Audio.Media.ALBUM);
            String title = getCol(cursor, MediaStore.Audio.Media.TITLE);


            log(" entry: " + displayName + " album: " + album + " artist: " + artist + " title: " + title);
        }
        cursor.close();
        log("listAudioFiles() *********************** exiting!");
    }

    private String getCol(Cursor cursor, String colName){
        int col = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
        return cursor.getString(col);
    }



    private List<String> getFilePaths(File topDir, String fileSuffix) {

        List<String> filePaths = new ArrayList<>();
        File[] currentDirFiles = topDir.listFiles();
        if (currentDirFiles != null) {

            for (File file : currentDirFiles) {
                if (file.isDirectory()) {
                    filePaths.addAll(getFilePaths(file, fileSuffix));
                } else {
                    if (file.getName().endsWith(fileSuffix)) {
                        filePaths.add(file.getAbsolutePath());
                    }

                }

            }

        }
        return filePaths;
    }


    private List<String> getAllMusicDirPaths(String topDirPath, final String musicSubDirPath) {

        List<String> musicPaths = new ArrayList<>();
        File topDir = new File(topDirPath);
        if (!isDirectoryValid(topDir)) {
            return musicPaths;
        }


        List<File> subdirs = Arrays.asList(topDir.listFiles());
        for (File subDir : subdirs) {
            String possibleMusicPath = subDir.getAbsolutePath() + musicSubDirPath;
            if (isDirectoryPathValid(possibleMusicPath)) {
                musicPaths.add(possibleMusicPath);
            }
        }

        return musicPaths;
    }

    private boolean isDirectoryPathValid(String path) {
        return isDirectoryValid(new File(path));
    }

    private boolean isDirectoryValid(File topDir) {
        if (!topDir.exists()) {
            log("isDirectoryValid() : dir " + topDir.getName() + " doesn't exist, exiting");
            return false;
        }

        if (!topDir.isDirectory()) {
            log("isDirectoryValid(), top dir isn't a directory, exiting.");
            return false;

        }
        File[] subDirArray = topDir.listFiles();
        if (subDirArray == null) {
            log("isDirectoryValid(), topDir has no files, listFiles() returned null. Exiting.");
            return false;
        }
        return true;
    }

    private void printAllMusicFilePaths(String topDirPath) {

        List<String> filepaths = listAllMusicFilesInPaths(topDirPath, "/Music", ".mp3");
        for (String path : filepaths) {
            log("music: " + path);
        }

    }



    private void log(String msg) {
        Log.i("musicmaker", msg);
    }

}