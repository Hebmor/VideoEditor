package com.project.videoeditor;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Parcelable;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import androidx.annotation.Nullable;

import com.project.videoeditor.filters.BaseFilter;
import com.project.videoeditor.filters.DefaultFilter;
import com.project.videoeditor.filters.FiltersHandler;

import java.io.IOException;


public class VideoFilteredView extends GLSurfaceView {

    private VideoSurfaceRenderer videoSurfaceRenderer;
    private PlayerController playerController;
    private Handler handler = new Handler();
    private static boolean  saveFlag = false;

    public VideoFilteredView(Context context, PlayerController playerController) throws Exception {
        super(context);
        this.setEGLContextClientVersion(2);
        this.setDebugFlags(DEBUG_CHECK_GL_ERROR);
        this. setPreserveEGLContextOnPause(true);
        this.playerController  = playerController;
        videoSurfaceRenderer = new VideoSurfaceRenderer(context,playerController,new DefaultFilter());


        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(playerController != null){
                    playerController.getPlayerControlView().show();
                }
                return false;
            }
        });

        this.setRenderer(videoSurfaceRenderer);
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
        //videoSurfaceRenderer.onSurfaceCreated(null,null);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void changeFragmentShader(FiltersHandler.nameFilters filter) throws IOException {
        videoSurfaceRenderer.changeFilter(FiltersHandler.getFiltersByName(filter,getContext()));

    }
    public void changeFilter(BaseFilter filter)
    {
        videoSurfaceRenderer.changeFilter(filter);
    }

    public void updatePlayerController(PlayerController playerController)
    {
        this.playerController = playerController;
        videoSurfaceRenderer.setPlayerController(playerController);
    }
}
