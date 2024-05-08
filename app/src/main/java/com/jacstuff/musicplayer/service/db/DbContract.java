package com.jacstuff.musicplayer.service.db;

import android.provider.BaseColumns;

public final class DbContract {

    private DbContract(){}


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
       public static final String COL_BITRATE = "pl_item_bitrate";
       public static final String COL_YEAR = "pl_item_year";
       public static final String COL_DISC = "pl_item_disc";
       public static final String COL_DURATION = "pl_item_duration";
    }

}
