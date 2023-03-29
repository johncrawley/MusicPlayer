package com.jacstuff.musicplayer.service.db;

import android.provider.BaseColumns;

public final class DbContract {

    private DbContract(){}

    public static class TracksEntry implements BaseColumns {
        public static final String TABLE_NAME = "Tracks";
        public static final String COL_PATH = "track_path";
        public static final String COL_TITLE = "track_title";
        public static final String COL_ALBUM = "track_album";
        public static final String COL_ALBUM_ID = "track_album_id";
        public static final String COL_ARTIST = "track_artist";
        public static final String COL_ARTIST_ID = "track_artist_id";
        public static final String COL_TRACK_NUMBER = "track_number";
        public static final String COL_GENRE = "track_genre";
        public static final String COL_DURATION = "track_duration";
        //static final String COL_LENGTH = "length";
    }


    public static class AlbumsEntry implements BaseColumns {
        public static final String TABLE_NAME = "Albums";
        public static final String COL_NAME = "album_name";
    }


    public static class ArtistsEntry implements BaseColumns {
        public static final String TABLE_NAME = "Artists";
        public static final String COL_NAME = "artist_name";
    }


    public static class PlaylistEntry implements BaseColumns {
        public static final String TABLE_NAME = "Playlists";
        public static final String COL_NAME = "playlist_name";
    }


    public static class PlaylistItemsEntry implements BaseColumns {
       public static final String TABLE_NAME = "Playlist_items";
        public static final String COL_PLAYLIST_ID= "pl_item_playlist_id";
        public static final String COL_INDEX = "pl_item_index";

       public static final String COL_PATH = "pl_item_path";
       public static final String COL_TITLE = "pl_item_title";
       public static final String COL_ALBUM = "pl_item_album";
       public static final String COL_ALBUM_ID = "pl_item_album_id";
       public static final String COL_ARTIST = "pl_item_artist";
       public static final String COL_ARTIST_ID = "pl_item_artist_id";
       public static final String COL_TRACK_NUMBER = "pl_item_number";
       public static final String COL_GENRE = "pl_item_genre";
       public static final String COL_DURATION = "pl_item_duration";
    }

}
