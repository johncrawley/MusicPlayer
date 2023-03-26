package com.jacstuff.musicplayer.view.search;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;


public class KeyListenerHelper {

    public static void setListener(EditText editText, Runnable runnable){
        editText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {
                runnable.run();
            }
        });
    }
}
