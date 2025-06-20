package com.jacstuff.musicplayer.view.fragments.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jacstuff.musicplayer.R;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class MultiSelectionStringListAdapter extends RecyclerView.Adapter<MultiSelectionStringListAdapter.TextViewHolder> {

    private final List<SelectableStringListItem> items = new ArrayList<>(200);
    private final BiConsumer<String, Integer> clickConsumer;


    public static class TextViewHolder extends RecyclerView.ViewHolder{

        private final View view;
        private final TextView textView;

        private TextViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            textView = itemView.findViewById(R.id.trackName);
        }
    }


    public MultiSelectionStringListAdapter(List<String> strList, BiConsumer<String, Integer> clickConsumer ) {
        this.clickConsumer = clickConsumer;
        if(strList == null){
            return;
        }
        for(String str: strList){
            items.add(new SelectableStringListItem(str));
        }
    }


    @Override
    @NonNull
    public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_multi_item_view, parent,false);
        return new TextViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final TextViewHolder holder, int position) {
        final SelectableStringListItem item = items.get(position);
        holder.textView.setText(item.getValue());
        holder.view.setSelected(item.isSelected());
        holder.view.setOnClickListener(v -> {
                item.toggleSelected();
                v.setSelected(item.isSelected());
                clickConsumer.accept(item.getValue(), holder.getBindingAdapterPosition());
            });
    }


    @Override
    public int getItemCount() {
        return items.size();
    }
}