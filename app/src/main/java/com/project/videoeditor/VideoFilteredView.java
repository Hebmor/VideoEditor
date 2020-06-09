package com.project.videoeditor;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import com.project.videoeditor.filters.BaseFilter;
import com.project.videoeditor.filters.DefaultFilter;
import com.project.videoeditor.filters.FiltersFactory;

import java.io.IOException;


public class VideoFilteredView extends GLSurfaceView {

    private VideoSurfaceRenderer videoSurfaceRenderer;
    private static boolean  saveFlag = false;

    public VideoFilteredView(Context context, PlayerController playerController) throws Exception {
        super(context);
        this.setEGLContextClientVersion(2);
        this.setDebugFlags(DEBUG_CHECK_GL_ERROR);
        this. setPreserveEGLContextOnPause(true);
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

    public void changeFragmentShader(FiltersFactory.NameFilters filter) throws IOException {
        videoSurfaceRenderer.changeFilter(FiltersFactory.getFiltersByName(filter,getContext()));

    }
    public void changeFilter(BaseFilter filter)
    {
        videoSurfaceRenderer.changeFilter(filter);
    }

    public void updatePlayerController(PlayerController playerController)
    {
        videoSurfaceRenderer.setPlayerController(playerController);
    }

    public BaseFilter getCurrentFilter()
    {
        return videoSurfaceRenderer.getCurrentFilter();
    }
}
