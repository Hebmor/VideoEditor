package com.project.videoeditor.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.media.MediaExtractor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import androidx.appcompat.widget.Toolbar;

import com.google.android.exoplayer2.util.Util;
import com.google.android.material.tabs.TabLayout;
import com.project.videoeditor.FilterListFragment;
import com.project.videoeditor.FragmentPagerAdapter;
import com.project.videoeditor.PlayerController;
import com.project.videoeditor.R;
import com.project.videoeditor.VideoFilteredView;
import com.project.videoeditor.VideoInfo;
import com.project.videoeditor.VideoInfoFragment;
import com.project.videoeditor.VideoTimelineController;
import com.project.videoeditor.codecs.ActionEditor;
import com.project.videoeditor.filters.BaseFilter;
import com.project.videoeditor.filters.BlackWhiteFilter;
import com.project.videoeditor.filters.FilterExecutor;
import com.project.videoeditor.support.SupportUtil;

import java.io.IOException;

public class MainEditor extends AppCompatActivity {
    public static final String EDIT_VIDEO_ID = "EditVideoInfo";
    public static final int REQUEST_TAKE_GALLERY_VIDEO = 1;
    private VideoInfo editVideoInfo;
    private VideoTimelineController videoTimelineControllerSplit;
    private VideoTimelineController videoTimelineControllerCut;
    private FrameLayout videoContainer;
    private VideoFilteredView videoFilteredView;
    private MediaExtractor mediaExtractor;
    private PlayerController playerController;
    private ViewPager viewPager;
    private VideoInfoFragment videoInfoFragment;
    private FilterListFragment filterListFragment;
    private Toolbar toolbar;

    public interface IResultCallbackTakeVideoInfo {
        void call(VideoInfo data);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_editor);
        videoContainer = findViewById(R.id.videoContainer);
        viewPager = (ViewPager)findViewById(R.id.viewPager_editor);
        TabLayout tabs = (TabLayout) findViewById(R.id.tabsEditor);
        editVideoInfo = (VideoInfo) getIntent().getParcelableExtra(EDIT_VIDEO_ID);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        playerController = new PlayerController(this,editVideoInfo.getPath());
        videoTimelineControllerSplit = VideoTimelineController.newInstance(editVideoInfo,playerController,false);
        videoTimelineControllerCut = VideoTimelineController.newInstance(editVideoInfo,playerController,true);
        videoInfoFragment = new VideoInfoFragment(editVideoInfo);
        filterListFragment = new FilterListFragment(editVideoInfo);
        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager(),
                androidx.fragment.app.FragmentPagerAdapter.POSITION_NONE,this);

        fragmentPagerAdapter.addItem(videoTimelineControllerCut);
        fragmentPagerAdapter.addItem(videoTimelineControllerSplit);
        fragmentPagerAdapter.addItem(filterListFragment);
        fragmentPagerAdapter.addItem(videoInfoFragment);

        viewPager.setAdapter(fragmentPagerAdapter);
        mediaExtractor = new MediaExtractor();

        tabs.setupWithViewPager(viewPager);

        initTabs(tabs);

        try {
            mediaExtractor.setDataSource(editVideoInfo.getPath());
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
        videoFilteredView.onPause();
        if (Util.SDK_INT < 24) {
            playerController.releasePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoFilteredView.onResume();
        playerController.hideSystemUi();
        if ((Util.SDK_INT < 24 || playerController.getPlayer() == null)) {
            playerController.initializePlayer();
        }
        videoFilteredView.updatePlayerController(playerController);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("editVideoInfo",editVideoInfo);
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

    public void ClickAddVideo(View view) throws InterruptedException {
        videoTimelineControllerSplit.addVideoToTimeline(editVideoInfo);
    }

    public BaseFilter getCurrentFilter()
    {
        return videoFilteredView.getCurrentFilter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        return true;
    }
}
