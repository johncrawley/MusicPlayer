package com.jacstuff.musicplayer.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.provider.MediaStore;

import com.jacstuff.musicplayer.service.loader.CursorCreator;

import org.junit.Before;
import org.junit.Test;

public class CursorCreatorTest {

    private CursorCreator cursorCreator;

    @Before
    public void useAppContext() {
        cursorCreator = new CursorCreator();
    }


    @Test
    public void canCreateSelectionWithNoArgs(){
        String result =  cursorCreator.getSelection(null, null);
        assertNull(result);
        String result2 =  cursorCreator.getSelection("", "");
        assertNull(result2);
    }


    @Test
    public void canCreateSelectionWithIncludeArg(){
        String result =  cursorCreator.getSelection("/Music", "");
        String expected = "_data LIKE %?%";
        assertEquals(expected, result);

        String result2 =  cursorCreator.getSelection("/Music", null);
        assertEquals(expected, result2);
    }


    @Test
    public void canCreateSelectionWithExcludeArg(){
        String result =  cursorCreator.getSelection("", "Joplin");
        String expected = "instr(_data, ?) < 1";
        assertEquals(expected, result);

        String result2 =  cursorCreator.getSelection(null, "Joplin");
        assertEquals(expected, result2);
    }


    @Test
    public void canCreateSelectionWithBothArgs(){
        String result =  cursorCreator.getSelection("/Music", "Joplin");
        String expected = "_data LIKE %?% AND instr(_data, ?) < 1";
        assertEquals(expected, result);
    }


}