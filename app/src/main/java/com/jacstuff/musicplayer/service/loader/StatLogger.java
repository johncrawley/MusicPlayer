package com.jacstuff.musicplayer.service.loader;

import android.database.Cursor;

public class StatLogger {

    private long startTime;

    public void start(){
        startTime = System.currentTimeMillis();
    }


    public void logLoadingStats(Cursor cursor){
        long duration = System.currentTimeMillis() - startTime;
        log("tracks loaded in " + duration + "ms number of total tracks: " + cursor.getCount());
    }


    private void log(String msg){
        System.out.println("^^^ StatLogger (For TrackLoader): " + msg);
    }
}
