package com.jacstuff.musicplayer.view.fragments.tracks;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.db.entities.Playlist;
import com.jacstuff.musicplayer.service.db.entities.Track;
import java.util.List;
import java.util.function.Consumer;

public class TrackListAdapter extends RecyclerView.Adapter<TrackListAdapter.TrackViewHolder> {

    private final List<Track> tracks;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private View currentlySelectedView;
    private int indexToScrollTo = -1;
    private final Consumer<Track> clickConsumer, longClickConsumer;
    private Playlist playlist;


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
        this.playlist = playlist;
        this.tracks = playlist.getTracks();
        clickConsumer = onClick;
        longClickConsumer = onLongClick;
    }


    @Override
    @NonNull
    public TrackListAdapter.TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_view, parent,false);
        return new TrackViewHolder(view);
    }


    public void selectItemAt(int index){
        deselectCurrentlySelectedItem();
        setIndexToScrollTo(index);
        changePositionTo(index);
    }


    public void deselectCurrentlySelectedItem(){
        if(currentlySelectedView != null){
            currentlySelectedView.setSelected(false);
            currentlySelectedView = null;
            selectedPosition = RecyclerView.NO_POSITION;
        }
    }


    public void setPlaylist(Playlist playlist){
        this.playlist = playlist;
    }


    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position){
        Track track = tracks.get(position);
        String ARTIST_TRACK_NAME_SEPARATOR = " - ";
        String displayName = switch(playlist.getType()){
            case ARTIST -> track.getAlbum() + ARTIST_TRACK_NAME_SEPARATOR + track.getTitle();
            case ALBUM -> getDisplayNameForAlbumTrack(track);
            default -> track.getArtist() + ARTIST_TRACK_NAME_SEPARATOR + track.getTitle();
        };
        holder.trackNameTextView.setText(displayName);
        holder.trackNameTextView.setTag(position);
        holder.itemView.setSelected(selectedPosition == position);

        if(position == indexToScrollTo){
            deselectCurrentlySelectedItem();
            currentlySelectedView = holder.itemView;
            currentlySelectedView.setSelected(true);
        }
    }


    private String getDisplayNameForAlbumTrack(Track track){
        String output = "";
        if(track.getTrackNumber() > 0){
            output += track.getTrackNumberStr();
            output += ". ";
        }
        return output +  track.getTitle();
    }


    @Override
    public int getItemCount(){
        return tracks == null ? 0 : tracks.size();
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
