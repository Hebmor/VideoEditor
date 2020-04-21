package com.project.videoeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.RelativeLayout;

import java.io.IOException;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageBoxBlurFilter;

public class FiltersView extends TextureView implements TextureView.SurfaceTextureListener {

    private MediaPlayer mMediaPlayer;
    private String videoPath;
    private GPUImage filters;
    private float framerate;
    public FiltersView(Context context,String path,float framerate) {
        super(context);
        this.videoPath = path;
        this.framerate = framerate;
        this.setSurfaceTextureListener(this);
        filters = new GPUImage(context);


    }
    private void updateTextureViewSize(int viewWidth, int viewHeight) {
        this.setLayoutParams(new RelativeLayout.LayoutParams(viewWidth, viewHeight));
    }
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        Surface surface = new Surface(surfaceTexture);
        try {
            filters.setFilter(new GPUImageBoxBlurFilter());
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(getContext(),Uri.parse(videoPath));

            mMediaPlayer.setSurface(surface);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepareAsync();

            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });

        } catch (IOException e) {
            Log.d("TAG", e.getMessage());
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture SurfaceTexture) {

        Matrix matrix = new Matrix();
        filters.setImage(this.getBitmap());
        Canvas canvas = lockCanvas();
        if(canvas!=null) {
            canvas.drawBitmap(filters.getBitmapWithFilterApplied(), matrix, null);
            unlockCanvasAndPost(canvas);
        }
    }

    /*
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        DrawFilteredVideoThread = new DrawFilteredVideoThread(holder,videoPath);
        DrawFilteredVideoThread.setFramerate(framerate);
        DrawFilteredVideoThread.setRunning(true);
        DrawFilteredVideoThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        // завершаем работу потока
        DrawFilteredVideoThread.setRunning(false);
        while (retry) {
            try {
                DrawFilteredVideoThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // если не получилось, то будем пытаться еще и еще
            }
        }
    }*/
}
