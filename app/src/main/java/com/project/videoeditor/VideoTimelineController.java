package com.project.videoeditor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.exoplayer2.PlaybackParameters;
import com.google.gson.Gson;
import com.project.videoeditor.activity.MainEditor;
import com.project.videoeditor.activity.SaveVideoActivity;
import com.project.videoeditor.codecs.ActionEditor;
import com.project.videoeditor.dialogs.SpeedDialog;
import com.project.videoeditor.filters.BaseFilter;
import com.project.videoeditor.filters.FiltersFactory;
import com.project.videoeditor.filters.ImageKernelFilter;
import com.project.videoeditor.support.SupportUtil;

import java.io.File;


public class VideoTimelineController extends Fragment implements PlayerControllerCallback, VideoTimelineCutView.IClickСutEdit,VideoTimelineSplitView.IClickSplitEdit {

    private VideoInfo videoInfo;
    private PlayerController playerController;
    private VideoTimelineCutView videoTimelineCutView;
    private VideoTimelineSplitView videoTimelineSplitView;
    private SpeedDialog speedDialog;


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

                if (viewModRange) {
                    if ((videoTimelineCutView.getParent() != null))
                        ((ViewGroup)videoTimelineCutView.getParent()).removeView(videoTimelineCutView);
                    frameLayout.addView(videoTimelineCutView);
                }
                else {
                    if ((videoTimelineSplitView.getParent() != null))
                        ((ViewGroup)videoTimelineSplitView.getParent()).removeView(videoTimelineSplitView);
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
                videoTimelineCutView.registerIClickCutEditCallback(this);
            }
            if (videoTimelineSplitView != null) {

                videoTimelineSplitView.addVideoInTimeline(videoInfo);
                videoTimelineSplitView.registerCallBack(this);
                videoTimelineSplitView.registerIClickSplitEditCallback(this);
            }
            speedDialog = new SpeedDialog(getActivity());
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
    public void moveVideoByVideoIndex(int index) {
        playerController.moveByVideoIndex(index);
    }


    @Override
    public void clickOpenSavePage(View view, float beginMs, float endMs) {
        if(view instanceof Button) {

            Intent intent = new Intent(getContext(), SaveVideoActivity.class);
            intent.putExtra(VideoInfo.class.getCanonicalName(), videoInfo);
            BaseFilter filter = ((MainEditor)getContext()).getCurrentFilter();

            intent.putExtra("beginValue", beginMs);
            intent.putExtra("endValue",endMs);
            intent.putExtra("filter",filter);
            intent.putExtra("videoSpeed",speedDialog.getSpeedValue());

            ((Activity)getContext()).startActivity(intent);
        }
    }

    @Override
    public void clickExtractFrames(View view, float beginMs, float endMs) {
        try {
            runExtractFrames(beginMs,endMs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clickExtractAudio(View view, float beginMs, float endMs) {

        try {
            runExtractAudio(beginMs, endMs);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void clickChangeSpeed(View view, float beginMs, float endMs, float speed) {
        speedDialog.startLoadingDialog(playerController);

    }

    public void runExtractFrames(float beginMs, float endMs) throws Exception {
        File framesFolder = SupportUtil.CreateFolder(getContext()
                .getExternalFilesDir(null).getPath() + "/" +"ExtractFrames" + "/" + videoInfo.getFilename());
        ActionEditor.extractFrames(videoInfo.getPath(),framesFolder.getCanonicalPath() + "/frame%0d.png",
                (int)beginMs, (int)endMs,0);
    }

    public void runExtractAudio(float beginMs, float endMs) throws Exception {
        String outFilename = SupportUtil.changeFormatFilename(videoInfo.getFilename(),"mp3");
        File framesFolder = SupportUtil.CreateFolder(getContext()
                .getExternalFilesDir(null).getPath() + "/" +"ExtractAudio");
        ActionEditor.extractAudio(videoInfo.getPath(),framesFolder.getCanonicalPath()  + "/" + outFilename,(long)beginMs,(long)endMs);
    }

    @Override
    public void clickExtractFrame(View view, String path, String filename, float frametimeInMs) {
        try {
            File framesFolder = SupportUtil.CreateFolder(getContext()
                    .getExternalFilesDir(null).getPath() + "/" +"ExtractFrames" + "/" + filename);
            ActionEditor.extractFrames(path,framesFolder.getCanonicalPath() + "/frame%0d.png",(long) frametimeInMs,0,1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clickSplit(View view) {

    }

    @Override
    public void clickSave(View view) {

    }
}
