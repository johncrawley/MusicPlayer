package com.jacstuff.musicplayer.view.fragments.tracks;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.db.entities.Playlist;
import com.jacstuff.musicplayer.service.db.entities.PlaylistType;
import com.jacstuff.musicplayer.service.db.entities.Track;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TrackListAdapter extends RecyclerView.Adapter<TrackListAdapter.TrackViewHolder> {

    private List<String> trackNames;
    private List<Track> tracks;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private View currentlySelectedView;
    private int indexToScrollTo = -1;
    private final Consumer<Track> clickConsumer, longClickConsumer;
    private final String ARTIST_TRACK_NAME_SEPARATOR = " - ";


    class TrackViewHolder extends RecyclerView.ViewHolder {

        TextView trackNameTextView;

        TrackViewHolder(View view) {
            super(view);
            trackNameTextView = view.findViewById(R.id.trackName);

            view.setOnClickListener(v -> {
                deselectCurrentlySelectedItem();
                currentlySelectedView = v;
                setIndexToScrollTo(getLayoutPosition());
                processTrackCorrespondingToCurrentView(clickConsumer, getLayoutPosition());
                currentlySelectedView.setSelected(true);
                selectedPosition = RecyclerView.NO_POSITION;
            });

            view.setOnLongClickListener(v ->{
                processTrackCorrespondingToCurrentView(longClickConsumer, getLayoutPosition());
                return true;
            });
        }


        private void processTrackCorrespondingToCurrentView(Consumer<Track> consumer, int layoutPosition){
            Track track = tracks.get(layoutPosition);
            if(track != null) {
                consumer.accept(track);
            }
        }
    }


    public TrackListAdapter(Playlist playlist, Consumer<Track> onClick, Consumer<Track> onLongClick){
        this.trackNames = new ArrayList<>();
        this.tracks = playlist.getTracks();
        clickConsumer = onClick;
        longClickConsumer = onLongClick;
        boolean isAlbumPlaylist = playlist.getType() == PlaylistType.ALBUM;
        for(Track track : tracks){
            this.trackNames.add(getStrOf(track, false, true, isAlbumPlaylist));
        }
    }


    public void setItems(Playlist playlist, boolean useTrackNumber, boolean useArtist){
        this.tracks = playlist.getTracks();
        boolean isAlbumPlaylist = isAlbumPlaylist(playlist);
        if(tracks == null){
            return;
        }
        trackNames = tracks.stream().map(t -> getStrOf(t, useTrackNumber, useArtist, isAlbumPlaylist)).collect(Collectors.toList());
    }


    private boolean isAlbumPlaylist(Playlist playlist){
        return playlist.getType() == PlaylistType.ALBUM;
    }


    @Override
    @NonNull
    public TrackListAdapter.TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_list_item_view, parent,false);
        return new TrackViewHolder(view);
    }


    private String getStrOf(Track track, boolean useTrackNumber,  boolean useArtist, boolean isAlbumPlaylist){
        return isAlbumPlaylist ?
            getDisplayNameForAlbumTrack(track, useTrackNumber, useArtist)
            : getDisplayNameFor(track);
    }


    private String getDisplayNameFor(Track track){
        return track.getArtist() + ARTIST_TRACK_NAME_SEPARATOR + track.getTitle();
    }


    private String getDisplayNameForAlbumTrack(Track track, boolean useTrackNumber, boolean useArtist){
        String output = "";
        if(useTrackNumber){
            output += track.getTrackNumberStr();
            output += ". ";
        }
        if(useArtist){
            output += track.getArtist();
            output += ARTIST_TRACK_NAME_SEPARATOR;
        }
        return output +  track.getTitle();
    }


    public void selectItemAt(int index){
        deselectCurrentlySelectedItem();
        setIndexToScrollTo(index);
        changePositionTo(index);
    }


    public void deselectCurrentlySelectedItem(){
        if(currentlySelectedView != null){
            currentlySelectedView.setSelected(false);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position){
        holder.trackNameTextView.setText(trackNames.get(position));
        holder.trackNameTextView.setTag(position);
        holder.itemView.setSelected(selectedPosition == position);

        if(position == indexToScrollTo){
            deselectCurrentlySelectedItem();
            currentlySelectedView = holder.itemView;
            currentlySelectedView.setSelected(true);
        }
    }


    @Override
    public int getItemCount(){
        return trackNames.size();
    }


    public void changePositionTo(int newPosition){
        notifyItemChanged(selectedPosition);
        selectedPosition = newPosition;
        notifyItemChanged(selectedPosition);
    }


    public void setIndexToScrollTo(int index){
        this.indexToScrollTo = index;
    }


}
