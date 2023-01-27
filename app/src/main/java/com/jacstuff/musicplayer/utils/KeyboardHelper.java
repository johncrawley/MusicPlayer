package com.jacstuff.musicplayer.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class KeyboardHelper {

    private final Activity activity;

    public KeyboardHelper(Activity activity){
        this.activity = activity;
    }


    public void showKeyboardAndFocusOn(EditText editText){
        if(editText == null){
            return;
        }
        editText.requestFocus();
        editText.postDelayed(()->showKeyboard(editText), 200);
    }


    public void hideKeyboard(View view){
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    private void showKeyboard(EditText editText){
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

}
