package com.project.videoeditor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.videoeditor.filters.BaseFilter;
import com.project.videoeditor.filters.BlackWhiteFilter;
import com.project.videoeditor.filters.CelShadingFilter;
import com.project.videoeditor.filters.DefaultFilter;
import com.project.videoeditor.filters.FilterExecutor;
import com.project.videoeditor.filters.ImageKernelMatrix;
import com.project.videoeditor.filters.ImageKernelFilter;
import com.project.videoeditor.filters.ImageParamFilter;
import com.project.videoeditor.filters.PixellationFilter;

import java.io.IOException;
import java.util.ArrayList;

public class FilterListFragment extends Fragment {

    private RecyclerView recyclerVideoInfo;
    private LinearLayoutManager layoutManager;
    private FilterListAdapter filterListAdapter;
    private ArrayList<BaseFilter> filters;
    private VideoFilteredView videoFilteredView;
    private VideoInfo videoInfo;
    private Button saveFilterResult;
    private FilterExecutor filterExecutor;
    private int lastSelectFilterIdx = 0;
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int itemPosition = recyclerVideoInfo.getChildLayoutPosition(view);
            if(itemPosition < filters.size()) {
                lastSelectFilterIdx = itemPosition;
                videoFilteredView.changeFilter(filters.get(itemPosition));
            }
        }
    };

    public void setVideoInfo(VideoInfo videoInfo) {
        this.videoInfo = videoInfo;
    }
    public void setVideoFilteredView(VideoFilteredView videoFilteredView) {
        this.videoFilteredView = videoFilteredView;
    }

    public FilterListFragment() {
        filters = new ArrayList<>();
    }
    public FilterListFragment(VideoInfo info) {
        filters = new ArrayList<>();
        this.videoInfo = info;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.filter_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerVideoInfo = view.findViewById(R.id.recyclerView);
        saveFilterResult = view.findViewById(R.id.buttonSaveFilterResult);
        layoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        filterListAdapter = createAdapter();
        recyclerVideoInfo.setLayoutManager(layoutManager);
        recyclerVideoInfo.setAdapter(filterListAdapter);
        filterExecutor = new FilterExecutor(getActivity());
        try {
            filterExecutor.setupSettings(videoInfo.getBitrate() * 1024,videoInfo.getPath(),30,filters.get(lastSelectFilterIdx));
        } catch (IOException e) {
            e.printStackTrace();
        }
        saveFilterResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    BaseFilter filterClone = (BaseFilter) filters.get(lastSelectFilterIdx).clone();
                    filterClone.setPlayerMod(false);
                    filterExecutor.setFilter(filterClone);
                    filterExecutor.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private FilterListAdapter createAdapter()
    {
        FilterListAdapter filterListAdapter = new FilterListAdapter(ResourcesCompat.getDrawable(getResources(),
                R.drawable.default_filter,null),"По умолчанию");
        filters.add(new DefaultFilter());
        filterListAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.black_filter,null),"Черно-белый");
        filters.add(new BlackWhiteFilter(getContext()));
        filterListAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.black_filter,null),"GAUSS");
        filters.add(new ImageKernelFilter(getContext(),200,200, ImageKernelMatrix.gaussianBlur_kernel));
        filterListAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.black_filter,null),"EDGE detection");
        filters.add(new ImageKernelFilter(getContext(),200,200, ImageKernelMatrix.edge_kernel));
        filterListAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.black_filter,null),"EDGE Enhance");
        filters.add(new ImageKernelFilter(getContext(),200,200, ImageKernelMatrix.edgeEnhance_kernel));
        filterListAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.black_filter,null),"sharp_kernel");
        filters.add(new ImageKernelFilter(getContext(),200,200, ImageKernelMatrix.sharp_kernel));
        filterListAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.black_filter,null),"C and B");
        filters.add(new ImageParamFilter(getContext(),0.2f,-2f));
        filterListAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.black_filter,null),"Pixellation");
        filters.add(new PixellationFilter(getContext(),videoInfo.getHeight(),videoInfo.getWidth(),20f));
        filterListAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.black_filter,null),"CellShading");
        filters.add(new CelShadingFilter(getContext(),4f));

        filterListAdapter.setOnClickListener(onClickListener);
        return  filterListAdapter;

    }
}
