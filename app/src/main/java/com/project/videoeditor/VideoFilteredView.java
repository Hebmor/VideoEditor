package com.project.videoeditor;

import android.app.Activity;
import android.content.Context;
import android.media.MediaExtractor;
import android.media.MediaPlayer;

import com.uncorkedstudios.android.view.recordablesurfaceview.RecordableSurfaceView;

public class VideoFilteredView extends RecordableSurfaceView implements RecordableSurfaceView.RendererCallbacks {

    private final VideoSurfaceRecorder videoSurfaceRecorder;
    public VideoFilteredView(Context context) {
        super(context);

        videoSurfaceRecorder = new VideoSurfaceRecorder(context);
        setRendererCallbacks(this);
    }
    public VideoFilteredView(Context context, MediaExtractor mediaExtractor) {
        super(context);
        videoSurfaceRecorder = new VideoSurfaceRecorder(context);
        MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {

            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

                setFitToFillAspectRatio(mp, width, height);

            }
        };

        videoSurfaceRecorder.setExtractor(mediaExtractor);

        setRendererCallbacks(this);
    }
    @Override
    public void onSurfaceCreated() {
        videoSurfaceRecorder.onSurfaceCreated(null,null);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        videoSurfaceRecorder.onSurfaceChanged(null,width,height);
    }

    @Override
    public void onSurfaceDestroyed() {

    }

    @Override
    public void onContextCreated() {

    }

    @Override
    public void onPreDrawFrame() {

    }

    @Override
    public void onDrawFrame() {
        videoSurfaceRecorder.onDrawFrame(null);
    }

    private void setFitToFillAspectRatio(MediaPlayer mp, int videoWidth, int videoHeight)
    {
        if(mp != null)
        {
            Integer screenWidth = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getWidth();
            Integer screenHeight = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getHeight();
            android.view.ViewGroup.LayoutParams videoParams = getLayoutParams();


            if (videoWidth > videoHeight)
            {
                videoParams.width = screenWidth;
                videoParams.height = screenWidth * videoHeight / videoWidth;
            }
            else
            {
                videoParams.width = screenHeight * videoWidth / videoHeight;
                videoParams.height = screenHeight;
            }


            setLayoutParams(videoParams);
        }
    }
}
