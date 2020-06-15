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

    public static BaseFilter getFiltersByName(NameFilters name, Context context)
    {
        switch (name)
        {
            case DEFAULT:
                return new DefaultFilter(context);
            case BLACK_AND_WHITE:
                return new BlackWhiteFilter(context);
            case CEL_SHADING:
                return new CelShadingFilter(context);
            case IMAGE_PARAM:
                return new ImageParamFilter(context);
            case PIXELATION:
                return new PixelationFilter(context);
            case IMAGE_KERNEL:
                return new ImageKernelFilter(context);
            default:
                throw new IllegalArgumentException("Неизвестный фильтр!");
        }
    }
}
