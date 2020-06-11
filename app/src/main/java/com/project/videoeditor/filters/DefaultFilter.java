package com.project.videoeditor.filters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.project.videoeditor.PlayerController;

@SuppressLint("ParcelCreator")
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

    public DefaultFilter(Parcel parcel)
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

    public static final Parcelable.Creator<DefaultFilter> CREATOR = new Parcelable.Creator<DefaultFilter>() {

        @Override
        public DefaultFilter createFromParcel(Parcel source) {
            return new DefaultFilter(source);
        }

        @Override
        public DefaultFilter[] newArray(int size) {
            return new DefaultFilter[size];
        }
    };
}
