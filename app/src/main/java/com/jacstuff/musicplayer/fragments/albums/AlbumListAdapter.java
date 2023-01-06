package com.jacstuff.musicplayer.fragments.albums;

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

public class AlbumListAdapter extends RecyclerView.Adapter<AlbumListAdapter.TrackViewHolder> {

    private final List<String> albumNames;
    private final MediaPlayerView mediaPlayerView;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private View currentlySelectedView;
    private String currentlySelectedItem;
    private int indexToScrollTo = -1;


    class TrackViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        TrackViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.trackName);

            view.setOnClickListener(v -> {
                deselectCurrentlySelectedItem();
                currentlySelectedView = v;
                currentlySelectedItem = textView.getText().toString();
                setIndexToScrollTo(getLayoutPosition());
                currentlySelectedView.setSelected(true);
                selectedPosition = RecyclerView.NO_POSITION;
            });
        }
    }


    public AlbumListAdapter(List<Artist> artists, MediaPlayerView view){
        this.albumNames = new ArrayList<>();
        this.mediaPlayerView = view;
        for(Artist artist : artists){
            this.albumNames.add(artist.getName());
        }
    }


    public String getCurrentlySelectedItem(){
        return currentlySelectedItem;
    }


    @Override
    @NonNull
    public AlbumListAdapter.TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_list_item_view, parent,false);
        return new AlbumListAdapter.TrackViewHolder(view);
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
    public void onBindViewHolder(@NonNull AlbumListAdapter.TrackViewHolder holder, int position){
        holder.textView.setText(albumNames.get(position));
        holder.textView.setTag(albumNames.get(position));
        holder.itemView.setSelected(selectedPosition == position);

        if(position == indexToScrollTo){
            deselectCurrentlySelectedItem();
            currentlySelectedView = holder.itemView;
            currentlySelectedView.setSelected(true);
        }
    }


    @Override
    public int getItemCount(){
        return albumNames.size();
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