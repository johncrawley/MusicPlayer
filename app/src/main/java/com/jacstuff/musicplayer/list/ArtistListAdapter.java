package com.jacstuff.musicplayer.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jacstuff.musicplayer.MediaPlayerView;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.db.artist.Artist;

import java.util.ArrayList;
import java.util.List;

public class ArtistListAdapter extends RecyclerView.Adapter<ArtistListAdapter.TrackViewHolder> {

    private final List<String> artistNames;
    private final MediaPlayerView mediaPlayerView;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private View currentlySelectedView;
    private String currentlySelectedArtistName;
    private int indexToScrollTo = -1;


    class TrackViewHolder extends RecyclerView.ViewHolder {

        TextView artistTextView;

        TrackViewHolder(View view) {
            super(view);
            artistTextView = view.findViewById(R.id.trackName);

            view.setOnClickListener(v -> {
                deselectCurrentlySelectedItem();
                currentlySelectedView = v;
                currentlySelectedArtistName = artistTextView.getText().toString();
                mediaPlayerView.notifyCurrentlySelectedTrack(getLayoutPosition());
                setIndexToScrollTo(getLayoutPosition());
                currentlySelectedView.setSelected(true);
                selectedPosition = RecyclerView.NO_POSITION;
            });
        }
    }


    public ArtistListAdapter(List<Artist> artists, MediaPlayerView view){
        this.artistNames = new ArrayList<>();
        this.mediaPlayerView = view;
        for(Artist artist : artists){
            this.artistNames.add(artist.getName());
        }
    }


    public String getCurrentlySelectedArtistName(){
        return currentlySelectedArtistName;
    }


    @Override
    @NonNull
    public ArtistListAdapter.TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_list_item_view, parent,false);
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
        }
    }


    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position){
        holder.artistTextView.setText(artistNames.get(position));
        holder.artistTextView.setTag(artistNames.get(position));
        holder.itemView.setSelected(selectedPosition == position);

        if(position == indexToScrollTo){
            deselectCurrentlySelectedItem();
            currentlySelectedView = holder.itemView;
            currentlySelectedView.setSelected(true);
        }
    }


    @Override
    public int getItemCount(){
        return artistNames.size();
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
