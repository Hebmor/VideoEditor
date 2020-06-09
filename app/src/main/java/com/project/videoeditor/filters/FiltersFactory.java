package com.project.videoeditor.filters;

import android.content.Context;

import com.project.videoeditor.R;

public class FiltersFactory {

    public enum NameFilters
    {
        DEFAULT,
        BLACK_AND_WHITE,
        CEL_SHADING,
        IMAGE_KERNEL,
        IMAGE_PARAM,
        PIXELATION

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
    public static BaseFilter getFiltersByName(NameFilters name, Context context)
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
