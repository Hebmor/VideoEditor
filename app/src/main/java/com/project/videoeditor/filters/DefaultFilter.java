package com.project.videoeditor.filters;

import android.content.Context;
import android.media.MediaPlayer;

public class DefaultFilter extends BaseFilters {

    private final String filterName = "DefaultFilter";
    @Override
    public String getFilterName() {
        return filterName;
    }
    public DefaultFilter() { }
    public DefaultFilter(Context context) {
        super(context);
    }
    public DefaultFilter(Context context, MediaPlayer mediaPlayer) {
        super(context, mediaPlayer);
    }

}
