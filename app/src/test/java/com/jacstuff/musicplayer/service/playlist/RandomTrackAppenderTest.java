package com.jacstuff.musicplayer.service.playlist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.jacstuff.musicplayer.service.db.entities.Track;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class RandomTrackAppenderTest {


    private RandomTrackAppender randomTrackAppender;

    @Before
    public void init(){
        randomTrackAppender = new RandomTrackAppender();
    }


    @Test
    public void generatesEmptyListIfInputEmpty(){
        List<Track> emptyList = List.of();
        List<Track> destination = new ArrayList<>();
        int result = randomTrackAppender.addTracksFrom(emptyList, destination);
        assertTrue(destination.isEmpty());
        assertEquals(0, result);
    }


    @Test
    public void tracksAreAddedToDestinationList(){
        List<Track> emptyList = List.of();
        List<Track> destination = new ArrayList<>();
        int result = randomTrackAppender.addTracksFrom(emptyList, destination);
        assertTrue(destination.isEmpty());
        assertEquals(0, result);
    }


    private List<Track> createTracks(){
        List<Track> tracks = new ArrayList<>();
        tracks.add(createTrack("song1", "artist1", 1L));
        return tracks;
    }


    private Track createTrack(String title, String artist, long id){
        return Track.Builder.newInstance().createTrackWithPathname("")
                .withArtist(artist)
                .withTitle(title)
                .withId(id)
                .withBitrate("")
                .withDisc("1")
                .withGenre("rock")
                .withIndex(1)
                .withYear("2000")
                .duration(100)
                .build();
    }

}
