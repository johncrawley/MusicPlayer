package com.jacstuff.musicplayer.settings;

import android.text.InputFilter;
import android.text.Spanned;

public class MinNumberInputFilter implements InputFilter {

    private final int min, max;

    public MinNumberInputFilter(int min, int max){
        this.min = min;
        this.max = max;
    }

    @Override
    public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
        try {
            int input = Integer.parseInt(spanned.toString() + charSequence.toString());
            if (isWithinRange(input))
                return null;
        } catch (NumberFormatException nfe) {
            System.out.println(" MinNumberInputFilter: " +  nfe.getMessage());
        }
        return "";
    }


    public boolean isWithinRange(int number){
        return number >= min && number <= max;
    }

}
