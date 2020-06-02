package com.project.videoeditor;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class VideoTimelineSplitView extends LinearLayout implements RecyclerView.OnScrollChangeListener{

    private ImageView timelinePointer;
    private RecyclerView timelineBody;
    private VideoAdapter timelineAdapter;
    private TextView indicatorTimeline;
    private TypedArray a;

    private int minMs = 0;
    private int maxMs = 0;
    private int scrollPath = 0;
    private int prevMsValue = 0;
    private int scrollRange = 0;
    @Override
    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

        int value = 0;
        scrollPath+= oldScrollX;
        if(timelineBody.computeHorizontalScrollRange() > 0 || scrollPath == 0) {
            scrollRange = timelineBody.computeHorizontalScrollRange();
            if(scrollRange > 0) {
                value = Math.abs(scrollPath) * maxMs / scrollRange;
            }
            updateTimeline(value);
        }
        else if(timelineBody.computeHorizontalScrollRange() == 0)
        {
            if(Math.abs(scrollPath) > scrollRange && scrollRange != 0)
                updateTimeline(maxMs);
            if(scrollPath == 0)
            {
                updateTimeline(minMs);
            }
        }
        Log.d("value", String.valueOf(value));
        Log.d("scrollPath", String.valueOf(scrollPath));
        Log.d("oldScrollX", String.valueOf(oldScrollX));
        Log.d("timelineBody.computeHorizontalScrollRange()", String.valueOf(timelineBody.computeHorizontalScrollRange()));

    }

    interface PlayerControllerCallback {
        void callingUpdatePlayerControllerPosition(int positionMS);
    }

    VideoTimelineCutView.PlayerControllerCallback playerControllerCallback;

    public void registerCallBack(VideoTimelineCutView.PlayerControllerCallback playerControllerCallback){
        this.playerControllerCallback = playerControllerCallback;
    }
    public VideoTimelineSplitView(Context context) {
        super(context);
        inflate(context,R.layout.timeline_split,this);

        timelinePointer = findViewById(R.id.timelinePointer);
        timelineBody = findViewById(R.id.timeline_recycler);
        indicatorTimeline = findViewById(R.id.indicatorTimeline);
        init(null);
    }
    public VideoTimelineSplitView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
         inflate(context,R.layout.timeline_split,this);

        timelinePointer = findViewById(R.id.timelinePointer);
        timelineBody = findViewById(R.id.timeline_recycler);
        indicatorTimeline = findViewById(R.id.indicatorTimeline);
        init(attrs);
    }
    private void init(AttributeSet attrs) {

        if(attrs != null) {
            a = getContext().obtainStyledAttributes(
                    attrs, R.styleable.VideoTimelineSplitView);
            Drawable imagePointer = a.getDrawable(R.styleable.VideoTimelineSplitView_timelinePointer);
            timelinePointer.setImageDrawable(imagePointer);
        }
        timelineAdapter = new VideoAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        timelineBody.setLayoutManager(layoutManager);
        timelineBody.setAdapter(timelineAdapter);
        timelineBody.setOnScrollChangeListener(this);

        getRootView().post(new Runnable() {
            @Override
            public void run() {
             ImageView videoCollage =  findViewById(R.id.videoCollage);
             LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,106);
             videoCollage.setLayoutParams(lp);
             videoCollage.invalidate();
            }
        });
    }
    public void addItemInTimelineBody(Bitmap frameCollage, String nameCollage)
    {
        timelineAdapter.addItem(frameCollage,nameCollage);
        timelineAdapter.notifyDataSetChanged();
    }
    public void setRange(int minMs,int maxMs)
    {
        this.minMs = minMs;
        this.maxMs = maxMs;
    }
    private void updateTimeline(int value)
    {
        String indicatorText = "";
        if(prevMsValue != value) {
            int millisSBL = value % 1000;
            int secondSBL = (value / 1000) % 60;
            int minuteSBL = (value/ (1000 * 60)) % 60;
            int hourSBL = (value / (1000 * 60 * 60)) % 24;
            prevMsValue = value;
            if(hourSBL > 0)
                indicatorText = String.format("%02d:%02d:%02d.%d",hourSBL,minuteSBL, secondSBL, millisSBL);
            else
                indicatorText = String.format("%02d:%02d.%d",minuteSBL, secondSBL, millisSBL);

            indicatorTimeline.setText(indicatorText);
            playerControllerCallback.callingUpdatePlayerControllerPosition((int)value);
        }
    }
}
