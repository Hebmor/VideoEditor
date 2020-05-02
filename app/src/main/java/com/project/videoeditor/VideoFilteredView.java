package com.project.videoeditor;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.view.SurfaceHolder;

import java.io.IOException;


public class VideoFilteredView extends GLSurfaceView {

    private VideoSurfaceRenderer videoSurfaceRenderer;


    public VideoFilteredView(Context context, MediaPlayer mediaPlayer) throws Exception {
        super(context);
        setEGLContextClientVersion(2);
        videoSurfaceRenderer = new VideoSurfaceRenderer(context,mediaPlayer,new DefaultFilter());

        MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {

            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

                setFitToFillAspectRatio(mp, width, height);

            }
        };
        mediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        videoSurfaceRenderer.setMediaPlayer(mediaPlayer);
        this.setRenderer(videoSurfaceRenderer);
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
        videoSurfaceRenderer.onSurfaceCreated(null,null);
    }
    private void setFitToFillAspectRatio(MediaPlayer mp, int videoWidth, int videoHeight) {
        if (mp != null) {
            Integer screenWidth = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getWidth();
            Integer screenHeight = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getHeight();
            android.view.ViewGroup.LayoutParams videoParams = getLayoutParams();


            if (videoWidth > videoHeight) {
                videoParams.width = screenWidth;
                videoParams.height = screenWidth * videoHeight / videoWidth;
            } else {
                videoParams.width = screenHeight * videoWidth / videoHeight;
                videoParams.height = screenHeight;
            }


            setLayoutParams(videoParams);
        }
    }
    public void changeFragmentShader(FiltersHandler.nameFilters filter) throws IOException {
        videoSurfaceRenderer.changeFilter(FiltersHandler.getFiltersByName(filter,getContext()));

    }
}
