package com.jacstuff.musicplayer.view.fragments.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.view.fragments.DialogFragmentUtils;
import com.jacstuff.musicplayer.view.utils.ButtonMaker;

public class AboutDialogFragment extends DialogFragment {

    public static AboutDialogFragment newInstance() {
        return new AboutDialogFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_about, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupButtons(view);
        setupFontInfo(view);
        DialogFragmentUtils.setScrollViewHeight(this, view, R.id.aboutInfoScrollView, R.id.aboutInfoLayout);
        DialogFragmentUtils.setTransparentBackground(this);
    }

    /*
        <string name="about_font_main_text">Main text:&#160;</string>
    <string name="about_font_main_text_font">Le Patin Magicien, Freeware \n https://www.fontspace.com/le-patin-magicien-font-f39622\n\n </string>
    <string name="about_font_time_elapsed">Time elapsed:&#160;</string>
    <string name="about_font_time_elapsed_font">Poe Monospace, Freeware\n https://www.fontspace.com/poe-monospace-font-f23572 </string>
     */

    private void setupFontInfo(View parentView){
        TextView fontInfoTextView = parentView.findViewById(R.id.aboutFontText);
        var fontMainText = getString(R.string.font_main);
        var fontMainTextFont = getString(R.string.font_main_detail);
        var fontTimeElapsed = getString(R.string.font_time);
        var fontTimeElapsedFont = getString(R.string.font_time_detail);

        var text = fontMainText + fontMainTextFont + fontTimeElapsed + fontTimeElapsedFont;
        fontInfoTextView.setText(text);
    }

    private void setupButtons(View parentView){
        ButtonMaker.setupButton(parentView, R.id.dismissAboutDialogButton, this::dismiss);
    }


}