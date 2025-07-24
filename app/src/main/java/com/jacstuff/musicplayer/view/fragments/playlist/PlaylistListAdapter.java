package com.jacstuff.musicplayer.view.fragments.playlist;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.db.entities.Playlist;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class PlaylistListAdapter extends RecyclerView.Adapter<PlaylistListAdapter.TextViewHolder> {

    private List<Playlist> items;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private View currentlySelectedView;
    private int indexToScrollTo = -1;
    private Playlist currentItem;
    private View longClickedView;
    private final BiConsumer<Playlist, Integer> clickConsumer;
    private final BiConsumer<Playlist, Integer> longClickConsumer;
    private Playlist longClickedPlaylist;


    class TextViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        TextViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.trackName);
            view.setOnClickListener(v -> onClick(this, v, textView));
            view.setOnLongClickListener(v -> onLongClick(v, textView));
        }
    }


    public PlaylistListAdapter(List<Playlist> items, BiConsumer<Playlist, Integer> clickConsumer, BiConsumer<Playlist, Integer> longClickConsumer){
        this.items = new ArrayList<>(items);
        this.clickConsumer = clickConsumer;
        this.longClickConsumer = longClickConsumer;
    }


    public void resetSelections(){
        selectedPosition = RecyclerView.NO_POSITION;
        currentlySelectedView = null;
        indexToScrollTo = -1;
        currentItem = null;
        longClickedView = null;
    }


    private void onClick(PlaylistListAdapter.TextViewHolder textViewHolder, View v, TextView textView){
        deselectCurrentlySelectedItem();
        currentlySelectedView = v;
        currentItem = getPlaylistFrom(textView);

        setIndexToScrollTo(textViewHolder.getLayoutPosition());
        currentlySelectedView.setSelected(true);

        selectedPosition = RecyclerView.NO_POSITION;
        clickConsumer.accept(currentItem, textViewHolder.getBindingAdapterPosition());
    }


    private boolean onLongClick(View v, TextView textView){
        int position = (int)textView.getTag();

        currentItem = getPlaylistFrom(textView);
        longClickConsumer.accept(currentItem, position);
        longClickedView = v;
        longClickedPlaylist = currentItem;
        return false;
    }


    private Playlist getPlaylistFrom(TextView textView){
        int position = (int)textView.getTag();
        return items.get(position);
    }


    public Playlist getLongClickedPlaylist(){
        return longClickedPlaylist;
    }


    public void  selectLongClickItem(){
        if(longClickedView == null){
            return;
        }
        longClickedView.callOnClick();
    }


    @SuppressWarnings("notifyDataSetChanged")
    public void setItems(List<Playlist> playlists){
        this.items = playlists;
        notifyDataSetChanged();
    }


    @Override
    @NonNull
    public PlaylistListAdapter.TextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_view, parent,false);
        return new PlaylistListAdapter.TextViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull PlaylistListAdapter.TextViewHolder holder, int position){
        holder.textView.setText(items.get(position).getName());
        holder.textView.setTag(position);
        holder.itemView.setSelected(selectedPosition == position);

        if(position == indexToScrollTo){
            deselectCurrentlySelectedItem();
            currentlySelectedView = holder.itemView;
            currentlySelectedView.setSelected(true);
        }
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


    public void select(View view, int position){
        changePositionTo(position);
        currentItem = items.get(position);
        currentlySelectedView = view;
    }


    public void changePositionTo(int newPosition){
        notifyItemChanged(selectedPosition);
        selectedPosition = newPosition;
        notifyItemChanged(selectedPosition);
    }

    @Override
    public int getItemCount(){
        return items.size();
    }


    public void setIndexToScrollTo(int index){
        this.indexToScrollTo = index;
    }


    public Playlist getCurrentItem(){
        return currentItem;
    }


    public void selectLongClickedView(){
        longClickedView.callOnClick();
    }


    public void clearLongClickedView(){
        if(currentItem == null || longClickedPlaylist == null){
            return;
        }
        if(currentItem.getId().equals(longClickedPlaylist.getId())){
            currentItem = null;
            currentlySelectedView = null;
        }
        longClickedView = null;
    }


    @SuppressLint("NotifyDataSetChanged")
    public void refresh(List<Playlist> playlists){
        this.items.clear();
        this.items.addAll(playlists);
        notifyDataSetChanged();
        reselectPreviouslySelectedPlaylist();
    }


    private void reselectPreviouslySelectedPlaylist(){
        if(currentItem == null){
            return;
        }
        for(int i = 0; i < items.size(); i++){
            var playlistId = items.get(i).getId();
            if(currentItem.getId().equals(playlistId)){
                selectedPosition = i;
                break;
            }
        }
    }
}