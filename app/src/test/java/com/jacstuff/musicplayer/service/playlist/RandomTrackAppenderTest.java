package com.jacstuff.musicplayer.service.playlist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.jacstuff.musicplayer.service.db.entities.Track;
import com.jacstuff.musicplayer.service.helpers.preferences.PreferencesHelper;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class RandomTrackAppenderTest {


    private RandomTrackAppender randomTrackAppender;
    private final int numberOfTracksToCopy = 5;
    private List<Track> target;
    private final MockPreferencesHelper preferencesHelper = new MockPreferencesHelper(numberOfTracksToCopy);

    @Before
    public void init(){
        randomTrackAppender = new RandomTrackAppender(preferencesHelper);
        target = new ArrayList<>();
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
        List<Track> source = createTracks(10);
        addTrackTo(target, "song_12,", "someArtist", 220L);
        int expectedSize = target.size() + numberOfTracksToCopy;

        int result = randomTrackAppender.addTracksFrom(source, target);
        assertEquals(expectedSize, target.size());
        assertEquals(numberOfTracksToCopy, result);
    }


    @Test
    public void differentTracksAreCopiedEachTime(){
        var source = createTracks(1000);

        var allTracksCopied = new HashSet<Track>();

        for(int i = 0; i < 100; i++){
            randomTrackAppender.addTracksFrom(source, target);
            allTracksCopied.addAll(target);
        }
        assertFalse(allTracksCopied.size() < 10);
    }


    @Test
    public void aTrackIsNotCopiedMoreThanOnce(){
        int expectedNumberOfTracks = 3;
        var source = createTracks(expectedNumberOfTracks);
        int result = randomTrackAppender.addTracksFrom(source, target);
        assertEquals(expectedNumberOfTracks, target.size());
        assertEquals(result, target.size());
    }


    @Test
    public void resultAlwaysMatchesNumberOfTracksCopied(){
        var source = createTracks(100);
        int result1 = randomTrackAppender.addTracksFrom(source, target);
        assertEquals(numberOfTracksToCopy, result1);
        int targetSize = target.size();
        int result2 = randomTrackAppender.addTracksFrom(source, target);
        int sizeDiff = target.size() - targetSize;
        assertEquals(sizeDiff, result2);
    }


    @Test
    public void aTrackIsNotCopiedIfItAlreadyExistsOnTarget(){
        int expectedNumberOfTracks = 2;
        var source = createTracks(expectedNumberOfTracks);
        target.addAll(source);
        assertEquals(expectedNumberOfTracks, target.size());
        for(int i = 0; i < 10; i++){
            randomTrackAppender.addTracksFrom(source, target);
        }
        assertEquals(expectedNumberOfTracks, target.size());
    }


    @Test
    public void canChangeNumberOfTracksToCopy(){
        int sourceTracks = 15;
        var source = createTracks(sourceTracks);
        randomTrackAppender.addTracksFrom(source, target);
        assertEquals(numberOfTracksToCopy, target.size());

        target.clear();
        int expectedTracksToCopy = 10;
        preferencesHelper.setNumberOfRandomTracksToAdd(expectedTracksToCopy);
        randomTrackAppender.addTracksFrom(source, target);
        assertEquals(expectedTracksToCopy, target.size());

        target.clear();
        expectedTracksToCopy = -1;
        preferencesHelper.setNumberOfRandomTracksToAdd(expectedTracksToCopy);
        randomTrackAppender.addTracksFrom(source, target);
        assertEquals(1, target.size());
    }


    private List<Track> createTracks(int numberOfTracksToCreate){
        List<Track> tracks = new ArrayList<>();
        for(int i = 1; i <= numberOfTracksToCreate; i++){
            var title = "song" + i;
            var artist = "artist" + i;
            addTrackTo(tracks, title, artist, i);
        }
        assertEquals(numberOfTracksToCreate, tracks.size());
        return tracks;
    }


    private void addTrackTo(List<Track> tracks, String title, String artist, long id ){
        tracks.add(createTrack(title, artist, id));
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
