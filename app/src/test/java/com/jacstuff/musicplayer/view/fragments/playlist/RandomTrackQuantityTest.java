package com.jacstuff.musicplayer.view.fragments.playlist;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class RandomTrackQuantityTest {

    private RandomTrackQuantity randomTrackQuantity;

    @Before
    public void init(){
        randomTrackQuantity = new RandomTrackQuantity();
    }

    @Test
    public void canIncrementValue(){
        assertEquals(RandomTrackQuantity.INITIAL_VALUE, randomTrackQuantity.get());
        randomTrackQuantity.increment();
        assertEquals(5, randomTrackQuantity.get());
        randomTrackQuantity.increment();
        assertEquals(10, randomTrackQuantity.get());
        randomTrackQuantity.increment();
        assertEquals(20, randomTrackQuantity.get());
        randomTrackQuantity.increment();
        assertEquals(30, randomTrackQuantity.get());

        randomTrackQuantity.increment();
        randomTrackQuantity.increment();
        randomTrackQuantity.increment();
        randomTrackQuantity.increment();
        assertEquals(70, randomTrackQuantity.get());
    }


    @Test
    public void doesNotExceedMaxValue(){
        assertEquals(RandomTrackQuantity.INITIAL_VALUE, randomTrackQuantity.get());
        for(int i = 0; i < 100; i++){
            randomTrackQuantity.increment();
        }
        assertEquals(RandomTrackQuantity.MAX_VALUE, randomTrackQuantity.get());
    }


    @Test
    public void canDecrementValue(){
        assertEquals(RandomTrackQuantity.INITIAL_VALUE, randomTrackQuantity.get());
        randomTrackQuantity.decrement();
        assertEquals(RandomTrackQuantity.INITIAL_VALUE, randomTrackQuantity.get());
        randomTrackQuantity.increment(); // 5
        randomTrackQuantity.increment(); // 10
        randomTrackQuantity.increment(); // 20
        randomTrackQuantity.decrement();
        assertEquals(10, randomTrackQuantity.get());
        randomTrackQuantity.decrement();
        assertEquals(5, randomTrackQuantity.get());
        randomTrackQuantity.decrement();
        assertEquals(1, randomTrackQuantity.get());
        randomTrackQuantity.decrement();
        assertEquals(1, randomTrackQuantity.get());
        randomTrackQuantity.decrement();
        assertEquals(1, randomTrackQuantity.get());

    }
}
