package com.project.videoeditor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.videoeditor.filters.BaseFilters;
import com.project.videoeditor.filters.BlackWhiteFilter;
import com.project.videoeditor.filters.DefaultFilter;
import com.project.videoeditor.filters.ImageKernelMatrix;
import com.project.videoeditor.filters.ImageKernelFilter;

import java.util.ArrayList;

public class FilterListFragment extends Fragment {

    private RecyclerView recyclerVideoInfo;
    private LinearLayoutManager layoutManager;
    private FilterListAdapter filterListAdapter;
    private ArrayList<BaseFilters> baseFiltersArrayList;
    private VideoFilteredView videoFilteredView;
    private VideoInfo videoInfo;
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int itemPosition = recyclerVideoInfo.getChildLayoutPosition(view);
            if(itemPosition < baseFiltersArrayList.size())
                videoFilteredView.changeFilter(baseFiltersArrayList.get(itemPosition));
        }
    };

    public void setVideoInfo(VideoInfo videoInfo) {
        this.videoInfo = videoInfo;
    }
    public void setVideoFilteredView(VideoFilteredView videoFilteredView) {
        this.videoFilteredView = videoFilteredView;
    }

    public FilterListFragment() {
        baseFiltersArrayList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recycler, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerVideoInfo = view.findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        filterListAdapter = createAdapter();
        recyclerVideoInfo.setLayoutManager(layoutManager);
        recyclerVideoInfo.setAdapter(filterListAdapter);
    }
    private FilterListAdapter createAdapter()
    {
        FilterListAdapter filterListAdapter = new FilterListAdapter(ResourcesCompat.getDrawable(getResources(),
                R.drawable.default_filter,null),"По умолчанию");
        baseFiltersArrayList.add(new DefaultFilter());
        filterListAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.black_filter,null),"Черно-белый");
        baseFiltersArrayList.add(new BlackWhiteFilter(getContext()));
        filterListAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.black_filter,null),"Гауссовское размытие");
        baseFiltersArrayList.add(new ImageKernelFilter(getContext(),200,200, ImageKernelMatrix.gaussianBlur_kernel));
        filterListAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.black_filter,null),"EDGE detection");
        baseFiltersArrayList.add(new ImageKernelFilter(getContext(),200,200, ImageKernelMatrix.edge_kernel));
        filterListAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.black_filter,null),"EDGE Enhance");
        baseFiltersArrayList.add(new ImageKernelFilter(getContext(),200,200, ImageKernelMatrix.edgeEnhance_kernel));
        filterListAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.black_filter,null),"sharp_kernel");
        baseFiltersArrayList.add(new ImageKernelFilter(getContext(),200,200, ImageKernelMatrix.sharp_kernel));
        filterListAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.black_filter,null),"Черно-белый");
        filterListAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.black_filter,null),"Черно-белый");
        filterListAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.black_filter,null),"Черно-белый");
        filterListAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.black_filter,null),"Черно-белый");
        filterListAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.black_filter,null),"Черно-белый");
        filterListAdapter.setOnClickListener(onClickListener);
        return  filterListAdapter;
    }
}
