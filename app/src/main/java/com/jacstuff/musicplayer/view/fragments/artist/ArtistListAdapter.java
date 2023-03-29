package com.jacstuff.musicplayer.view.fragments.artist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.service.db.artist.Artist;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ArtistListAdapter extends RecyclerView.Adapter<ArtistListAdapter.TrackViewHolder> {

    private final List<String> artistNames;
    private List<Artist> artists;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private View currentlySelectedView;
    private String currentlySelectedArtistName;
    private int indexToScrollTo = -1;
    private Artist selectedItem;
    private final Consumer<Artist> onClickConsumer;


    class TrackViewHolder extends RecyclerView.ViewHolder {

        TextView artistTextView;

        TrackViewHolder(View view) {
            super(view);
            artistTextView = view.findViewById(R.id.trackName);

            view.setOnClickListener(v -> {
                deselectCurrentlySelectedItem();
                currentlySelectedView = v;
                int currentPosition = (int)artistTextView.getTag();
                selectedItem = artists.get(currentPosition);
                setIndexToScrollTo(getLayoutPosition());
                currentlySelectedView.setSelected(true);
                selectedPosition = RecyclerView.NO_POSITION;
                onClickConsumer.accept(selectedItem);
            });
        }
    }


    public ArtistListAdapter(List<Artist> artists, Consumer<Artist> onClickConsumer){
        this.artistNames = new ArrayList<>();
        this.artists = artists;
        artists.forEach(a -> artistNames.add(a.getName()));
        this.onClickConsumer = onClickConsumer;
    }


    public Artist getCurrentlySelectedItem(){
        return selectedItem;
    }


    @Override
    @NonNull
    public ArtistListAdapter.TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_list_item_view, parent,false);
        return new TrackViewHolder(view);
    }


    public void deselectCurrentlySelectedItem(){
        if(currentlySelectedView != null){
            currentlySelectedView.setSelected(false);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position){
        holder.artistTextView.setText(artistNames.get(position));
        holder.artistTextView.setTag(position);
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


    public void setIndexToScrollTo(int index){
        this.indexToScrollTo = index;
    }



}
