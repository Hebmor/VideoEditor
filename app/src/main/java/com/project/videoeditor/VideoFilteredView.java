package com.project.videoeditor;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;

import com.project.videoeditor.VideoSurfaceRenderer;
import com.uncorkedstudios.android.view.recordablesurfaceview.RecordableSurfaceView;

public class VideoFilteredView extends RecordableSurfaceView implements RecordableSurfaceView.RendererCallbacks {

    private final VideoSurfaceRenderer videoSurfaceRenderer;
    public VideoFilteredView(Context context) {
        super(context);

        videoSurfaceRenderer = new VideoSurfaceRenderer(context);
        setRendererCallbacks(this);
    }
    public VideoFilteredView(Context context, MediaPlayer mediaPlayer) {
        super(context);

        videoSurfaceRenderer = new VideoSurfaceRenderer(context);
        MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {

            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

                setFitToFillAspectRatio(mp, width, height);

            }
        };
        mediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        videoSurfaceRenderer.setMediaPlayer(mediaPlayer);

        setRendererCallbacks(this);
    }
    @Override
    public void onSurfaceCreated() {
        videoSurfaceRenderer.onSurfaceCreated(null,null);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        videoSurfaceRenderer.onSurfaceChanged(null,width,height);
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
        videoSurfaceRenderer.onDrawFrame(null);
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
