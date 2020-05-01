package com.project.videoeditor;

import android.content.Context;

public class BlackWhiteFilter extends BaseFilters {

    public BlackWhiteFilter(Context context) {
        super(context);
        this.loadFragmentShaderFromResource(R.raw.black_and_white);
    }
}
