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
    private View longClickedView;


    class TextViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        TextViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.trackName);
            view.setOnClickListener(v -> onClick(this, v, textView));
            view.setOnLongClickListener(v -> onLongClick(v, textView));
        }
    }


    private void onClick(TextViewHolder textViewHolder, View v, TextView textView){
        deselectCurrentlySelectedItem();
        currentlySelectedView = v;
        currentItem = textView.getText().toString();
        setIndexToScrollTo(textViewHolder.getLayoutPosition());
        currentlySelectedView.setSelected(true);
        selectedPosition = RecyclerView.NO_POSITION;
        clickConsumer.accept(currentItem);
    }


    private boolean onLongClick(View v, TextView textView){
        currentItem = textView.getText().toString();
        longClickConsumer.accept(currentItem);
        longClickedView = v;
        return false;
    }


    public void  selectLongClickItem(){
        if(longClickedView == null){
            return;
        }
        longClickedView.callOnClick();
    }


    public void setItems(List<String> albumNames){
        this.items = albumNames;
    }


    public StringListAdapter(List<String> items, Consumer<String> clickConsumer, Consumer<String> longClickConsumer){
        this.items = new ArrayList<>(items);
        this.clickConsumer = clickConsumer;
        this.longClickConsumer = longClickConsumer;
    }


    @Override
    @NonNull
    public TextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_list_item_view, parent,false);
        return new TextViewHolder(view);
    }


    public void deselectCurrentlySelectedItem(){
        log("deselectCurrentlySelectedItem() ");
        if(currentlySelectedView != null){
            log("deselectCurrentlySelectedItem() currentlySelectedView is not null!");
            currentlySelectedView.setSelected(false);
        }
    }


    private void log(String msg){
        System.out.println("^^^ StringListAdapter: " + msg);
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
            log("onBindViewHolder() just selected a view!");
        }
    }


    @Override
    public int getItemCount(){
        return items.size();
    }


    public void setIndexToScrollTo(int index){
        this.indexToScrollTo = index;
    }



}