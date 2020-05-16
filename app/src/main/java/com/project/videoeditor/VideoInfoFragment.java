package com.project.videoeditor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class VideoInfoFragment extends Fragment {

    private RecyclerView recyclerVideoInfo;
    private LinearLayoutManager layoutManager;
    private VideoInfoAdapter videoInfoAdapter;
    private VideoInfo videoInfo;


    public VideoInfoFragment(VideoInfo videoInfo) {
        this.videoInfo = videoInfo;
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
        layoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        videoInfoAdapter = createAdapterFromVideoInfo(videoInfo);
        recyclerVideoInfo.setLayoutManager(layoutManager);
        recyclerVideoInfo.setAdapter(videoInfoAdapter);
    }
    private VideoInfoAdapter createAdapterFromVideoInfo(VideoInfo videoInfo)
    {
        VideoInfoAdapter videoInfoAdapter = new VideoInfoAdapter(ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_filename_20dp, null), "Название файла", videoInfo.getFilename());
        videoInfoAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_file_20dp, null), "Расширение", videoInfo.getExtension());
        videoInfoAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_encode_20dp, null), "Кодек", videoInfo.getCodec());
        videoInfoAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_resolution_20dp, null), "Разрешение", videoInfo.getWidth()+ "x" + videoInfo.getHeight());
        videoInfoAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_aspect_ratio_20dp, null), "Соотношение сторон", videoInfo.getAspectRatio());
        videoInfoAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_path_20dp, null), "Путь к файлу", videoInfo.getPath());
        videoInfoAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_framerate_20dp, null), "Кадры в секунду", videoInfo.getFrameRate() + " к/с");
        videoInfoAdapter.addItem(ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_bitrate_20dp, null), "Битрейт", videoInfo.getBitrate() + " кбит/с");

        return videoInfoAdapter;
    }
}
