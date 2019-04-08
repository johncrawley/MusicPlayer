package com.jacstuff.musicplayer;

import java.util.concurrent.TimeUnit;

public class TimeConverter {


    public static String convert(long millis) {

        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));

        return String.format("%d:%02d", minutes, seconds );

    }
}
