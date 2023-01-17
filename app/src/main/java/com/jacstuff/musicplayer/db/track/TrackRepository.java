package com.jacstuff.musicplayer.db.track;

import com.jacstuff.musicplayer.db.album.Album;
import com.jacstuff.musicplayer.db.artist.Artist;

import java.util.List;

public interface TrackRepository {

    void addTrack(Track track);
    void deleteTrack(Track track);
    void recreateTracksTables();
    List<Track> getAllTracks();
    List<Track> getTracksForArtist(Artist artist);
    List<Track> getTracksForAlbum(Album album);
    List<Track> getAllTracksContaining(String prefix);

}
