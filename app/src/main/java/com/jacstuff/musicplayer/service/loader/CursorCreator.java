package com.jacstuff.musicplayer.service.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.jacstuff.musicplayer.service.helpers.PreferencesHelper;

public class CursorCreator {


    public Cursor createCursor(PreferencesHelper preferencesHelper, Context context){
        String includeArg = preferencesHelper.getPathsStr();
        String excludeArg = preferencesHelper.getExcludeStr();
        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER + " ASC";
        Uri collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);

        try {
            return context.getContentResolver().query(
                    collection,
                    createProjection(),
                    getSelection(includeArg, excludeArg),
                    createArgs(includeArg, excludeArg),
                    sortOrder);
        }catch (RuntimeException e){
            e.printStackTrace();
        }
        return null;
    }


    public String getSelection(String includeArg, String excludeArg){
        String includePath = includeArg == null ? "" : includeArg;
        String excludePath = excludeArg == null ? "" : excludeArg;

        if(excludePath.isBlank() && includePath.isBlank()){
            log("both include and exclude paths are blank");
            return null;
        }
        if(includePath.isBlank()){
            log("include path is blank, exclude path: " + excludePath);
            return getExcludeStr();
        }
        if(excludePath.isBlank()){
            log("exclude path is blank, include path: " + includePath);
            return getIncludeStr();
        }
        log("neither paths are blank, include: " + includePath + " exclude: " + excludePath);
        return getIncludeStr() + " AND " + getExcludeStr();
    }


    private void log(String msg){
        System.out.println("^^^ CursorLoader: " + msg);
    }

    private String[] createArgs(String... args){
        for(String arg : args){
            if(arg != null && !arg.isBlank()){
                break;
            }
            return null;
        }
        return args;
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
