package com.project.videoeditor;

import androidx.core.content.ContextCompat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageView;

import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;
import com.jaygoo.widget.SeekBar;
import com.project.videoeditor.codecs.ActionEditor;

import java.io.File;


public class VideoTimeline extends Fragment {

    private RangeSeekBar seekBar;
    private RecyclerView recyclerTimeline;
    private LinearLayoutManager layoutManager;
    private SeekBar SBR;
    private SeekBar SBL;
    private ImageView videoFramesCollage;
    private VideoInfo videoInfo;
    private boolean rangeMode = false;
    private Button saveButton;

    private PlayerController playerController;

    private float tempLeftValue = 0;
    private float tempRightValue = 0;

    public VideoTimeline(VideoInfo videoInfo, PlayerController playerController) {
        this.videoInfo = videoInfo;
        this.playerController = playerController;
    }
    public VideoTimeline(VideoInfo videoInfo, PlayerController playerController,boolean rangeMode) {
        this.videoInfo = videoInfo;
        this.playerController = playerController;
        this.rangeMode = rangeMode;
    }
    public SeekBar getSBR() {
        return SBR;
    }

    public SeekBar getSBL() {
        return SBL;
    }

    public boolean isRangeMode() {
        return rangeMode;
    }

    public void setRangeMode(boolean rangeMode) {
        this.rangeMode = rangeMode;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if(rangeMode)
            return inflater.inflate(R.layout.video_timeline_cut, container, false);
        else
            return inflater.inflate(R.layout.video_timeline_split, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        seekBar = view.findViewById(R.id.seekBarVideo);
        SBR = seekBar.getRightSeekBar();
        SBL = seekBar.getLeftSeekBar();

        if(rangeMode)
            seekBar.setSeekBarMode(RangeSeekBar.SEEKBAR_MODE_RANGE);
        else {
            seekBar.setSeekBarMode(RangeSeekBar.SEEKBAR_MODE_SINGLE);
            seekBar.setProgressWidth(2000);
            SBL.setThumbDrawableId(R.drawable.ic_shapes);
            SBL.setThumbHeight(500);
        }

        recyclerTimeline = (RecyclerView) view.findViewById(R.id.timeline_recycler);

        String pathCollage = ActionEditor.GenFrameCollage(videoInfo.getPath(),getActivity());
        Bitmap bitmap = getBitmapByPath(pathCollage);
        VideoAdapter videoAdapter = new VideoAdapter(bitmap,videoInfo.getFilename());
        layoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        recyclerTimeline.setLayoutManager(layoutManager);
        recyclerTimeline.setAdapter(videoAdapter);


        seekBar.setOnRangeChangedListener(new OnRangeChangedListener()
        {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                if (isFromUser) {

                    if(rangeMode)
                        updateTimeline(leftValue,rightValue);
                    else
                        updateTimeline(leftValue);
                }
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {
                if(rangeMode)
                    updateTimeline(SBL.getProgress(),SBR.getProgress());
                else
                    updateTimeline(SBL.getProgress());
            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }
        });

            getView().post(new Runnable() {
                @Override
                public void run() {

                    if(rangeMode) {
                        seekBar.setProgressBottom(recyclerTimeline.getBottom());
                        seekBar.setProgressTop(recyclerTimeline.getTop() - 48);
                        seekBar.setProgressColor(ContextCompat.getColor(getContext(),R.color.colorPrimaryDark));
                        seekBar.setProgressHeight(200);
                        seekBar.invalidate();
                    }
                }
            });
    }

    public void setPlayerController(PlayerController playerController) {
        this.playerController = playerController;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO: Use the ViewModel
    }
    @Override
    public void onStart() {

        super.onStart();

        seekBar.setRange(0,videoInfo.getDuration(),1000);
        seekBar.setProgress(0,videoInfo.getDuration());
    }
    public void setVideoInfo(VideoInfo videoInfo)
    {
        this.videoInfo = videoInfo;
    }
    private Bitmap getBitmapByPath(String PathToFrameСollage)
    {
        File image = new File(PathToFrameСollage);
        if(image.canRead())
        {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            return BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
        }
        return null;
    }
    public void setFramesFromVideo(String PathToFrameСollage)
    {
        File image = new File(PathToFrameСollage);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
        videoFramesCollage.setImageBitmap(bitmap);
    }
    private void updateTimeline(float value)
    {
        if(tempLeftValue != value) {
            long millisSBL = (long) SBL.getProgress() % 1000;
            long secondSBL = ((long) SBL.getProgress() / 1000) % 60;
            long minuteSBL = ((long) SBL.getProgress() / (1000 * 60)) % 60;
            long hourSBL = ((long)SBL.getProgress() / (1000 * 60 * 60)) % 24;
            tempLeftValue = value;
            if(hourSBL > 0)
                SBL.setIndicatorText(String.format("%02d:%02d:%02d.%d",hourSBL,minuteSBL, secondSBL, millisSBL));
            else
                SBL.setIndicatorText(String.format("%02d:%02d.%d",minuteSBL, secondSBL, millisSBL));

            playerController.getPlayer().seekTo((int)value);
        }
    }
    private void updateTimeline(float leftValue, float rightValue)
    {
        if(tempLeftValue != leftValue) {
            long millisSBL = (long) SBL.getProgress() % 1000;
            long secondSBL = ((long) SBL.getProgress() / 1000) % 60;
            long minuteSBL = ((long) SBL.getProgress() / (1000 * 60)) % 60;
            long hourSBL = ((long)SBL.getProgress() / (1000 * 60 * 60)) % 24;
            tempLeftValue = leftValue;
            if(hourSBL > 0)
                SBL.setIndicatorText(String.format("%02d:%02d:%02d.%d",hourSBL,minuteSBL, secondSBL, millisSBL));
            else
                SBL.setIndicatorText(String.format("%02d:%02d.%d",minuteSBL, secondSBL, millisSBL));

            playerController.getPlayer().seekTo((int)leftValue);
        }
        if(tempRightValue != rightValue) {
            long millisSBR = (long) SBR.getProgress() % 1000;
            long secondSBR = ((long) SBR.getProgress() / 1000) % 60;
            long minuteSBR = ((long) SBR.getProgress() / (1000 * 60)) % 60;
            long hourSBR = ((long)SBR.getProgress() / (1000 * 60 * 60)) % 24;
            tempRightValue = rightValue;
            if(hourSBR > 0)
                SBR.setIndicatorText(String.format("%02d:%02d:%02d.%d",hourSBR,minuteSBR, secondSBR, millisSBR));
            else
                SBR.setIndicatorText(String.format("%02d:%02d.%d",minuteSBR, secondSBR, millisSBR));
            playerController.getPlayer().seekTo((int)rightValue);
        }
    }

    public float getLeftValue() {
        return tempLeftValue;
    }

    public float getRightValue() {
        return tempRightValue;
    }
}
