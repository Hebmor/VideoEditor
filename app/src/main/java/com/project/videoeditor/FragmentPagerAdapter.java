package com.project.videoeditor;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;

public class FragmentPagerAdapter  extends androidx.fragment.app.FragmentPagerAdapter {

    private Context mContext;
    private ArrayList<Fragment> fragmentArrayList;

    public FragmentPagerAdapter(@NonNull FragmentManager fm, int behavior,Context context) {
        super(fm, behavior);
        this.mContext = context;
        fragmentArrayList = new ArrayList<>();
    }
    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragmentArrayList.get(position);
    }
    public void addItem(Fragment fragment)
    {
        this.fragmentArrayList.add(fragment);
    }
    @Override
    public int getCount() {
        return fragmentArrayList.size();
    }
}
