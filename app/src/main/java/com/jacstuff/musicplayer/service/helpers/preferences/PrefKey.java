package com.jacstuff.musicplayer.service.helpers.preferences;

public enum PrefKey {

    IS_SEARCH_DISMISSED_AFTER_SELECTION ("dismissSearchAfterSelection", true),
    IS_SIMPLE_SEARCH_ENABLED("simpleSearchView", true),
    ARE_ONLY_ALBUM_TRACKS_FROM_SELECTED_ARTIST_SHOWN("onlyShowAlbumTracksFromSelectedArtist", true),
    ARE_TABS_SWITCHED_AFTER_PLAYLIST_SELECTION("autoSwitchTabsAfterPlaylistSelection", true),
    IS_NEXT_TRACK_SELECTED_AFTER_PLAYLIST_LOADED("autoNextTrackOnPlaylistLoaded", false),
    NUMBER_OF_RANDOM_TRACKS_TO_ADD("numberOfRandomTracksToAdd"),
    ARE_DUPLICATE_TRACKS_IGNORED("ignoreDuplicateTracks", true),
    TRACKS_PATH_STR("tracksPathnameString_1"),
    EXCLUDED_TRACKS_PATH_STR("excludeTracksWithPathname"),
    IS_SHUFFLE_ENABLED("isShuffleEnabled", false);


    private final String keyStr;
    private final boolean defaultBool;


    PrefKey(String key){
        this(key, false);
    }


    PrefKey(String key, boolean defaultB){
        keyStr = key;
        defaultBool = defaultB;
    }


    public boolean getDefault(){
        return defaultBool;
    }


    public String getKeyStr(){
        return keyStr;
    }

}
