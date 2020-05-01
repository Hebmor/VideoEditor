package com.project.videoeditor;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.FloatBuffer;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

public class FiltersHandler {

    public enum nameFilters
    {
        DEFAULT,
        BLACK_AND_WHITE
    }

    private final String NameFrameBuffer = "filteredBuffer.png";

    public static int getDefault()
    {
        return R.raw.default_state;
    }
    public static int getBlackAndWhite()
    {
        return R.raw.black_and_white;
    }
    public static BaseFilters getFiltersByName(nameFilters name,Context context)
    {
        switch (name)
        {
            case DEFAULT:
                return new DefaultFilter(context);
            case BLACK_AND_WHITE:
                return new BlackWhiteFilter(context);

            default:
                throw new IllegalArgumentException("Неизвестный фильтр!");
        }
    }

}
