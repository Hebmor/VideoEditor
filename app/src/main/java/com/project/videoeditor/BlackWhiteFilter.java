package com.project.videoeditor;

import android.content.Context;

public class BlackWhiteFilter extends BaseFilters {

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
