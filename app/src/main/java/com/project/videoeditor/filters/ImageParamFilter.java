package com.project.videoeditor.filters;

import android.content.Context;
import android.opengl.GLES20;

import com.project.videoeditor.R;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ImageParamFilter extends BaseFilter{

    private boolean isGetLocation = false;
    private int mBrightnessHandler;
    private int mContrastHandler;
    private float mBrightness = 0f;
    private float mContrast = 1f;
    private static final FiltersFactory.NameFilters name = FiltersFactory.NameFilters.IMAGE_PARAM;

    public ImageParamFilter(Context context) {
        super(context);
        this.loadFragmentShaderFromResource(R.raw.image_param);
    }

    public ImageParamFilter(Context context, float mBrightness, float mContrast) {
        super(context);
        this.loadFragmentShaderFromResource(R.raw.image_param);
        this.mBrightness = mBrightness;
        this.mContrast = mContrast;
    }

    @Override
    public FiltersFactory.NameFilters getFilterName() {
        return name;
    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
        this.getLocation();
        isGetLocation = true;
    }
    private void getLocation()
    {
        mBrightnessHandler = GLES20.glGetUniformLocation(mProgram, "brightness");
        checkGlError("glGetUniformLocation mBrightness");
        if (mBrightnessHandler == -1) {
            throw new RuntimeException("Could not get uniform location for mBrightness");
        }

        mContrastHandler = GLES20.glGetUniformLocation(mProgram, "contrast");
        checkGlError("glGetUniformLocation mContrast");
        if (mContrastHandler == -1) {
            throw new RuntimeException("Could not get uniform location for mContrast");
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
        GLES20.glUniform1f(mBrightnessHandler, mBrightness);
        GLES20.glUniform1f(mContrastHandler, mContrast);

        super.draw();
    }

    public float getBrightness() {
        return mBrightness;
    }

    public void setBrightness(float mBrightness) {
        this.mBrightness = mBrightness;
    }

    public float getContrast() {
        return mContrast;
    }

    public void setContrast(float mContrast) {
        this.mContrast = mContrast;
    }
}
