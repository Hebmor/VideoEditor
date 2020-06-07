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


public class VideoTimelineController extends Fragment implements PlayerControllerCallback {

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(viewModRange)
            videoTimelineCutView = new VideoTimelineCutView(getActivity());
        else
            videoTimelineSplitView = new VideoTimelineSplitView(getActivity());

        init();
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
                frameLayout.addView(videoTimelineCutView);
            else
                frameLayout.addView(videoTimelineSplitView);

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

    private void init()
    {
        if(videoInfo != null) {

            if (videoTimelineCutView != null) {
                Bitmap frameCollage = ActionEditor.getFrameCollage(videoInfo.getPath(),videoInfo.getDuration(),
                        160,90,6);
                videoTimelineCutView.addItemInTimelineBody(frameCollage, videoInfo.getFilename());
                videoTimelineCutView.getSeekBarTimeline().setRange(0, videoInfo.getDuration(), 1000);
                videoTimelineCutView.getSeekBarTimeline().setProgress(0, videoInfo.getDuration());
                videoTimelineCutView.registerCallBack(this);
            }
            if (videoTimelineSplitView != null) {

                videoTimelineSplitView.addVideoInTimeline(videoInfo);
                videoTimelineSplitView.registerCallBack(this);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
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
        playerController.seekTo(positionMS);
    }

    @Override
    public int callingAddVideoToPlaylistPlayer(String path) {
        return playerController.addPlaylistByPath(path);
    }

    @Override
    public void callingMoveNextVideo(int beginPositionInMs) {
        playerController.moveNextVideo(beginPositionInMs);
    }

    @Override
    public void callingMovePrevVideo(int endPositionMs) {
        playerController.movePrevVideo(endPositionMs);
    }


}
