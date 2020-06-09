package com.project.videoeditor.filters;

import android.content.Context;

import com.project.videoeditor.PlayerController;

public class DefaultFilter extends BaseFilter {

    private static final FiltersFactory.NameFilters name = FiltersFactory.NameFilters.DEFAULT;

    @Override
    public FiltersFactory.NameFilters getFilterName() {
        return name;
    }

    public DefaultFilter() { }

    public DefaultFilter(Context context) {
        super(context);
    }

    public DefaultFilter(Context context, PlayerController playerController) {
        super(context, playerController);
    }

}
