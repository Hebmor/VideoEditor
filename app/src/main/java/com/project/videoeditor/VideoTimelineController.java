package com.project.videoeditor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.project.videoeditor.codecs.ActionEditor;

import java.io.File;


public class VideoTimelineController extends Fragment implements VideoTimelineCutView.PlayerControllerCallback {

    private VideoInfo videoInfo;
    private PlayerController playerController;
    private VideoTimelineCutView videoTimelineCutView;
    private VideoTimelineSplitView videoTimelineSplitView;
    private boolean viewModRange = true;

    public VideoTimelineController(VideoInfo videoInfo, PlayerController playerController) {
        this.videoInfo = videoInfo;
        this.playerController = playerController;
    }
    public VideoTimelineController(VideoInfo videoInfo, PlayerController playerController, boolean viewModRange) {
        this.videoInfo = videoInfo;
        this.playerController = playerController;
        this.viewModRange = viewModRange;
    }
    public VideoTimelineController() {
    }


    public static VideoTimelineController newInstance(VideoInfo videoInfo, PlayerController playerController)
    {
        VideoTimelineController videoTimelineController = new VideoTimelineController(videoInfo,playerController);
        return videoTimelineController;
    }
    public static VideoTimelineController newInstance(VideoInfo videoInfo, PlayerController playerController, boolean viewModRange)
    {
        VideoTimelineController videoTimelineController = new VideoTimelineController(videoInfo,playerController,viewModRange);
        return videoTimelineController;
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.video_timeline, container, false);
            FrameLayout frameLayout = view.findViewById(R.id.timelineLayout);
            if(viewModRange)
            {
                videoTimelineCutView = new VideoTimelineCutView(getActivity());
                frameLayout.addView(videoTimelineCutView);
            }
            else
            {
                videoTimelineSplitView = new VideoTimelineSplitView(getActivity());
                frameLayout.addView(videoTimelineSplitView);
            }

            return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        if(savedInstanceState != null) {
            videoInfo = savedInstanceState.getParcelable("videoInfo");
        }
    }

    public void setPlayerController(PlayerController playerController) {
        this.playerController = playerController;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
    @Override
    public void onStart() {
        super.onStart();
        if(videoInfo != null) {
            String pathCollage = null;

            if (videoTimelineCutView != null) {
                Bitmap frameCollage = ActionEditor.getFrameCollage(videoInfo.getPath(),videoInfo.getDuration(),
                        160,90,6);
                videoTimelineCutView.addItemInTimelineBody(frameCollage, videoInfo.getFilename());
                videoTimelineCutView.getSeekBarTimeline().setRange(0, videoInfo.getDuration(), 1000);
                videoTimelineCutView.getSeekBarTimeline().setProgress(0, videoInfo.getDuration());
                videoTimelineCutView.registerCallBack(this);
            }
            if (videoTimelineSplitView != null) {
                Bitmap frameCollage = ActionEditor.getFrameCollage(videoInfo.getPath(),videoInfo.getDuration(),
                        160,90,12);
                videoTimelineSplitView.addItemInTimelineBody(frameCollage, videoInfo.getFilename(),
                        160 * 12,106,0,videoInfo.getDuration(),0,160 * 12,TimelineEntity.Type.SCROLLABLE);
                videoTimelineSplitView.registerCallBack(this);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("videoInfo", videoInfo);
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
        //videoFramesCollage.setImageBitmap(bitmap);
    }


    public void addVideoToTimeline(VideoInfo newVideo) throws InterruptedException {

        Bitmap bitmap = ActionEditor.getFrameCollage(videoInfo.getPath(),videoInfo.getDuration(),160,90,6);
        videoTimelineCutView.addItemInTimelineBody(bitmap,videoInfo.getFilename());
        //timelineAdapter.addItem(bitmap,newVideo.getFilename() + "1");
        //timelineAdapter.notifyDataSetChanged();
    }

    @Override
    public void callingUpdatePlayerControllerPosition(int positionMS) {
        playerController.getPlayer().seekTo((int)positionMS);
    }
}
