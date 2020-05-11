package com.project.videoeditor;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.widget.MediaController;

import com.project.videoeditor.filters.BaseFilters;

import java.io.IOException;


import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class VideoSurfaceRenderer
        implements GLSurfaceView.Renderer {
    private static String TAG = "VideoRender";

    private PlayerController playerController;
    private BaseFilters currentFilter;
    private BaseFilters newFilter;
    private boolean isChangeFilter = false;
    SurfaceTexture.OnFrameAvailableListener onFrameAvailableListener;

    public VideoSurfaceRenderer(Context context,PlayerController playerController,BaseFilters filter) {

        this.currentFilter = filter;
        this.playerController = playerController;
        filter.setContext(context);
        filter.setPlayerController(playerController);
        onFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener()
        {

            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                currentFilter.onFrameAvailable(surfaceTexture);
            }
        };
    }


    @Override
    public void onDrawFrame(GL10 glUnused) {
        if(currentFilter != null)
        {
            if(isChangeFilter)
            {
                synchronized (this) {
                    try {
                        newFilter.recreate(currentFilter);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    currentFilter = newFilter;
                    isChangeFilter = false;
                    GLES20.glFinish();
                    return;
                }
            }
            currentFilter.onDrawFrame(glUnused);
        }

    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        if(currentFilter != null)
        {
            if(isChangeFilter)
            {
                synchronized (this) {
                    currentFilter = newFilter;
                    isChangeFilter = false;
                }
            }
            currentFilter.onSurfaceChanged(glUnused,width,height);
        }
        //onSurfaceChanged(glUnused, width,height);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {

        if(currentFilter != null)
        {
            if(isChangeFilter)
            {
                synchronized (this) {
                    currentFilter = newFilter;
                    isChangeFilter = false;

                }
            }
            currentFilter.onSurfaceCreated(glUnused,config);
            if(currentFilter.getSurfaceTexture() != null && currentFilter.getPlayerController() != null) {

                currentFilter.getSurfaceTexture().setOnFrameAvailableListener(onFrameAvailableListener);
                //currentFilter.getmMediaPlayer().start();
            }
        }


    }

    public void changeFilter(BaseFilters filter) {
        if(currentFilter != null)
        {
            newFilter = filter;
            newFilter.setPlayerController(playerController);
            isChangeFilter = true;
        }
    }

    public void setPlayerController(PlayerController playerController) {
    }
}
