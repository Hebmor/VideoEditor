package com.project.videoeditor.filters;

import android.content.Context;
import android.opengl.GLES20;
import android.os.Parcel;
import android.os.Parcelable;

import com.project.videoeditor.R;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ImageKernelFilter extends BaseFilter {

    private int mImageResolutionHandle;
    private int mKernelHandle;

    private float [] resolutionVideo = new float[2];
    private boolean isGetLocation = false;
    private float [] currentKernelMatrix = ImageKernelMatrix.edge_kernel;
    private static final FiltersFactory.NameFilters name = FiltersFactory.NameFilters.IMAGE_KERNEL;


    public float[] getResolutionVideo() {
        return resolutionVideo;
    }

    public float[] getCurrentKernelMatrix() {
        return currentKernelMatrix;
    }

    public void setCurrentKernelMatrix(float[] currentKernelMatrix) {
        this.currentKernelMatrix = currentKernelMatrix;
    }

    @Override
    public FiltersFactory.NameFilters getFilterName() {
        return name;
    }

    public ImageKernelFilter(Context context) {
        super(context);
        this.loadFragmentShaderFromResource(R.raw.image_kernel);
    }

    public ImageKernelFilter(Parcel parcel)
    {
        super();
        parcel.readFloatArray( this.resolutionVideo);
        parcel.readFloatArray( this.currentKernelMatrix);
        this.FRAGMENT_SHADER = parcel.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloatArray(resolutionVideo);
        dest.writeFloatArray(currentKernelMatrix);
        dest.writeString(this.FRAGMENT_SHADER);
    }

    public static final Parcelable.Creator<ImageKernelFilter> CREATOR = new Parcelable.Creator<ImageKernelFilter>() {

        @Override
        public ImageKernelFilter createFromParcel(Parcel source) {
            return new ImageKernelFilter(source);
        }

        @Override
        public ImageKernelFilter[] newArray(int size) {
            return new ImageKernelFilter[size];
        }
    };
}
