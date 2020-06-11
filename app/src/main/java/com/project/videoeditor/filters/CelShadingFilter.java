package com.project.videoeditor.filters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLES20;
import android.os.Parcel;
import android.os.Parcelable;

import com.project.videoeditor.R;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

@SuppressLint("ParcelCreator")
public class CelShadingFilter extends BaseFilter {

    private boolean isGetLocation = false;
    private int colorCountHandler;

    private float colorsCount = 1f;
    private static final FiltersFactory.NameFilters name = FiltersFactory.NameFilters.CEL_SHADING;

    public CelShadingFilter(Context context) {
        super(context);
        this.loadFragmentShaderFromResource(R.raw.cel_shading);
    }

    public CelShadingFilter(Context context, float colorsCount) {
        super(context);
        this.loadFragmentShaderFromResource(R.raw.cel_shading);
        this.colorsCount = colorsCount;
    }

    public CelShadingFilter(Parcel parcel)
    {
        super();
        colorsCount = parcel.readFloat();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(colorsCount);
    }

    public static final Parcelable.Creator<CelShadingFilter> CREATOR = new Parcelable.Creator<CelShadingFilter>() {

        @Override
        public CelShadingFilter createFromParcel(Parcel source) {
            return new CelShadingFilter(source);
        }

        @Override
        public CelShadingFilter[] newArray(int size) {
            return new CelShadingFilter[size];
        }
    };

    public float getColorsCount() {
        return colorsCount;
    }
}
