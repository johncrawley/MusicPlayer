package com.jacstuff.musicplayer.db.search;

import com.jacstuff.musicplayer.db.track.Track;

import java.util.List;
import java.util.Set;

public class TrieNode {

    private Set<TrieNode> nodes;
    private char value;
    private List<Track> results;


}
