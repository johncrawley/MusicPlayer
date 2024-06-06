package com.jacstuff.musicplayer;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.jacstuff.musicplayer.service.loader.CursorCreator;

import org.junit.Before;
import org.junit.Test;

public class CursorCreatorTest {

    private CursorCreator cursorCreator;

    @Before
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        cursorCreator = new CursorCreator(appContext);

    }

    @Test
    public void canCreateExcludeSelectionStr(){
        String excludedPath= "Janis";
        String result = cursorCreator.getExcludeStr(excludedPath);
        String expected = " AND instr(_data, 'Janis') < 1";
        System.out.println("result: " + result);
        System.out.println("expected: " + expected);
        assertEquals(expected, result);
    }


}