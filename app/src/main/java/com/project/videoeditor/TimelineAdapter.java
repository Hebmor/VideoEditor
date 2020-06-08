package com.project.videoeditor;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {

    private ArrayList<Bitmap> framesCollage;
    private ArrayList<String> namesCollage;

    public TimelineAdapter(@NonNull Bitmap frameCollage, @NonNull String nameCollage) {
        framesCollage = new ArrayList<>();
        namesCollage = new ArrayList<>();
        addItem(frameCollage,nameCollage);
    }
    public TimelineAdapter(ArrayList<Bitmap> framesCollage, ArrayList<String> namesCollage) {
        this.framesCollage = framesCollage;
        this.namesCollage = namesCollage;
    }

    public TimelineAdapter() {
        framesCollage = new ArrayList<>();
        namesCollage = new ArrayList<>();
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
    public TimelineAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView cv = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_video_image,parent,false);
        return new ViewHolder(cv);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineAdapter.ViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        ImageView imageView = (ImageView)cardView.findViewById(R.id.videoCollage);
        TextView textView = (TextView)cardView.findViewById(R.id.nameVideo);

        imageView.setImageBitmap(framesCollage.get(position));
        imageView.setContentDescription(namesCollage.get(position));
        textView.setText(namesCollage.get(position));
    }

    @Override
    public int getItemCount() {
        return framesCollage.size();
    }

    public void addItem(@NonNull Bitmap frameCollage,@NonNull String nameCollage)
    {
        this.framesCollage.add(frameCollage);
        this.namesCollage.add(nameCollage);
    }
    public void addItemInPos(@NonNull Bitmap frameCollage,@NonNull String nameCollage,int pos)
    {
        this.framesCollage.add(pos,frameCollage);
        this.namesCollage.add(pos,nameCollage);
    }
    public Bitmap getBitmapByItemIndex(int index)
    {
        return this.framesCollage.get(index);
    }

    public String getNameByItemIndex(int index)
    {
        return this.namesCollage.get(index);
    }

    public void removeItemByIndex(int index)
    {
        this.framesCollage.remove(index);
        this.namesCollage.remove(index);
    }
}
