package com.project.videoeditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;
import android.view.SurfaceHolder;

public class DrawFilteredVideoThread extends Thread{

    private boolean runFlag = false;
    private SurfaceHolder surfaceHolder;
    private Bitmap currentBitmap;
    private MediaMetadataRetriever mediaMetadataRetriever;
    private long prevTime;
    private int framePos;
    private Matrix matrix;
    private float framerate;

    public void setFramerate(float framerate) {
        this.framerate = framerate;
    }



    public DrawFilteredVideoThread(SurfaceHolder surfaceHolder,String path)
    {
        this.surfaceHolder  = surfaceHolder;
        this.mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        prevTime = System.currentTimeMillis();
        framePos = 0;
        matrix = new Matrix();
    }
    public void setRunning(boolean run) {
        runFlag = run;
    }

    @Override
    public void run() {
        Canvas canvas;
        while (runFlag) {
            long now = System.currentTimeMillis();
            long elapsedTime = now - prevTime;
                currentBitmap = mediaMetadataRetriever.getFrameAtIndex(framePos++);
                prevTime = now;
                Log.d("RENDER PROCESS", String.valueOf(framePos));

                canvas = null;
                try {
                    // получаем объект Canvas и выполняем отрисовку
                    canvas = surfaceHolder.lockCanvas(null);
                    synchronized (surfaceHolder) {
                       // canvas.drawColor(Color.BLACK);
                        if (currentBitmap != null)
                            canvas.drawBitmap(currentBitmap,matrix,null);
                    }
                } finally {
                    if (canvas != null) {
                        // отрисовка выполнена. выводим результат на экран
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }

        }

    }

}
