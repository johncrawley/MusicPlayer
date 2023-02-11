package com.jacstuff.musicplayer.list;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.db.track.Track;
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


    public TrackListAdapter(List<Track> tracks, Consumer<Track> onClick, Consumer<Track> onLongClick){
        this.trackNames = new ArrayList<>();
        this.tracks = tracks;
        clickConsumer = onClick;
        longClickConsumer = onLongClick;
        for(Track track : tracks){
            this.trackNames.add(getStrOf(track));
        }
    }


    public void setItems(List<Track> tracks){
        this.tracks = tracks;
        trackNames = tracks.stream().map(this::getStrOf).collect(Collectors.toList());
    }


    @Override
    @NonNull
    public TrackListAdapter.TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
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

        else{
            log("Current selected item is null!");
        }
    }


    private void log(String msg){
        System.out.println("^^^ TrackListAdapter: " + msg);
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
        log("Entered changePositionTo() " +  newPosition);
        notifyItemChanged(selectedPosition);
        selectedPosition = newPosition;
        notifyItemChanged(selectedPosition);
    }


    public void setIndexToScrollTo(int index){
        this.indexToScrollTo = index;
    }



}
