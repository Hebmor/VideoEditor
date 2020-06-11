package com.project.videoeditor.filters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.project.videoeditor.R;

@SuppressLint("ParcelCreator")
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

    public BlackWhiteFilter(Parcel parcel)
    {
        super();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public static final Parcelable.Creator<BlackWhiteFilter> CREATOR = new Parcelable.Creator<BlackWhiteFilter>() {

        @Override
        public BlackWhiteFilter createFromParcel(Parcel source) {
            return new BlackWhiteFilter(source);
        }

        @Override
        public BlackWhiteFilter[] newArray(int size) {
            return new BlackWhiteFilter[size];
        }
    };
}
