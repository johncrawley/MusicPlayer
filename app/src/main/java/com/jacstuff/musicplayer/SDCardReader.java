package com.jacstuff.musicplayer;


import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SDCardReader {


    public List<String> listAllMusicFilesInPaths(String topDirPath, String musicDirectoryPath, String fileSuffix) {

        List<String> musicDirPaths = getAllMusicDirPaths(topDirPath, musicDirectoryPath);
        List<String> musicFilePaths = new ArrayList<>();

        for (String musicPath : musicDirPaths) {
            musicFilePaths.addAll(getFilePaths(new File(musicPath), fileSuffix));
        }
        return musicFilePaths;
    }


    public Map<String, List<String>> createThumbnailLists(String topDirPath, String musicDirectoryPath, String fileSuffix){
        Map<String, List<String>> thumbnailsMap = new HashMap<>();

        List<String> musicDirPaths = getAllMusicDirPaths(topDirPath, musicDirectoryPath);

        for(String path : musicDirPaths){

            thumbnailsMap.put(musicDirectoryPath, getFilePaths(new File(path), fileSuffix));
        }

        return thumbnailsMap;
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