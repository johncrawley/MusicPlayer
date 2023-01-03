package com.jacstuff.musicplayer.db.track;

import com.jacstuff.musicplayer.db.artist.Artist;

import java.util.List;

public interface TrackRepository {

    void addTrack(Track track);
    void deleteTrack(Track track);
    List<Track> getAllTracks();
    List<Track> getTracksForArtist(Artist artist);
    List<Track> getAllTracksStartingWith(String prefix);

}
