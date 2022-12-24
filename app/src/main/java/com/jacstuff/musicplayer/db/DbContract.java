package com.jacstuff.musicplayer.db;

import android.provider.BaseColumns;

public final class DbContract {

    private DbContract(){}

    public static class TracksEntry implements BaseColumns {
        public static final String TABLE_NAME = "Tracks";
        public static final String COL_PATH = "path";
        public static final String COL_NAME = "name";
        public static final String COL_ALBUM = "album";
        public static final String COL_ARTIST = "artist";
        public static final String COL_ARTIST_ID = "artist_id";
        public static final String COL_TRACK_NUMBER = "track_number";
        public static final String COL_GENRE = "genre";
        public static final String COL_DURATION = "duration";
        //static final String COL_LENGTH = "length";
    }


    public static class AlbumsEntry implements BaseColumns {
        static final String TABLE_NAME = "Albums";
        static final String COL_NAME = "name";
    }


    public static class ArtistsEntry implements BaseColumns {
        public static final String TABLE_NAME = "Artists";
        public static final String COL_NAME = "name";
    }


    public static class PlaylistEntry implements BaseColumns {
        public static final String TABLE_NAME = "Playlist";
        public static final String COL_NAME = "name";
    }


    public static class PlaylistItemsEntry implements BaseColumns {
       public static final String TABLE_NAME = "Playlist_items";
       public static final String COL_PLAYLIST_ID = "playlistId";
       public static final String COL_TRACK_ID = "songId";
    }

}
