package com.project.videoeditor.filters;

import android.content.Context;
import android.opengl.GLES20;

import com.project.videoeditor.R;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ImageKernelFilter extends BaseFilter {

    private int mImageResolutionHandle;
    private int mKernelHandle;
    private float [] resolutionVideo = new float[2];
    private float [] kernelMatrix = new float[9];
    private boolean isGetLocation = false;
    private float [] currentKernelMatrix = ImageKernelMatrix.edge_kernel;

    public void setCurrentKernelMatrix(float[] currentKernelMatrix) {
        this.currentKernelMatrix = currentKernelMatrix;
    }

    @Override
    public String getFilterName() {
        return "ImageKernel";
    }

    public ImageKernelFilter(Context context) {
        super(context);
        this.loadFragmentShaderFromResource(R.raw.image_kernel);
    }

    public ImageKernelFilter(Context context,float heightVideo,float widthVideo) {
        super(context);
        this.loadFragmentShaderFromResource(R.raw.image_kernel);
        this.setResolutionVideo(heightVideo,widthVideo);
    }

    public ImageKernelFilter(Context context,float heightVideo,float widthVideo,float [] kernelMatrix) {
        super(context);
        this.loadFragmentShaderFromResource(R.raw.image_kernel);
        this.setResolutionVideo(heightVideo,widthVideo);
        this.setCurrentKernelMatrix(kernelMatrix);
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
        mImageResolutionHandle = GLES20.glGetUniformLocation(mProgram, "imageResolution");
        checkGlError("glGetUniformLocation imageResolution");
        if (mImageResolutionHandle == -1) {
            throw new RuntimeException("Could not get uniform location for imageResolution");
        }

        mKernelHandle = GLES20.glGetUniformLocation(mProgram, "kernel");
        checkGlError("glGetUniformLocation kernel");
        if (mKernelHandle == -1) {
            throw new RuntimeException("Could not get uniform location for kernel");
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
        GLES20.glUniform2fv(mImageResolutionHandle, 1, resolutionVideo, 0);
        GLES20.glUniformMatrix3fv(mKernelHandle, 1,false, currentKernelMatrix,0);
        super.draw();
    }

}
