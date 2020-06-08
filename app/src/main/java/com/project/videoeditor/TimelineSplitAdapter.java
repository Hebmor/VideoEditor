package com.project.videoeditor;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TimelineSplitAdapter extends RecyclerView.Adapter<TimelineSplitAdapter.ViewHolder> {

    private ArrayList<Bitmap> framesCollage;
    private ArrayList<String> namesCollage;
    private ArrayList<TimelineEntity> timelineEntities;
    private int commonDurationMs = 0;
    private int commonWidthDp = 0;
    private boolean isDebugMod = false;

    public TimelineSplitAdapter(@NonNull Bitmap frameCollage, @NonNull String nameCollage,@NonNull TimelineEntity timelineEntity) {
        framesCollage = new ArrayList<>();
        namesCollage = new ArrayList<>();
        timelineEntities = new ArrayList<>();
        addItem(frameCollage,nameCollage,timelineEntity);
    }

    public TimelineSplitAdapter(ArrayList<Bitmap> framesCollage, ArrayList<String> namesCollage,ArrayList<TimelineEntity> timelineEntities) {
        this.framesCollage = framesCollage;
        this.namesCollage = namesCollage;
        this.timelineEntities = timelineEntities;
    }

    public TimelineSplitAdapter() {
        framesCollage = new ArrayList<>();
        namesCollage = new ArrayList<>();
        timelineEntities = new ArrayList<>();
    }

    public int getCommonDurationMs() {
        return commonDurationMs;
    }

    public int getCommonWidthDp() {
        return commonWidthDp;
    }

    public boolean isDebugMod() {
        return isDebugMod;
    }

    public void setDebugMod(boolean debugMod) {
        isDebugMod = debugMod;
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
    public TimelineSplitAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView cv = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_video_image,parent,false);
        return new ViewHolder(cv);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineSplitAdapter.ViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(timelineEntities.get(position).getWidth(),
                timelineEntities.get(position).getHeight());

        ImageView imageView = (ImageView)cardView.findViewById(R.id.videoCollage);
        TextView textView = (TextView)cardView.findViewById(R.id.nameVideo);

        imageView.setImageBitmap(framesCollage.get(position));
        imageView.setContentDescription(namesCollage.get(position));
        imageView.setLayoutParams(lp);
        if(this.timelineEntities.get(position).getType() != TimelineEntity.Type.EMPTY)
            textView.setText(namesCollage.get(position));
        else
            textView.setVisibility(View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return framesCollage.size();
    }

    public void addItem(@NonNull Bitmap frameCollage, @NonNull String nameCollage, @NonNull TimelineEntity timelineEntity)
    {
        this.framesCollage.add(frameCollage);
        this.namesCollage.add(nameCollage);
        this.timelineEntities.add(timelineEntity);
        if(isDebugMod)
        {
            Log.d("addItem","Добавлен элемент!");
            timelineEntity.printDebugInfo();
        }
        this.commonDurationMs+= timelineEntity.getDurationMs();
        this.commonWidthDp+= timelineEntity.getWidth();
    }
    public void addItemInPos(@NonNull Bitmap frameCollage, @NonNull String nameCollage, @NonNull TimelineEntity timelineEntity, int pos)
    {
        this.framesCollage.add(pos,frameCollage);
        this.namesCollage.add(pos,nameCollage);
        this.timelineEntities.add(pos,timelineEntity);
        if(isDebugMod)
        {
            Log.d("addItemInPos","Добавлен элемент!");
            timelineEntity.printDebugInfo();
        }
        this.commonDurationMs+= timelineEntity.getDurationMs();
        this.commonWidthDp+= timelineEntity.getWidth();
    }

    public Bitmap getBitmapByItemIndex(int index)
    {
        return this.framesCollage.get(index);
    }

    public String getNameByItemIndex(int index)
    {
        return this.namesCollage.get(index);
    }

    public TimelineEntity getTimelineEntityByItemIndex(int index)
    {
        return this.timelineEntities.get(index);
    }

    public void removeItemByIndex(int index)
    {
        this.framesCollage.remove(index);
        this.namesCollage.remove(index);
        if(isDebugMod)
        {
            Log.d("removeItemByIndex","Удален элемент!");
            this.timelineEntities.get(index).printDebugInfo();
        }
        this.commonDurationMs-= this.timelineEntities.get(index).getDurationMs();
        this.commonWidthDp-= this.timelineEntities.get(index).getWidth();
        this.timelineEntities.remove(index);
    }

    public void printDebugInfo(int index)
    {
        this.timelineEntities.get(index).printDebugInfo();
    }
    public void printDebugInfo(int indexBegin,int indexEnd)
    {
        for(int i= indexBegin;i < indexEnd;i++)
            this.timelineEntities.get(i).printDebugInfo();

    }

}
