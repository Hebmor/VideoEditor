package com.project.videoeditor.filters;

import android.content.Context;
import android.opengl.GLES20;

import com.project.videoeditor.R;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PixellationFilter extends BaseFilter {

    private boolean isGetLocation = false;
    private int resolutionVideoHandle;
    private int pixelSizeHandle;
    private float[] resolutionVideo = new float[2];
    private float pixelSize = 0;

    @Override
    public String getFilterName() {
        return "PixellationFilter";
    }

    public PixellationFilter(Context context) {
        super(context);
        this.loadFragmentShaderFromResource(R.raw.pixellation);
    }

    public PixellationFilter(Context context,float heightVideo,float widthVideo) {
        super(context);
        this.loadFragmentShaderFromResource(R.raw.pixellation);
        this.setResolutionVideo(heightVideo,widthVideo);
    }

    public PixellationFilter(Context context,float heightVideo,float widthVideo,float pixelSize) {
        super(context);
        this.loadFragmentShaderFromResource(R.raw.pixellation);
        this.setResolutionVideo(heightVideo,widthVideo);
        this.pixelSize = pixelSize;

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
        this.getLocation();
        isGetLocation = true;
    }
    private void setResolutionVideo(float height,float width)
    {
        this.resolutionVideo[0] = width;
        this.resolutionVideo[1] = height;
    }
    private void getLocation()
    {
        resolutionVideoHandle = GLES20.glGetUniformLocation(mProgram, "imageResolution");
        checkGlError("glGetUniformLocation imageResolution");
        if (resolutionVideoHandle == -1) {
            throw new RuntimeException("Could not get uniform location for imageResolution");
        }

        pixelSizeHandle = GLES20.glGetUniformLocation(mProgram, "size");
        checkGlError("glGetUniformLocation size");
        if (pixelSizeHandle == -1) {
            throw new RuntimeException("Could not get uniform location for size");
        }
    }
    @Override
    public void onDrawFrame(GL10 gl) {
        if(!isGetLocation) {
            this.getLocation();
            isGetLocation = true;
        }
        super.synchronizeDrawFrame();
        super.preDraw();
        super.bindResource();
        GLES20.glUniform2fv(resolutionVideoHandle, 1, resolutionVideo, 0);
        GLES20.glUniform1f(pixelSizeHandle, pixelSize);
        super.draw();
    }
}
