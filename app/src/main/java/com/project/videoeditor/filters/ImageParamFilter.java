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
public class ImageParamFilter extends BaseFilter{

    private boolean isGetLocation = false;
    private int mBrightnessHandler;
    private int mContrastHandler;
    public static final float DEFAULT_BRIGHTNESS = 0f;
    public static final float DEFAULT_CONTRAST = 1f;

    private float mContrast = DEFAULT_CONTRAST;
    private float mBrightness = DEFAULT_BRIGHTNESS;
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

    public ImageParamFilter(Parcel parcel)
    {
        super();
        this.mBrightness = parcel.readFloat();
        this.mContrast = parcel.readFloat();
        this.FRAGMENT_SHADER = parcel.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(mBrightness);
        dest.writeFloat(mContrast);
        dest.writeString(this.FRAGMENT_SHADER);
    }

    public static final Parcelable.Creator<ImageParamFilter> CREATOR = new Parcelable.Creator<ImageParamFilter>() {

        @Override
        public ImageParamFilter createFromParcel(Parcel source) {
            return new ImageParamFilter(source);
        }

        @Override
        public ImageParamFilter[] newArray(int size) {
            return new ImageParamFilter[size];
        }
    };
}
