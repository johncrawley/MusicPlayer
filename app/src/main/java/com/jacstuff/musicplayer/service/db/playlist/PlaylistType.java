package com.jacstuff.musicplayer.service.db.playlist;

public enum PlaylistType {
    ALL_TRACKS(-100L, false),
    PLAYLIST(-1L, true),
    ALBUM(-10L, false),
    ARTIST(-20L, false),
    GENRE(-30L, false);

    final long defaultId;
    final boolean isUserPlaylist;


    PlaylistType(long defaultId, boolean isUserPlaylist) {
        this.defaultId = defaultId;
        this.isUserPlaylist = isUserPlaylist;
    }


    public long getDefaultId() {
        return defaultId;
    }


    public boolean isUserPlaylist() {
        return isUserPlaylist;
    }

}
