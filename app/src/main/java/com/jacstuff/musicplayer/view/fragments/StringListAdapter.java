package com.jacstuff.musicplayer.view.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jacstuff.musicplayer.R;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StringListAdapter extends RecyclerView.Adapter<StringListAdapter.TextViewHolder> {

    private List<String> items;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private View currentlySelectedView;
    private int indexToScrollTo = -1;
    private final Consumer<String> clickConsumer, longClickConsumer;
    private String currentItem;


    class TextViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        TextViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.trackName);

            view.setOnClickListener(v -> {
                deselectCurrentlySelectedItem();
                currentlySelectedView = v;
                currentItem = textView.getText().toString();
                setIndexToScrollTo(getLayoutPosition());
                currentlySelectedView.setSelected(true);
                selectedPosition = RecyclerView.NO_POSITION;
                clickConsumer.accept(currentItem);
            });

            view.setOnLongClickListener(v -> {
                longClickConsumer.accept(currentItem);
                return false;
            });
        }
    }


    public void setItems(List<String> albumNames){
        this.items = albumNames;
    }


    public StringListAdapter(List<String> items, Consumer<String> clickConsumer, Consumer<String> longClickConsumer){
        this.items = new ArrayList<>(items);
        this.clickConsumer = clickConsumer;
        this.longClickConsumer = longClickConsumer;
    }


    public StringListAdapter(List<String> items, Consumer<String> clickConsumer){
        this(items, clickConsumer, s-> {});
    }


    public String getCurrentlySelectedItem(){
        return currentItem;
    }


    @Override
    @NonNull
    public TextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_list_item_view, parent,false);
        return new TextViewHolder(view);
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
    public void onBindViewHolder(@NonNull TextViewHolder holder, int position){
        holder.textView.setText(items.get(position));
        holder.textView.setTag(items.get(position));
        holder.itemView.setSelected(selectedPosition == position);

        if(position == indexToScrollTo){
            deselectCurrentlySelectedItem();
            currentlySelectedView = holder.itemView;
            currentlySelectedView.setSelected(true);
        }
    }


    @Override
    public int getItemCount(){
        return items.size();
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