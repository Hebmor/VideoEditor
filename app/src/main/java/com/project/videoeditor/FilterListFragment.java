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

public class FilterListFragment extends Fragment {

    private RecyclerView recyclerVideoInfo;
    private LinearLayoutManager layoutManager;
    private FilterListAdapter filterListAdapter;

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
        return  filterListAdapter;
    }
}
