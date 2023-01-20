package com.jacstuff.musicplayer.fragments.playlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.db.playlist.Playlist;
import com.jacstuff.musicplayer.db.track.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistRecyclerAdapter extends RecyclerView.Adapter<PlaylistRecyclerAdapter.PlaylistViewHolder> {

    private final List<Playlist> playlists;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private View currentlySelectedView;
    private int indexToScrollTo = -1;
    private final Consumer<Playlist> onItemClickConsumer;
    private Playlist selectedPlaylist;


    class PlaylistViewHolder extends RecyclerView.ViewHolder {

        TextView trackNameTextView;

        PlaylistViewHolder(View view) {
            super(view);
            trackNameTextView = view.findViewById(R.id.trackName);

            view.setOnClickListener(v -> {
                if(currentlySelectedView != null){
                    currentlySelectedView.setSelected(false);
                }
                selectedPlaylist = (Playlist)trackNameTextView.getTag();
                currentlySelectedView = v;
                currentlySelectedView.setSelected(true);
            });
        }
    }


    public PlaylistRecyclerAdapter(List<Playlist> playlists, Consumer<Playlist> onItemClickConsumer){
        this.playlists = new ArrayList<>(playlists);
        this.onItemClickConsumer = onItemClickConsumer;
    }


    public void refresh(List<Playlist> playlists){
        this.playlists.clear();
        this.playlists.addAll(playlists);
    }


    public Playlist getSelectedPlaylist(){
        return selectedPlaylist;
    }


    public int getSelectedPosition(){
        return selectedPosition;
    }


    public void select(View view){
       // int index = Math.max(Math.min(position, playlists.size()-1), 0);
        changePositionTo(0);
        selectedPlaylist = playlists.get(0);
        currentlySelectedView = view;
    }


    @Override
    @NonNull
    public PlaylistRecyclerAdapter.PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_list_item_view, parent,false);
        return new PlaylistViewHolder(view);
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
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position){
        holder.trackNameTextView.setText(playlists.get(position).getName());
        holder.trackNameTextView.setTag(playlists.get(position));
        holder.itemView.setSelected(selectedPosition == position);
        if(position == indexToScrollTo){
            deselectCurrentlySelectedItem();
            currentlySelectedView = holder.itemView;
            currentlySelectedView.setSelected(true);
        }
    }


    @Override
    public int getItemCount(){
        return playlists.size();
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
