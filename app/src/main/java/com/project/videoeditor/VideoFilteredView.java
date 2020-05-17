package com.project.videoeditor;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import com.project.videoeditor.filters.BaseFilter;
import com.project.videoeditor.filters.DefaultFilter;
import com.project.videoeditor.filters.FiltersHandler;

import java.io.IOException;


public class VideoFilteredView extends GLSurfaceView {

    private VideoSurfaceRenderer videoSurfaceRenderer;
    private PlayerController playerController;
    private Handler handler = new Handler();

    public VideoFilteredView(Context context, PlayerController playerController) throws Exception {
        super(context);
        setEGLContextClientVersion(2);
        this.playerController  = playerController;
        videoSurfaceRenderer = new VideoSurfaceRenderer(context,playerController,new DefaultFilter());
        videoSurfaceRenderer.setPlayerController(playerController);

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
        videoSurfaceRenderer.onSurfaceCreated(null,null);
    }

    public void changeFragmentShader(FiltersHandler.nameFilters filter) throws IOException {
        videoSurfaceRenderer.changeFilter(FiltersHandler.getFiltersByName(filter,getContext()));

    }
    public void changeFilter(BaseFilter filter)
    {
        videoSurfaceRenderer.changeFilter(filter);
    }
}
