package com.project.videoeditor;

import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.media.TimedMetaData;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.MediaController;

import com.project.videoeditor.filters.DefaultFilter;
import com.project.videoeditor.filters.FiltersHandler;

import java.io.IOException;


public class VideoFilteredView extends GLSurfaceView implements MediaPlayer.OnPreparedListener,
        MediaController.MediaPlayerControl {

    private VideoSurfaceRenderer videoSurfaceRenderer;
    private MediaPlayer mMediaPlayer;
    private MediaController mediaController;
    private Handler handler = new Handler();

    public VideoFilteredView(Context context, MediaPlayer mediaPlayer) throws Exception {
        super(context);
        setEGLContextClientVersion(2);
        this.mMediaPlayer  = mediaPlayer;
        videoSurfaceRenderer = new VideoSurfaceRenderer(context,mediaPlayer,new DefaultFilter());

        MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {

            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

                setFitToFillAspectRatio(mp, width, height);

            }
        };
        this.mMediaPlayer.setOnTimedMetaDataAvailableListener(new MediaPlayer.OnTimedMetaDataAvailableListener() {
            @Override
            public void onTimedMetaDataAvailable(MediaPlayer mp, TimedMetaData data) {
                Log.d("onTimedMetaDataAvailable","time!");
            }
        });
        this.mMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        this.mMediaPlayer.setOnPreparedListener(this::onPrepared);
        this.mMediaPlayer.setScreenOnWhilePlaying(true);
        this.mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                Log.d("TEST BUFFERING","------------");
            }
        });
        videoSurfaceRenderer.setMediaPlayer(this.mMediaPlayer);

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(mediaController != null){
                    mediaController.show();
                }
                return false;
            }
        });

        this.setRenderer(videoSurfaceRenderer);
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
        mediaController = new MediaController(getContext());
        videoSurfaceRenderer.onSurfaceCreated(null,null);
    }
    private void setFitToFillAspectRatio(MediaPlayer mp, int videoWidth, int videoHeight) {
        if (mp != null) {
            Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            Integer screenWidth = this.getWidth();
            Integer screenHeight = this.getHeight();
            android.view.ViewGroup.LayoutParams videoParams = this.getLayoutParams();

            float videoProportion = (float) videoWidth / (float) videoHeight;
            float screenProportion = (float) screenWidth / (float) screenHeight;

            if (videoProportion > screenProportion) {
                videoParams.width = screenWidth;
                videoParams.height = (int) ((float) screenWidth / videoProportion);
            } else {
                videoParams.width = (int) (videoProportion * (float) screenHeight);
                videoParams.height = screenHeight;
            }
            // Commit the layout parameters
            setLayoutParams(videoParams);

        }
    }
    public void changeFragmentShader(FiltersHandler.nameFilters filter) throws IOException {
        videoSurfaceRenderer.changeFilter(FiltersHandler.getFiltersByName(filter,getContext()));

    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        mediaController.setMediaPlayer(this);
        mediaController.setAnchorView(this);
        handler.post(new Runnable() {

            public void run() {
                mediaController.setEnabled(true);
                mediaController.show();
            }
        });

    }

    @Override
    public void start() {
        mMediaPlayer.start();
    }

    @Override
    public void pause() {
        mMediaPlayer.pause();
    }

    @Override
    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        mMediaPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return mMediaPlayer.getAudioSessionId();
    }
}
