package com.jacstuff.musicplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import static com.jacstuff.musicplayer.HandlerCode.LOAD_BITMAPS;

public class ImageLoader implements Runnable {

    private List<String> imagePathnames;
    Handler handler;


    public ImageLoader(List<String> imagePathnames){
        this.imagePathnames = imagePathnames;
    }

    public void run(){
        if(imagePathnames.isEmpty()){
            return;
        }
        List<Bitmap> images = new ArrayList<>();
        for(String pathname : imagePathnames){
            File imgFile = new File(pathname);

            images.add(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));

        }
        //int index = ThreadLocalRandom.current().nextInt(imagePathnames.size());

        Message message = handler.obtainMessage(LOAD_BITMAPS, images );
        message.sendToTarget();
    }


}