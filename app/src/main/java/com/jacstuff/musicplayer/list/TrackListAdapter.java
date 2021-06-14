package com.jacstuff.musicplayer.list;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jacstuff.musicplayer.MediaPlayerView;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.TrackDetails;
import java.util.ArrayList;
import java.util.List;

public class TrackListAdapter extends RecyclerView.Adapter<TrackListAdapter.TrackViewHolder> {

    private List<String> trackNames;
    private MediaPlayerView mediaPlayerView;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private View currentlySelectedView;
    private int indexToScrollTo = -1;


    class TrackViewHolder extends RecyclerView.ViewHolder {

        TextView trackNameTextView;

        TrackViewHolder(View view) {
            super(view);
            trackNameTextView = view.findViewById(R.id.trackName);

            view.setOnClickListener(new View.OnClickListener()
                {
                @Override
                public void onClick (View v){
                    if(currentlySelectedView != null){
                        currentlySelectedView.setSelected(false);
                    }
                    currentlySelectedView = v;
                    currentlySelectedView.setSelected(true);
                    mediaPlayerView.notifyCurrentlySelectedTrack(getLayoutPosition());
                     mediaPlayerView.scrollToListPosition(getLayoutPosition());
                    setIndexToScrollTo(getLayoutPosition());
                    currentlySelectedView.setSelected(true);
                }
            });
        }
    }


    public TrackListAdapter(List<TrackDetails> trackDetailsList, MediaPlayerView view){
        this.trackNames = new ArrayList<>();
        this.mediaPlayerView = view;
        for(TrackDetails trackDetails : trackDetailsList){
            this.trackNames.add(getStrOf(trackDetails));
        }
    }


    @Override
    @NonNull
    public TrackListAdapter.TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_list_item_view, parent,false);
        return new TrackViewHolder(view);
    }


    private String getStrOf(TrackDetails trackDetails){
        return trackDetails.getArtist() + " : " +  trackDetails.getName();
    }


    public void deselectCurrentlySelectedItem(){
        if(currentlySelectedView != null){
            System.out.println("TrackListAdapter.deselectCurrentlySelectedItem() - setting to false");
            currentlySelectedView.setSelected(false);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position){
        holder.trackNameTextView.setText(trackNames.get(position));
        holder.trackNameTextView.setTag(trackNames.get(position));
        holder.itemView.setSelected(selectedPosition == position);

        if(position == indexToScrollTo){
            System.out.println("TrackListAdapter.onBindViewHolder() - about to deselect current item");
            deselectCurrentlySelectedItem();
            currentlySelectedView = holder.itemView;
            currentlySelectedView.setSelected(true);
        }
    }


    @Override
    public int getItemCount(){
        return trackNames.size();
    }


    private void changePositionTo(int newPosition){
        notifyItemChanged(selectedPosition);
        selectedPosition = newPosition;
        notifyItemChanged(selectedPosition);
    }


    public void setIndexToScrollTo(int index){
        this.indexToScrollTo = index;
    }



}
