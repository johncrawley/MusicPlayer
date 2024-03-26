package com.jacstuff.musicplayer.view.fragments.playlist;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.db.entities.Playlist;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistRecyclerAdapter extends RecyclerView.Adapter<PlaylistRecyclerAdapter.PlaylistViewHolder> {

    private final List<Playlist> playlists;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private View currentlySelectedView;
    private final Consumer<Playlist> onItemClickConsumer, onItemLongClickConsumer;
    private Playlist selectedPlaylist;
    private Playlist longClickedPlaylist;
    private View longClickedView;


    class PlaylistViewHolder extends RecyclerView.ViewHolder {

        TextView trackNameTextView;

        PlaylistViewHolder(View view) {
            super(view);
            trackNameTextView = view.findViewById(R.id.trackName);
            view.setOnClickListener(v -> onClick(v, trackNameTextView));
            view.setOnLongClickListener(v -> onLongClick(v, trackNameTextView));
        }
    }


    private void onClick(View v, TextView trackNameTextView){
        if(currentlySelectedView != null){
            currentlySelectedView.setSelected(false);
        }
        selectedPlaylist = (Playlist)trackNameTextView.getTag();
        currentlySelectedView = v;
        currentlySelectedView.setSelected(true);
        onItemClickConsumer.accept(selectedPlaylist);
    }


    private boolean onLongClick(View v, TextView trackNameTextView){
        longClickedView = v;
        longClickedPlaylist = (Playlist)trackNameTextView.getTag();
        onItemLongClickConsumer.accept(longClickedPlaylist);
        return false;
    }


    public PlaylistRecyclerAdapter(List<Playlist> playlists,
                                   Consumer<Playlist> onItemClickConsumer,
                                   Consumer<Playlist> onItemLongClickConsumer){
        this.playlists = new ArrayList<>(playlists);
        this.onItemClickConsumer = onItemClickConsumer;
        this.onItemLongClickConsumer = onItemLongClickConsumer;
    }


    public PlaylistRecyclerAdapter(List<Playlist> playlists, Consumer<Playlist> onItemClickConsumer){
        this.playlists = new ArrayList<>(playlists);
        this.onItemClickConsumer = onItemClickConsumer;
        this.onItemLongClickConsumer = playlist -> {};
    }


    public Playlist getLongClickedPlaylist(){
        return longClickedPlaylist;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh(List<Playlist> playlists){
        this.playlists.clear();
        this.playlists.addAll(playlists);
        notifyDataSetChanged();
        reselectPreviouslySelectedPlaylist();
    }


    public Playlist getSelectedPlaylist(){
        return selectedPlaylist;
    }


    public void selectLongClickedView(){
        longClickedView.callOnClick();
    }


    public void select(View view){
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
        int indexToScrollTo = -1;
        if(position == indexToScrollTo){
            deselectCurrentlySelectedItem();
            currentlySelectedView = holder.itemView;
            currentlySelectedView.setSelected(true);
        }
    }


    public void clearLongClickedView(){
        if(selectedPlaylist == null || longClickedPlaylist == null){
            return;
        }
        if(selectedPlaylist.getId().equals(longClickedPlaylist.getId())){
           selectedPlaylist = null;
           currentlySelectedView = null;
        }
        longClickedView = null;

    }


    private void reselectPreviouslySelectedPlaylist(){
        if(selectedPlaylist == null){
            return;
        }
        for(int i=0; i< playlists.size(); i++){
            if(selectedPlaylist.getId().equals(playlists.get(i).getId())){
                selectedPosition = i;
                break;
            }
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

}
