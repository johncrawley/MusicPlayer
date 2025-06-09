package com.jacstuff.musicplayer.service.playlist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.jacstuff.musicplayer.service.db.entities.Track;

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
        var randomTracks = randomTrackAppender.getUniqueRandomTracksFrom(emptyList, destination);
        assertTrue(randomTracks.isEmpty());
    }


    @Test
    public void tracksAreAddedToDestinationList(){
        List<Track> source = createTracks(10);
        addTrackTo(target, "song_12,", "someArtist", 220L);

        var randomTracks = randomTrackAppender.getUniqueRandomTracksFrom(source, target);
        assertEquals(numberOfTracksToCopy, randomTracks.size());
    }


    @Test
    public void differentTracksAreCopiedEachTime(){
        var source = createTracks(1000);

        var allTracksCopied = new HashSet<Track>();

        for(int i = 0; i < 100; i++){
            var randomTracks = randomTrackAppender.getUniqueRandomTracksFrom(source, target);
            allTracksCopied.addAll(randomTracks);
        }
        assertFalse(allTracksCopied.size() < 10);
    }


    @Test
    public void aTrackIsNotCopiedMoreThanOnce(){
        int expectedNumberOfTracks = 3;
        var source = createTracks(expectedNumberOfTracks);
        var randomTracks = randomTrackAppender.getUniqueRandomTracksFrom(source, target);
        assertEquals(expectedNumberOfTracks, randomTracks.size());
    }


    @Test
    public void lastSourceTracksWillBeCopied(){
        int sourceSize = 20;
        int expectedSize = 4;
        int targetSize = sourceSize - expectedSize;
        var source = createTracks(sourceSize);
        var target = createTracks(targetSize);
        target.forEach(System.out::println);
        System.out.println("****************************");
        var randomTracks = randomTrackAppender.getUniqueRandomTracksFrom(source, target);
        randomTracks.forEach(System.out::println);
        assertEquals(expectedSize, randomTracks.size());
    }


    @Test
    public void aTrackIsNotCopiedIfItAlreadyExistsOnTarget(){
        int expectedNumberOfTracks = 2;
        var source = createTracks(expectedNumberOfTracks);
        target.addAll(source);
        assertEquals(expectedNumberOfTracks, target.size());
        for(int i = 0; i < 10; i++){
            randomTrackAppender.getUniqueRandomTracksFrom(source, target);
        }
        assertEquals(expectedNumberOfTracks, target.size());
    }


    @Test
    public void canChangeNumberOfTracksToCopy(){
        int sourceTracks = 15;
        var source = createTracks(sourceTracks);
        var randomTracks = randomTrackAppender.getUniqueRandomTracksFrom(source, target);
        assertEquals(numberOfTracksToCopy, randomTracks.size());

        int expectedTracksToCopy = 10;
        preferencesHelper.setNumberOfRandomTracksToAdd(expectedTracksToCopy);
        randomTracks = randomTrackAppender.getUniqueRandomTracksFrom(source, target);
        assertEquals(expectedTracksToCopy, randomTracks.size());

        expectedTracksToCopy = -1;
        preferencesHelper.setNumberOfRandomTracksToAdd(expectedTracksToCopy);
        randomTracks = randomTrackAppender.getUniqueRandomTracksFrom(source, target);
        assertEquals(1, randomTracks.size());
    }


    @Test
    public void canExplicitlyStateNumberOfTracksToCopy() {
        var source = createTracks(50);
        int firstAmountToCopy = 30;
        var randomTracks = randomTrackAppender.getUniqueRandomTracksFrom(source, target, firstAmountToCopy);
        assertEquals(firstAmountToCopy, randomTracks.size());

        int secondAmountToCopy = 42;
        randomTracks = randomTrackAppender.getUniqueRandomTracksFrom(source, target, secondAmountToCopy);
        assertEquals(secondAmountToCopy, randomTracks.size());
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
        return Track.Builder.newInstance().createTrackWithPathname("/" + title + ".mp3")
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
