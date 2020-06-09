package com.project.videoeditor;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;
import com.jaygoo.widget.SeekBar;

public class VideoTimelineCutView extends LinearLayout{

    private RangeSeekBar seekBarTimeline;
    private RecyclerView timelineBody;
    private LinearLayoutManager layoutManager;
    private SeekBar SBR;
    private SeekBar SBL;
    private ImageView videoFramesCollage;

    private Button saveButton;
    private Button speedButton;
    private Button extractFrames;
    private Button extractAudio;

    private TimelineAdapter timelineAdapter;
    private TypedArray a;

    PlayerControllerCallback playerControllerCallback;

    public interface IClickСutEdit
    {
        void clickOpenSavePage(View view, float beginMs, float endMs);
        void clickExtractFrames(View view, float beginMs, float endMs);
        void clickExtractAudio(View view, float beginMs, float endMs);
        void clickChangeSpeed(View view, float beginMs, float endMs, float speed);
    }

    private IClickСutEdit callbackClickEdit;



    public void registerIClickCutEditCallback(IClickСutEdit callback)
    {
        this.callbackClickEdit = callback;
    }
    public void registerCallBack(PlayerControllerCallback playerControllerCallback){
        this.playerControllerCallback = playerControllerCallback;
    }

    private float prevLeftValue = 0;
    private float prevRightValue = 0;

    public VideoTimelineCutView(Context context) {
        super(context);
        inflate(context, R.layout.timeline_cut,this);

        seekBarTimeline = findViewById(R.id.seekBarTimeline);
        timelineBody = findViewById(R.id.timeline_recycler);
        init(null);
        registerButton();
    }
    public VideoTimelineCutView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.timeline_cut,this);

        seekBarTimeline = findViewById(R.id.seekBarTimeline);
        timelineBody = findViewById(R.id.timeline_recycler);
        videoFramesCollage = findViewById(R.id.videoCollage);
        LayoutParams lp = new LayoutParams(400,
                40);
        videoFramesCollage.setLayoutParams(lp);
        init(attrs);
        registerButton();
    }
    private void init(AttributeSet attrs) {

        if(attrs != null) {
            a = getContext().obtainStyledAttributes(
                    attrs, R.styleable.VideoTimelineCutView);
        }

        SBR = seekBarTimeline.getRightSeekBar();
        SBL = seekBarTimeline.getLeftSeekBar();
        seekBarTimeline.setSeekBarMode(RangeSeekBar.SEEKBAR_MODE_RANGE);
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        timelineBody.setLayoutManager(layoutManager);
        timelineBody.setAdapter(timelineAdapter);

        timelineAdapter = new TimelineAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        timelineBody.setLayoutManager(layoutManager);
        timelineBody.setAdapter(timelineAdapter);
        seekBarTimeline.setOnRangeChangedListener(new OnRangeChangedListener()
        {
            int pathValue = 0;
            int speedTemp = 0;
            float boostValue = 1;
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                if (isFromUser) {
                    updateTimeline(leftValue,rightValue);
                }
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {


            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }
        });

        getRootView().post(new Runnable() {
            @Override
            public void run() {

                seekBarTimeline.setProgressBottom(timelineBody.getBottom());
                seekBarTimeline.setProgressTop(timelineBody.getTop() - 100);
                seekBarTimeline.setProgressColor(ContextCompat.getColor(getContext(),R.color.colorPrimaryDark));
                seekBarTimeline.setProgressHeight(200);
                seekBarTimeline.invalidate();

            }
        });
    }

    private void registerButton()
    {
        saveButton = findViewById(R.id.buttonEncodeCut);
        extractFrames = findViewById(R.id.buttonExtractFrames);
        extractAudio = findViewById(R.id.buttonExtractAudio);
        speedButton = findViewById(R.id.buttonSpeed);

        saveButton.setOnClickListener(this::clickSave);
        extractFrames.setOnClickListener(this::clickExtractFrames);
        extractAudio.setOnClickListener(this::clickExtractAudio);
    }

    public void addItemInTimelineBody(Bitmap frameCollage, String nameCollage)
    {
        timelineAdapter.addItem(frameCollage,nameCollage);
        timelineAdapter.notifyDataSetChanged();
    }

    private void updateTimeline(float leftValue, float rightValue)
    {
        if(prevLeftValue != leftValue) {
            long millisSBL = (long) SBL.getProgress() % 1000;
            long secondSBL = ((long) SBL.getProgress() / 1000) % 60;
            long minuteSBL = ((long) SBL.getProgress() / (1000 * 60)) % 60;
            long hourSBL = ((long)SBL.getProgress() / (1000 * 60 * 60)) % 24;
            prevLeftValue = leftValue;
            if(hourSBL > 0)
                SBL.setIndicatorText(String.format("%02d:%02d:%02d.%d",hourSBL,minuteSBL, secondSBL, millisSBL));
            else
                SBL.setIndicatorText(String.format("%02d:%02d.%d",minuteSBL, secondSBL, millisSBL));

            playerControllerCallback.callingUpdatePlayerControllerPosition((int)leftValue);
        }
        if(prevRightValue != rightValue) {
            long millisSBR = (long) SBR.getProgress() % 1000;
            long secondSBR = ((long) SBR.getProgress() / 1000) % 60;
            long minuteSBR = ((long) SBR.getProgress() / (1000 * 60)) % 60;
            long hourSBR = ((long)SBR.getProgress() / (1000 * 60 * 60)) % 24;
            prevRightValue = rightValue;
            if(hourSBR > 0)
                SBR.setIndicatorText(String.format("%02d:%02d:%02d.%d",hourSBR,minuteSBR, secondSBR, millisSBR));
            else
                SBR.setIndicatorText(String.format("%02d:%02d.%d",minuteSBR, secondSBR, millisSBR));

            playerControllerCallback.callingUpdatePlayerControllerPosition((int)rightValue);

        }
    }

    public float getLeftValue() {
        return prevLeftValue;
    }

    public float getRightValue() {
        return prevRightValue;
    }

    public RangeSeekBar getSeekBarTimeline() {
        return seekBarTimeline;
    }

    public void clickSave(View view)
    {
        callbackClickEdit.clickOpenSavePage(view,getLeftValue(),getRightValue());
    }

    public void clickExtractFrames(View view)
    {
        callbackClickEdit.clickExtractFrames(view,getLeftValue(),getRightValue());
    }

    public void clickExtractAudio(View view)
    {
        callbackClickEdit.clickExtractAudio(view,getLeftValue(),getRightValue());
    }

    public void clickChangeSpeed(View view)
    {
        callbackClickEdit.clickChangeSpeed(view,getLeftValue(),getRightValue(),0);
    }

}
