package com.project.videoeditor;

import android.content.Context;
import android.media.MediaPlayer;

public class DefaultFilter extends BaseFilters {

    public DefaultFilter() { }
    public DefaultFilter(Context context) {
        super(context);
    }
    public DefaultFilter(Context context, MediaPlayer mediaPlayer) {
        super(context, mediaPlayer);
    }

}
