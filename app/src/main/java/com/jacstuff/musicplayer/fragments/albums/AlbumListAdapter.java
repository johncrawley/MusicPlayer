package com.jacstuff.musicplayer.fragments.albums;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.db.album.Album;
import com.jacstuff.musicplayer.db.artist.Artist;
import com.jacstuff.musicplayer.db.track.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AlbumListAdapter extends RecyclerView.Adapter<AlbumListAdapter.TrackViewHolder> {

    private List<String> albumNames;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private View currentlySelectedView;
    private String currentlySelectedName;
    private int indexToScrollTo = -1;
    private Consumer<Album> clickConsumer;
    private Album currentItem;


    class TrackViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        TrackViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.trackName);

            view.setOnClickListener(v -> {
                deselectCurrentlySelectedItem();
                currentlySelectedView = v;
                currentlySelectedName = textView.getText().toString();
                currentItem = new Album(-1, currentlySelectedName);
                setIndexToScrollTo(getLayoutPosition());
                currentlySelectedView.setSelected(true);
                selectedPosition = RecyclerView.NO_POSITION;
                clickConsumer.accept(new Album(-1, currentlySelectedName));
            });
        }
    }


    public void setItems(List<String> albumNames){
        this.albumNames = albumNames;
    }


    public AlbumListAdapter(List<Album> albums, Consumer<Album> clickConsumer){
        this.albumNames = new ArrayList<>();
        for(Album album : albums){
            this.albumNames.add(album.getName());
        }
        this.clickConsumer = clickConsumer;
    }





    public Album getCurrentlySelectedItem(){
        return currentItem;
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