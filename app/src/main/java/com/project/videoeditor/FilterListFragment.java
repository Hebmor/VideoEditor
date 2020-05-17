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

import com.project.videoeditor.filters.BaseFilter;
import com.project.videoeditor.filters.BlackWhiteFilter;
import com.project.videoeditor.filters.DefaultFilter;
import com.project.videoeditor.filters.ImageKernelMatrix;
import com.project.videoeditor.filters.ImageKernelFilter;
import com.project.videoeditor.filters.ImageParamFilter;

import java.util.ArrayList;

public class FilterListFragment extends Fragment {

    private RecyclerView recyclerVideoInfo;
    private LinearLayoutManager layoutManager;
    private FilterListAdapter filterListAdapter;
    private ArrayList<BaseFilter> baseFilterArrayList;
    private VideoFilteredView videoFilteredView;
    private VideoInfo videoInfo;
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int itemPosition = recyclerVideoInfo.getChildLayoutPosition(view);
            if(itemPosition < baseFilterArrayList.size())
                videoFilteredView.changeFilter(baseFilterArrayList.get(itemPosition));
        }
    };

    public void setVideoInfo(VideoInfo videoInfo) {
        this.videoInfo = videoInfo;
    }
    public void setVideoFilteredView(VideoFilteredView videoFilteredView) {
        this.videoFilteredView = videoFilteredView;
    }

    public FilterListFragment() {
        baseFilterArrayList = new ArrayList<>();
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
        baseFilterArrayList.add(new DefaultFilter());
        filterListAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.black_filter,null),"Черно-белый");
        baseFilterArrayList.add(new BlackWhiteFilter(getContext()));
        filterListAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.black_filter,null),"Гауссовское размытие");
        baseFilterArrayList.add(new ImageKernelFilter(getContext(),200,200, ImageKernelMatrix.gaussianBlur_kernel));
        filterListAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.black_filter,null),"EDGE detection");
        baseFilterArrayList.add(new ImageKernelFilter(getContext(),200,200, ImageKernelMatrix.edge_kernel));
        filterListAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.black_filter,null),"EDGE Enhance");
        baseFilterArrayList.add(new ImageKernelFilter(getContext(),200,200, ImageKernelMatrix.edgeEnhance_kernel));
        filterListAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.black_filter,null),"sharp_kernel");
        baseFilterArrayList.add(new ImageKernelFilter(getContext(),200,200, ImageKernelMatrix.sharp_kernel));
        filterListAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.black_filter,null),"C and B");
        baseFilterArrayList.add(new ImageParamFilter(getContext(),0.2f,-2f));
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
