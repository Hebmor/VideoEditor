package com.project.videoeditor.filters;

import android.content.Context;

import com.project.videoeditor.R;

public class BlackWhiteFilter extends BaseFilter {

    private final String filterName = "BlackWhiteFilter";
    @Override
    public String getFilterName() {
        return filterName;
    }

    public BlackWhiteFilter(Context context) {
        super(context);
        this.loadFragmentShaderFromResource(R.raw.black_and_white);
    }
}
