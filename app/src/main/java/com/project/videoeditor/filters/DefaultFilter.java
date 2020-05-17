package com.project.videoeditor.filters;

import android.content.Context;

import com.project.videoeditor.PlayerController;

public class DefaultFilter extends BaseFilter {

    private final String filterName = "DefaultFilter";
    @Override
    public String getFilterName() {
        return filterName;
    }
    public DefaultFilter() { }
    public DefaultFilter(Context context) {
        super(context);
    }
    public DefaultFilter(Context context, PlayerController playerController) {
        super(context, playerController);
    }

}
