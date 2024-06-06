package com.jacstuff.musicplayer.service.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.jacstuff.musicplayer.service.helpers.PreferencesHelper;

public class CursorCreator {

   private  PreferencesHelper preferencesHelper;
   private Context context;


   public CursorCreator(Context context){
       preferencesHelper = new PreferencesHelper(context);
       this.context = context;
   }

    public Cursor createCursor(){
        String includePath = preferencesHelper.getPathsStr();
        String[] args = new String[]{"%" +  includePath + "%"};
        String pathname = MediaStore.Audio.Media.DATA;
        String selection = pathname + " LIKE ?";
        //log("createCursor() selection str: " + selection);
        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER + " ASC";
        Uri collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);

        return context.getContentResolver().query(
                collection,
                createProjection(),
                selection,
                args,
                sortOrder);
    }



    public String getSelectionWithExclude(String selectionStr, String includePath){
        String excludePath = preferencesHelper.getExcludeStr();
        String andStr = includePath.trim().isEmpty() ? "" : " AND ";

        //  String selectionWithExclude = excludePath.trim().isEmpty() ? selection : selection + "AND instr(" + pathname + ",'" + excludePath + "') < 1";
        return "";
    }


    public String getSelection(String includePath, String excludePath){
        String pathname = MediaStore.Audio.Media.DATA;
        String selection = pathname + " LIKE ?";
       if(excludePath.isBlank()){
           if(includePath.isBlank()){
               return null;
           }
           return selection;
       }
       return selection + getExcludeStr(excludePath);
    }


    public String getExcludeStr(String excludePath){
       return  " AND instr(" + MediaStore.Audio.Media.DATA + ",'" + excludePath + "') < 1";
    }


    private String[] createProjection() {
        return new String[]{
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.CD_TRACK_NUMBER,
                MediaStore.Audio.Media.GENRE,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.BITRATE,
                MediaStore.Audio.Media.DISC_NUMBER
        };
    }

}
