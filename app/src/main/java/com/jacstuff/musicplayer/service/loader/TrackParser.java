package com.jacstuff.musicplayer.service.loader;

import android.database.Cursor;
import android.provider.MediaStore;

import com.jacstuff.musicplayer.service.db.entities.Track;

import java.util.HashMap;
import java.util.Map;

public class TrackParser {


    private Map<String, Integer> columnMap;

    public TrackParser(Cursor cursor){
        setupColumnMap(cursor);
    }


    public Track parseTrackFrom(Cursor cursor) {
        return new Track(
                getStr(cursor, MediaStore.Audio.Media.DATA),
                getStr(cursor, MediaStore.Audio.Media.TITLE),
                getStr(cursor, MediaStore.Audio.Media.ARTIST),
                getStr(cursor, MediaStore.Audio.Media.ALBUM),
                getDiscNumber(cursor),
                getGenre(cursor),
                getStr(cursor, MediaStore.Audio.Media.YEAR),
                getIntValueFrom(cursor, MediaStore.Audio.Media.DURATION),
                getTrackNumber(cursor),
                getBitrate(cursor));
    }


    private void setupColumnMap(Cursor cursor){
        columnMap = new HashMap<>();
        addToColumnMap(cursor, MediaStore.Audio.Media.DATA);
        addToColumnMap(cursor, MediaStore.Audio.Media.ARTIST);
        addToColumnMap(cursor, MediaStore.Audio.Media.ALBUM);
        addToColumnMap(cursor, MediaStore.Audio.Media.TITLE);
        addToColumnMap(cursor, MediaStore.Audio.Media.DURATION);
        addToColumnMap(cursor, MediaStore.Audio.Media.CD_TRACK_NUMBER);
        addToColumnMap(cursor, MediaStore.Audio.Media.GENRE);
        addToColumnMap(cursor, MediaStore.Audio.Media.YEAR);
        addToColumnMap(cursor, MediaStore.Audio.Media.BITRATE);
        addToColumnMap(cursor, MediaStore.Audio.Media.DISC_NUMBER);
        addToColumnMap(cursor, MediaStore.Audio.Media.RELATIVE_PATH);
    }


    private void addToColumnMap(Cursor cursor, String str){
        columnMap.put(str, cursor.getColumnIndexOrThrow(str));
    }



    private String getDiscNumber(Cursor cursor){
        return String.valueOf(getDiscNumberFrom(cursor));
    }


    private String getGenre(Cursor cursor){
        String genre = "";
        genre = getStr(cursor, MediaStore.Audio.Media.GENRE);
        return genre == null ? "" : genre;
    }


    private String getBitrate(Cursor cursor){
        int bitrate = getIntValueFrom(cursor, MediaStore.Audio.Media.BITRATE);
        if(bitrate > 1000){
            int kbps = bitrate / 1000;
            return kbps + "kbps";
        }
        return String.valueOf(bitrate);
    }


    private int getTrackNumber(Cursor cursor){
        return getIntValueFrom(cursor, MediaStore.Audio.Media.CD_TRACK_NUMBER);
    }

    @SuppressWarnings("ConstantConditions")
    private String getStr(Cursor cursor, String colName){
        return cursor.getString(columnMap.get(colName));
    }


    @SuppressWarnings("ConstantConditions")
    private int getIntValueFrom(Cursor cursor, String colName){
        return (int)cursor.getLong(columnMap.get(colName));
    }


    private long getDiscNumberFrom(Cursor cursor){
        Integer name = columnMap.get(MediaStore.Audio.Media.DISC_NUMBER);
        if(name == null){
            return -1;
        }
        return Math.max(1, cursor.getLong(name));
    }

}
