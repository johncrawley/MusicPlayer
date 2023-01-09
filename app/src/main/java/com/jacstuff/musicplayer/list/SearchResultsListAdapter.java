package com.jacstuff.musicplayer.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.db.track.Track;

import java.util.ArrayList;
import java.util.List;

public class SearchResultsListAdapter extends RecyclerView.Adapter<SearchResultsListAdapter.TrackViewHolder> {

    private final List<String> trackNames;
    private List<Track> tracks;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private View currentlySelectedView;
    private int indexToScrollTo = -1;


    class TrackViewHolder extends RecyclerView.ViewHolder {

        TextView trackNameTextView;

        TrackViewHolder(View view) {
            super(view);
            trackNameTextView = view.findViewById(R.id.trackName);

            view.setOnClickListener(v -> {
                deselectCurrentlySelectedItem();
                currentlySelectedView = v;
                currentlySelectedView.setSelected(true);
                selectedPosition = RecyclerView.NO_POSITION;
            });
        }
    }


    public SearchResultsListAdapter(List<Track> tracks){
        this.trackNames = new ArrayList<>();
        this.tracks = tracks;
        for(Track trackDetails : tracks){
            this.trackNames.add(getStrOf(trackDetails));
        }
    }


    public void setTracks(List<Track> tracks){
        this.tracks = tracks;
        this.trackNames.clear();
        for(Track track : tracks){
            this.trackNames.add(getStrOf(track));
        }
    }


    public Track getSelectedTrack(){
        if(selectedPosition == RecyclerView.NO_POSITION){
            return null;
        }
        return tracks.get(selectedPosition);
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
        holder.trackNameTextView.setTag(trackNames.get(position));
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
