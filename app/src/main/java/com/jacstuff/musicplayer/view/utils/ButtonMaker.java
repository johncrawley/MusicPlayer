package com.jacstuff.musicplayer.view.utils;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class ButtonMaker {

    public static Button createButton(View parentView, int id, Runnable onClickAction){
        Button button = parentView.findViewById(id);
        if(button == null){
            return null;
        }
        button.setOnClickListener((View v)-> onClickAction.run());
        return button;
    }

    public static ImageButton createImageButton(View parentView, int id, Runnable onClickAction){
        ImageButton button = parentView.findViewById(id);
        if(button == null){
            return null;
        }
        button.setOnClickListener((View v)-> onClickAction.run());
        return button;
    }
}
