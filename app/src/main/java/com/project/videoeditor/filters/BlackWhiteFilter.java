package com.project.videoeditor.filters;

import android.content.Context;

import com.project.videoeditor.R;

public class BlackWhiteFilter extends BaseFilter {

    private static final FiltersFactory.NameFilters name = FiltersFactory.NameFilters.BLACK_AND_WHITE;

    @Override
    public FiltersFactory.NameFilters getFilterName() {
        return name;
    }

    public BlackWhiteFilter(Context context) {
        super(context);
        this.loadFragmentShaderFromResource(R.raw.black_and_white);
    }
}
