package com.project.videoeditor;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    private ArrayList<Bitmap> framesCollage;
    private ArrayList<String> namesCollage;

    public VideoAdapter(@NonNull Bitmap frameCollage,@NonNull String nameCollage) {
        framesCollage = new ArrayList<>();
        namesCollage = new ArrayList<>();
        addItem(frameCollage,nameCollage);

    }
    public VideoAdapter(ArrayList<Bitmap> framesCollage, ArrayList<String> namesCollage) {
        this.framesCollage = framesCollage;
        this.namesCollage = namesCollage;
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
    public VideoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView cv = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_video_image,parent,false);
        return new ViewHolder(cv);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoAdapter.ViewHolder holder, int position) {
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
}
