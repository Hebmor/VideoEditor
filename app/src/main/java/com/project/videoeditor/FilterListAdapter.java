package com.project.videoeditor;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FilterListAdapter  extends RecyclerView.Adapter<FilterListAdapter.ViewHolder> {
    private ArrayList<Drawable> idCoversDrawable;
    private ArrayList<String> namesFilters;

    public FilterListAdapter(@NonNull ArrayList<Drawable> idCoversDrawable,@NonNull ArrayList<String> namesFilters) {
        this.idCoversDrawable = idCoversDrawable;
        this.namesFilters = namesFilters;
    }

    public FilterListAdapter(@NonNull Drawable idCoverDrawable,@NonNull String nameFilters) {
        this.idCoversDrawable = new ArrayList<>();
        this.namesFilters = new ArrayList<>();
        this.addItem(idCoverDrawable,nameFilters);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public ViewHolder(@NonNull CardView cardView) {
            super(cardView);
            this.cardView = cardView;
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView cv = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_filter,parent,false);
        return new FilterListAdapter.ViewHolder(cv);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        ImageView imageCover = (ImageView)cardView.findViewById(R.id.filterPreview);
        TextView nameFilter = (TextView)cardView.findViewById(R.id.filterName);

        imageCover.setImageDrawable(idCoversDrawable.get(position));
        nameFilter.setText(namesFilters.get(position));
    }

    @Override
    public int getItemCount() {
        return namesFilters.size();
    }

    public void addItem(@NonNull Drawable idCover, @NonNull String nameFilter)
    {
        idCoversDrawable.add(idCover);
        namesFilters.add(nameFilter);
    }
}
