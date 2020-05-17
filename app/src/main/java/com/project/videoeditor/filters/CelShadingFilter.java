package com.project.videoeditor.filters;

import android.content.Context;
import android.opengl.GLES20;

import com.project.videoeditor.R;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CelShadingFilter extends BaseFilter {
    private boolean isGetLocation = false;
    private int colorCountHandler;
    private float colorsCount = 1f;

    public CelShadingFilter(Context context) {
        super(context);
        this.loadFragmentShaderFromResource(R.raw.cel_shading);
    }

    public CelShadingFilter(Context context, float colorsCount) {
        super(context);
        this.loadFragmentShaderFromResource(R.raw.cel_shading);
        this.colorsCount = colorsCount;
    }

    @Override
    public String getFilterName() {
        return "CelShadingFilter";
    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
        this.getLocation();
        isGetLocation = true;
    }
    private void getLocation()
    {
        colorCountHandler = GLES20.glGetUniformLocation(mProgram, "nColors");
        checkGlError("glGetUniformLocation nColors");
        if (colorCountHandler == -1) {
            throw new RuntimeException("Could not get uniform location for nColors");
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
        GLES20.glUniform1f(colorCountHandler, colorsCount);

        super.draw();
    }
}
