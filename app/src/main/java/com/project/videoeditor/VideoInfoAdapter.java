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
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class VideoInfoAdapter extends RecyclerView.Adapter<VideoInfoAdapter.ViewHolder>{

    private ArrayList<Drawable> idIconsDrawable;
    private ArrayList<String> namesInfo;
    private ArrayList<String> contents;

    public VideoInfoAdapter(@NonNull ArrayList<Drawable> idIcons, @NonNull ArrayList<String> namesInfo,
                            @NonNull ArrayList<String> contents)
    {
        this.idIconsDrawable = idIcons;
        this.namesInfo = namesInfo;
        this.contents = contents;
    }
    public VideoInfoAdapter(@NonNull Drawable idIcon, @NonNull String nameInfo, String content)
    {
        this.idIconsDrawable = new ArrayList<>();
        this.namesInfo = new ArrayList<>();
        this.contents = new ArrayList<>();
        this.addItem(idIcon,nameInfo,content);
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
        CardView cv = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_videoinfo,parent,false);
        return new VideoInfoAdapter.ViewHolder(cv);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        ImageView imageIcon = (ImageView)cardView.findViewById(R.id.icon);
        TextView nameInfo = (TextView)cardView.findViewById(R.id.nameInfo);
        TextView content = (TextView)cardView.findViewById(R.id.videoInfo);

        imageIcon.setImageDrawable(idIconsDrawable.get(position));
        nameInfo.setText(namesInfo.get(position));
        content.setText(contents.get(position));
    }

    @Override
    public int getItemCount() {
        return contents.size();
    }
    public void addItem(@NonNull Drawable idIcon, @NonNull String nameInfo, String content)
    {
        idIconsDrawable.add(idIcon);
        namesInfo.add(nameInfo);
        contents.add(content);
    }

}
