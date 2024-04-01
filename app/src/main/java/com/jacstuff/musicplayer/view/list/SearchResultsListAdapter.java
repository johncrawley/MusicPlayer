package com.jacstuff.musicplayer.view.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.db.entities.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SearchResultsListAdapter extends RecyclerView.Adapter<SearchResultsListAdapter.TrackViewHolder> {

    private final List<String> trackNames;
    private List<Track> tracks;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private View currentlySelectedView;
    private int indexToScrollTo = -1;
    private Track selectedTrack;
    private final Consumer<Track> onClickConsumer;

    class TrackViewHolder extends RecyclerView.ViewHolder {

        TextView trackNameTextView;

        TrackViewHolder(View view) {
            super(view);
            trackNameTextView = view.findViewById(R.id.trackName);

            view.setOnClickListener(v -> {
                deselectCurrentlySelectedItem();
                currentlySelectedView = v;
                currentlySelectedView.setSelected(true);
                Integer position = (Integer)trackNameTextView.getTag();
                if(position != null) {
                    selectedTrack = tracks.get(position);
                }
                onClickConsumer.accept(selectedTrack);
                selectedPosition = RecyclerView.NO_POSITION;
            });
        }
    }


    public SearchResultsListAdapter(List<Track> tracks, Consumer<Track> onClickConsumer){
        this.trackNames = new ArrayList<>();
        this.tracks = tracks;
        tracks.forEach(t -> trackNames.add(getStrOf(t)));
        this.onClickConsumer = onClickConsumer;
    }


    public void setTracks(List<Track> tracks){
        this.tracks = tracks;
        this.trackNames.clear();
        for(Track track : tracks){
            this.trackNames.add(getStrOf(track));
        }
    }


    @Override
    @NonNull
    public SearchResultsListAdapter.TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_list_item_view, parent,false);
        return new TrackViewHolder(view);
    }


    private String getStrOf(Track trackDetails){
        return trackDetails.getArtist() + " : " +  trackDetails.getTitle();
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


}
