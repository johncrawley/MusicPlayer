package com.jacstuff.musicplayer.service.loader;

import static com.jacstuff.musicplayer.service.helpers.preferences.PrefKey.EXCLUDED_TRACKS_PATH_STR;
import static com.jacstuff.musicplayer.service.helpers.preferences.PrefKey.TRACKS_PATH_STR;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.jacstuff.musicplayer.service.helpers.preferences.PreferencesHelperImpl;

public class CursorCreator {


    public Cursor createCursor(PreferencesHelperImpl preferencesHelper, Context context){
        String includeArg = preferencesHelper.getStr(TRACKS_PATH_STR);
        String excludeArg = preferencesHelper.getStr(EXCLUDED_TRACKS_PATH_STR);
        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER + " ASC";

        return context.getContentResolver().query(
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
                createProjection(),
                getSelection(includeArg, excludeArg),
                createArgs(includeArg, excludeArg),
                sortOrder);
    }


    public String getSelection(String includeArg, String excludeArg){
        String includePath = includeArg == null ? "" : includeArg;
        String excludePath = excludeArg == null ? "" : excludeArg;

        if(excludePath.isBlank() && includePath.isBlank()){
            return null;
        }
        if(includePath.isBlank()){
            return getExcludeStr();
        }
        if(excludePath.isBlank()){
            return getIncludeStr();
        }
        return getIncludeStr() + " AND " + getExcludeStr();
    }


    private String[] createArgs(String includeArg, String excludeArg){
        String include = includeArg == null ? "" : includeArg;
        String exclude = excludeArg == null ? "" : excludeArg;
        if(include.isBlank() && exclude.isBlank()){
            return null;
        }
        if(!include.isBlank() && !exclude.isBlank()){
            return new String[]{"%" + include + "%", exclude};
        }
        if(!include.isBlank()){
            return new String[]{"%" + include + "%"};
        }
        return new String[]{exclude};
    }


    private String getIncludeStr(){
       return MediaStore.Audio.Media.RELATIVE_PATH + " LIKE ?";
    }


    private String getExcludeStr(){
       return  "instr(" + MediaStore.Audio.Media.RELATIVE_PATH + ", ?) < 1";
    }


    private String[] createProjection() {
        return new String[]{
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.RELATIVE_PATH,
                MediaStore.Audio.Media.CD_TRACK_NUMBER,
                MediaStore.Audio.Media.GENRE,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.BITRATE,
                MediaStore.Audio.Media.DISC_NUMBER
        };
    }

}
