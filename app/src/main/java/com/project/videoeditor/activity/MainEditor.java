package com.project.videoeditor.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.media.MediaExtractor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.exoplayer2.util.Util;
import com.google.android.material.tabs.TabLayout;
import com.project.videoeditor.FilterListAdapter;
import com.project.videoeditor.FilterListFragment;
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

public class MainEditor extends AppCompatActivity {
    public static final String EDIT_VIDEO_ID = "EditVideoInfo";
    private VideoInfo editVideoInfo;
    private VideoTimeline videoTimelineSplit;
    private VideoTimeline videoTimelineCut;
    private FrameLayout videoContainer;
    private VideoFilteredView videoFilteredView;
    private FilterExecutor filterExecutor;
    private MediaExtractor mediaExtractor;
    private PlayerController playerController;
    private ViewPager viewPager;
    private VideoInfoFragment videoInfoFragment;
    private FilterListFragment filterListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_editor);
        videoContainer = findViewById(R.id.videoContainer);
        viewPager = (ViewPager)findViewById(R.id.viewPager_editor);
        TabLayout tabs = (TabLayout) findViewById(R.id.tabsEditor);
        editVideoInfo = (VideoInfo) getIntent().getParcelableExtra(EDIT_VIDEO_ID);

        playerController = new PlayerController(this,editVideoInfo.getPath());
        videoTimelineSplit = new VideoTimeline(editVideoInfo,playerController);
        videoTimelineCut = new VideoTimeline(editVideoInfo,playerController,true);
        videoInfoFragment = new VideoInfoFragment(editVideoInfo);
        filterListFragment = new FilterListFragment(editVideoInfo);
        filterListFragment.setCurrentTimeline(videoTimelineCut);
        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager(),
                androidx.fragment.app.FragmentPagerAdapter.POSITION_NONE,this);

        fragmentPagerAdapter.addItem(videoTimelineCut);
        fragmentPagerAdapter.addItem(videoTimelineSplit);
        fragmentPagerAdapter.addItem(filterListFragment);
        fragmentPagerAdapter.addItem(videoInfoFragment);

        viewPager.setAdapter(fragmentPagerAdapter);

        mediaExtractor = new MediaExtractor();
        filterExecutor = new FilterExecutor(this);
        tabs.setupWithViewPager(viewPager);

        initTabs(tabs);

        try {
            mediaExtractor.setDataSource(editVideoInfo.getPath());
            filterExecutor.setupSettings(editVideoInfo.getBitrate() * 1024,editVideoInfo.getPath(),(int)Float.parseFloat(editVideoInfo.getFrameRate()),new BlackWhiteFilter(this));
            videoFilteredView = new VideoFilteredView(this,playerController);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        filterListFragment.setVideoFilteredView(videoFilteredView);
        videoContainer.addView(videoFilteredView);
        videoContainer.addView(playerController.getPlayerControlView());
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

    public void ClickOpenSavePage(View view) {
        if(view instanceof Button) {

            Intent intent = new Intent(this, SaveVideoActivity.class);
            intent.putExtra(VideoInfo.class.getCanonicalName(), editVideoInfo);
            if(view.getId() == R.id.buttonEncodeSplit) {
                intent.putExtra("beginValue", 0);
                intent.putExtra("endValue", videoTimelineSplit.getLeftValue());
            }
            else if(view.getId() == R.id.buttonEncodeCut) {
                intent.putExtra("beginValue", videoTimelineCut.getLeftValue());
                intent.putExtra("endValue",  videoTimelineCut.getRightValue());
            }
            startActivity(intent);
        }
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
