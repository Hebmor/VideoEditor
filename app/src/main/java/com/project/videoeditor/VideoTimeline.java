package com.project.videoeditor;

import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;
import com.jaygoo.widget.SeekBar;

import java.io.File;

import static android.app.Activity.RESULT_OK;


public class VideoTimeline extends Fragment {


    private  RangeSeekBar seekBar;
    private VideoEditBar videoEditBar;

    public SeekBar getSBR() {
        return SBR;
    }

    public SeekBar getSBL() {
        return SBL;
    }

    private  SeekBar SBR;
    private SeekBar SBL;
    private ImageView videoFramesCollage;
    private VideoInfo videoInfo;
    private VideoView pVideoView;


    private float tempLeftValue = 0;
    private float tempRightValue = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.video_timeline_fragment, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        seekBar = view.findViewById(R.id.seekBarVideo);
        videoEditBar = view.findViewById(R.id.rectVideo);
        videoFramesCollage = view.findViewById(R.id.videoFramesCollage);
        //linearLayout = view.findViewById(R.id.linear_layout);



        SBR = seekBar.getRightSeekBar();
        SBL = seekBar.getLeftSeekBar();
        videoEditBar.setmLeftBackground((int) ((seekBar.getProgressWidth() / seekBar.getMaxProgress() * SBL.getProgress()) + seekBar.getPaddingLeft() + SBL.getThumbWidth() / 2));
        videoEditBar.setmRightBackground((int) ((seekBar.getProgressWidth() / seekBar.getMaxProgress() * SBR.getProgress()) + seekBar.getPaddingLeft() + SBR.getThumbWidth() / 2));
        videoEditBar.invalidate();

        seekBar.setOnRangeChangedListener(new OnRangeChangedListener()
        {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                if (isFromUser) {

                    updateTimeline(leftValue,rightValue);
                }
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {
                updateTimeline(SBL.getProgress(),SBR.getProgress());
            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }
        });

    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO: Use the ViewModel
    }
    @Override
    public void onStart() {

        super.onStart();
        pVideoView = getActivity().findViewById(R.id.videoView_EditVideo);
        seekBar.setRange(0,videoInfo.getDuration(),10);
        updateTimeline(0,videoInfo.getDuration());
        //pVideoView = getActivity().findViewById(R.id.videoView);
    }
    public void setVideoInfo(VideoInfo videoInfo)
    {
        this.videoInfo = videoInfo;
    }
    public void setFramesFromVideo(String PathToFrameСollage)
    {
        File image = new File(PathToFrameСollage);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
        videoFramesCollage.setImageBitmap(bitmap);
    }
    private void updateTimeline(float leftValue, float rightValue)
    {
        videoEditBar.setmLeftBackground((int) ((seekBar.getProgressWidth() / seekBar.getMaxProgress() * SBL.getProgress()) + seekBar.getPaddingLeft() + SBL.getThumbWidth() / 2));
        videoEditBar.setmRightBackground((int) ((seekBar.getProgressWidth() / seekBar.getMaxProgress() * SBR.getProgress()) + seekBar.getPaddingLeft() + SBR.getThumbWidth() / 2));

        if(tempLeftValue != leftValue) {
            long millisSBL = (long) SBL.getProgress() % 1000;
            long secondSBL = ((long) SBL.getProgress() / 1000) % 60;
            long minuteSBL = ((long) SBL.getProgress() / (1000 * 60)) % 60;
            // long hourSBL = ((long)SBL.getProgress() / (1000 * 60 * 60)) % 24;
            tempLeftValue = leftValue;
            SBL.setIndicatorText(String.format("%02d:%02d.%d",minuteSBL, secondSBL, millisSBL));
            pVideoView.seekTo((int)leftValue);
        }
        if(tempRightValue != rightValue) {
            long millisSBR = (long) SBR.getProgress() % 1000;
            long secondSBR = ((long) SBR.getProgress() / 1000) % 60;
            long minuteSBR = ((long) SBR.getProgress() / (1000 * 60)) % 60;
            // long hourSBR = ((long)SBR.getProgress() / (1000 * 60 * 60)) % 24;
            tempRightValue = rightValue;
            SBR.setIndicatorText(String.format("%02d:%02d.%d",minuteSBR, secondSBR, millisSBR));
            pVideoView.seekTo((int)rightValue);
        }
        videoEditBar.invalidate();
    }

}
