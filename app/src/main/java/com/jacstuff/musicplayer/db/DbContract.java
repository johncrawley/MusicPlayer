package com.jacstuff.musicplayer.db;

import android.provider.BaseColumns;

public final class DbContract {

    private DbContract(){}

    static class SongsEntry implements BaseColumns {
        static final String TABLE_NAME = "Songs";
        static final String COL_PATH = "path";
        static final String COL_NAME = "name";
        static final String COL_ALBUM = "album";
        static final String COL_ARTIST = "artist";
        static final String COL_LENGTH = "length";
    }

    static class PlaylistEntry implements BaseColumns {
        static final String TABLE_NAME = "Playlist";
        static final String COL_NAME = "name";
    }

    static class PlaylistItemsEntry implements BaseColumns {
        static final String TABLE_NAME = "Playlist_items";
        static final String COL_PLAYLIST_ID = "playlistId";
        static final String COL_SONG_ID = "songId";
    }

}
