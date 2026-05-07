package com.jacstuff.musicplayer.view.fragments.options;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.db.entities.Playlist;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistRecyclerAdapter extends RecyclerView.Adapter<PlaylistRecyclerAdapter.PlaylistViewHolder> {

    private final List<Playlist> playlists;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private View currentlySelectedView;
    private final BiConsumer<Playlist, Integer> onItemClickConsumer,  onItemLongClickConsumer;
    private Playlist selectedPlaylist;
    private Playlist longClickedPlaylist;
    private int indexToScrollTo = -1;
    public boolean isMultiSelectEnabled;


    class PlaylistViewHolder extends RecyclerView.ViewHolder {

        TextView trackNameTextView;

        PlaylistViewHolder(View view) {
            super(view);
            trackNameTextView = view.findViewById(R.id.itemName);
            view.setOnClickListener(v -> onClick(this, v, trackNameTextView));
            view.setOnLongClickListener(v -> onLongClick(this, v, trackNameTextView));
        }
    }


    private void onClick(RecyclerView.ViewHolder viewHolder, View v, TextView textView){
        deselectPreviousSelection();
        selectedPlaylist = (Playlist)textView.getTag();
        currentlySelectedView = v;
        toggleSelected();
        currentlySelectedView.setSelected(true);
        onItemClickConsumer.accept(selectedPlaylist, viewHolder.getBindingAdapterPosition());
    }


    private void toggleSelected(){
        if(isMultiSelectEnabled){
            currentlySelectedView.setSelected(!currentlySelectedView.isSelected());
            return;
        }
        currentlySelectedView.setSelected(true);
    }


    private void deselectPreviousSelection(){
        if(currentlySelectedView != null && !isMultiSelectEnabled){
            currentlySelectedView.setSelected(false);
        }
    }


    private boolean onLongClick(RecyclerView.ViewHolder viewHolder, View v, TextView trackNameTextView){
        longClickedPlaylist = (Playlist)trackNameTextView.getTag();
        onItemLongClickConsumer.accept(longClickedPlaylist, viewHolder.getBindingAdapterPosition());
        return false;
    }


    public PlaylistRecyclerAdapter(List<Playlist> playlists, BiConsumer<Playlist, Integer> onItemClickConsumer){
        this.playlists = new ArrayList<>(playlists);
        this.onItemClickConsumer = onItemClickConsumer;
        this.onItemLongClickConsumer = (playlist, position) -> {};
    }


    @Override
    @NonNull
    public PlaylistRecyclerAdapter.PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_view, parent,false);
        return new PlaylistViewHolder(view);
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


}
