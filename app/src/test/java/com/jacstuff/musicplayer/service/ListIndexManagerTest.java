package com.jacstuff.musicplayer.service;

import static junit.framework.TestCase.assertEquals;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ListIndexManagerTest {

    private ListIndexManager lim;
    private IndexMethods artist, album, genre, playlist;


    @Before
    public void init(){
        lim = new ListIndexManager();
        artist = new IndexMethods(lim::setArtistIndex, lim::getArtistIndex);
        album = new IndexMethods(lim::setAlbumIndex, lim::getAlbumIndex);
        genre = new IndexMethods(lim::setGenreIndex, lim::getGenreIndex);
        playlist = new IndexMethods(lim::setPlaylistIndex, lim::getPlaylistIndex);
    }


    @Test
    public void canSetPlaylistIndex(){
        assertIndex(playlist, 1);
        assertIndex(playlist, 2);;
    }

    @Test
    public void canSetArtistIndex(){
        assertIndex(artist, 1);
        assertIndex(artist, 2);
    }


    @Test
    public void canResetAllIndexes(){
        lim.setArtistIndex(5);
        lim.resetAllIndexes();
        assertEmptyIndex(lim::getArtistIndex);

        lim.setPlaylistIndex(3);
        lim.resetAllIndexes();
        assertEmptyIndex(lim::getPlaylistIndex);

        lim.setAlbumIndex(2);
        lim.resetAllIndexes();
        assertEmptyIndex(lim::getAlbumIndex);

        lim.setGenreIndex(1);
        lim.resetAllIndexes();
        assertEmptyIndex(lim::getGenreIndex);

    }


    private void assertEmptyIndex(Supplier<Optional<Integer>> supplier){
        assertFalse(supplier.get().isPresent());
    }


    @Test
    public void canSetAlbumIndex(){
        assertIndex(album, 1);
        assertIndex(album, 2);
        assertStatusOf(artist, lim::setAlbumIndex, true);
    }


    @Test
    public void canSetGenreIndex(){
        assertIndex(genre, 1);
        assertIndex(genre, 2);;
    }


    @Test
    public void settingPlaylistWillUnsetOtherIndexes(){
        assertUnsetOfIndexesAfterSetting(playlist, artist, album, genre);
    }


    @Test
    public void settingArtistWillUnsetOtherIndexes(){
        assertUnsetOfIndexesAfterSetting(artist, album, genre, playlist);
    }


    @Test
    public void settingAlbumWillUnsetOtherIndexes(){
        assertUnsetOfIndexesAfterSetting(album, playlist, genre);
    }


    @Test
    public void settingGenreWillUnsetOtherIndexes(){
        assertUnsetOfIndexesAfterSetting(genre, playlist, artist, album);
    }


    private void assertUnsetOfIndexesAfterSetting(IndexMethods indexToSet, IndexMethods... expectedUnsetIndexes){
        for(IndexMethods indexMethods : expectedUnsetIndexes){
            assertUnsetStatus(indexMethods, indexToSet.setter);
        }
    }


    private record IndexMethods(Consumer<Integer> setter, Supplier<Optional<Integer>> getter){ }


    // when the first supplier is set (e.g. setAlbum), and the second supplier is set (e.g. setGenre)
    // then the getter corresponding to the first supplier (i.e. getAlbum) should return an non-present optional.
    private void assertUnsetStatus(IndexMethods indexMethods, Consumer<Integer> consumer2){
        assertStatusOf(indexMethods, consumer2, false);
    }


    private void assertStatusOf(IndexMethods indexMethods, Consumer<Integer> consumer2, boolean expected){
        indexMethods.setter.accept(1);
        consumer2.accept(1);
        assertEquals(expected, indexMethods.getter.get().isPresent());
    }


    private void assertIndex(IndexMethods indexMethods, int index){
        indexMethods.setter.accept(index);
        Optional<Integer> savedIndex = indexMethods.getter.get();
        assertTrue(savedIndex.isPresent());
        int savedValue = savedIndex.get();
        assertEquals(index, savedValue);
    }
}
