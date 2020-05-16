package com.project.videoeditor.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.media.MediaExtractor;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.VideoView;

import com.google.android.exoplayer2.util.Util;
import com.google.android.material.tabs.TabLayout;
import com.project.videoeditor.FilterListFragment;
import com.project.videoeditor.database.ExpansionEntity;
import com.project.videoeditor.database.ExpansionEntityViewModel;
import com.project.videoeditor.FragmentPagerAdapter;
import com.project.videoeditor.PlayerController;
import com.project.videoeditor.R;
import com.project.videoeditor.VideoFilteredView;
import com.project.videoeditor.VideoInfo;
import com.project.videoeditor.VideoInfoFragment;
import com.project.videoeditor.VideoTimeline;
import com.project.videoeditor.filters.BlackWhiteFilter;
import com.project.videoeditor.filters.FilterExecutor;
import com.project.videoeditor.filters.FiltersVideoActivity;

import java.io.IOException;
import java.util.List;

public class MainEditor extends AppCompatActivity {

    public static final String EDIT_VIDEO_ID = "6001";
    private ExpansionEntityViewModel mExpansionViewModel;
    private VideoInfo editVideoInfo;
    private VideoTimeline videoTimeline;
    private FragmentManager fragmentManager;
    private FrameLayout videoContainer;
    private VideoFilteredView videoFilteredView;
    private FilterExecutor filterExecutor;
    private MediaExtractor mediaExtractor;
    private VideoView videoView;
    private PlayerController playerController;
    private ViewPager pager;
    private VideoInfoFragment videoInfoFragment;
    private FilterListFragment filterListFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_editor);
        mExpansionViewModel = new ViewModelProvider(this).get(ExpansionEntityViewModel.class);
        mExpansionViewModel.getAllWords().observe(this, new Observer<List<ExpansionEntity>>() {
            @Override
            public void onChanged(@Nullable final List<ExpansionEntity> words) {
                // Update the cached copy of the words in the adapter.
               // adapter.setWords(words);
            }
        });
        videoContainer = findViewById(R.id.videoContainer);
        fragmentManager = getSupportFragmentManager();
        editVideoInfo = (VideoInfo) getIntent().getParcelableExtra(EDIT_VIDEO_ID);

        playerController = new PlayerController(this,editVideoInfo.getPath());
        videoTimeline = new VideoTimeline(editVideoInfo,playerController);
        videoInfoFragment = new VideoInfoFragment(editVideoInfo);
        filterListFragment = new FilterListFragment();
        int framerate = (int)Float.parseFloat(editVideoInfo.getFrameRate());
        pager = (ViewPager)findViewById(R.id.viewPager_editor);
        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager(), androidx.fragment.app.FragmentPagerAdapter.POSITION_NONE,this);
        fragmentPagerAdapter.addItem(videoTimeline);
        fragmentPagerAdapter.addItem(new VideoTimeline(editVideoInfo,playerController,true));
        fragmentPagerAdapter.addItem(filterListFragment);
        fragmentPagerAdapter.addItem(videoInfoFragment);


        pager.setAdapter(fragmentPagerAdapter);
        videoView = new VideoView(this);

        mediaExtractor = new MediaExtractor();
        filterExecutor = new FilterExecutor(this);

        TabLayout tabs = (TabLayout) findViewById(R.id.tabsEditor);
        tabs.setupWithViewPager(pager);
        initTabs(tabs);

        try {

            videoView.setVideoPath(editVideoInfo.getPath());
            mediaExtractor.setDataSource(editVideoInfo.getPath());
            filterExecutor.setupSettings(mediaExtractor,editVideoInfo.getBitrate() * 1024,editVideoInfo.getPath(),framerate,new BlackWhiteFilter(this));
            videoFilteredView = new VideoFilteredView(this,playerController);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //videoView.start();
        videoContainer.addView(videoFilteredView);
        videoContainer.addView(playerController.getPlayerControlView());

        //videoFilteredView.changeFragmentShader(UtilUri.OpenRawResourcesAsString(this,R.raw.black_and_white));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24) {
            playerController.releasePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        playerController.hideSystemUi();
        if ((Util.SDK_INT < 24 || playerController.getPlayer() == null)) {
            playerController.initializePlayer();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24) {
            playerController.initializePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            playerController.releasePlayer();
        }
    }

    public void ClickOpenEncodersPage(View view) {
        Intent intent = new Intent(this, VideoEncoders.class);
        intent.putExtra(VideoInfo.class.getCanonicalName(),editVideoInfo);
        startActivity(intent);
    }
    public void ClickOpenVideoInfoPage(View view)
    {
        Intent intent = new Intent(this, VideoInfoFragment.class);
        intent.putExtra(VideoInfo.class.getCanonicalName(),editVideoInfo);
        startActivity(intent);
    }
    public void ClickOpenEditorPage(View view)
    {
        Intent intent = new Intent(this, VideoEditPage.class);
        intent.putExtra(VideoInfo.class.getCanonicalName(),editVideoInfo);
        startActivity(intent);
    }
    public void ClickOpenFilterPage(View view)
    {
        Intent intent = new Intent(this, FiltersVideoActivity.class);
        intent.putExtra(FiltersVideoActivity.EDIT_VIDEO_ID,editVideoInfo);
        startActivity(intent);
    }
    public void ClickCropVideoPage(View view)
    {
        Intent intent = new Intent(this, CropVideoActivity.class);
        String path = editVideoInfo.getPath();
        intent.putExtra(CropVideoActivity.FRAME_BITMAP_URI,path);
        startActivity(intent);
    }
    private void initTabs(TabLayout tabs)
    {
        tabs.getTabAt(0).setText("Выделить");
        tabs.getTabAt(0).setIcon(R.drawable.ic_video_crop_24dp);
        tabs.getTabAt(1).setText("Выбрать");
        tabs.getTabAt(1).setIcon(R.drawable.ic_cut_24dp);
        tabs.getTabAt(2).setText("Фильтры");
        tabs.getTabAt(2).setIcon(R.drawable.ic_filter_24dp);
        tabs.getTabAt(3).setText("Информация");
        tabs.getTabAt(3).setIcon(R.drawable.ic_info_24dp);
    }
}
