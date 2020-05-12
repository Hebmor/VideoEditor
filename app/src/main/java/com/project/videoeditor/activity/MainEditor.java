package com.project.videoeditor.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.media.MediaExtractor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import com.google.android.exoplayer2.util.Util;
import com.project.videoeditor.PlayerController;
import com.project.videoeditor.R;
import com.project.videoeditor.VideoFilteredView;
import com.project.videoeditor.VideoInfo;
import com.project.videoeditor.VideoInfoPage;
import com.project.videoeditor.VideoTimeline;
import com.project.videoeditor.codecs.ActionEditor;
import com.project.videoeditor.filters.BlackWhiteFilter;
import com.project.videoeditor.filters.FilterExecutor;
import com.project.videoeditor.filters.FiltersVideoActivity;

import java.io.IOException;

public class MainEditor extends AppCompatActivity {

    public static final String EDIT_VIDEO_ID = "6001";
    private VideoInfo editVideoInfo;
    private VideoTimeline videoTimeline;
    private FragmentManager fragmentManager;
    private FrameLayout videoContainer;
    private VideoFilteredView videoFilteredView;
    private FilterExecutor filterExecutor;
    private MediaExtractor mediaExtractor;
    private VideoView videoView;
    private PlayerController playerController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_editor);
        videoContainer = findViewById(R.id.videoContainer);
        fragmentManager = getSupportFragmentManager();
        editVideoInfo = (VideoInfo) getIntent().getParcelableExtra(EDIT_VIDEO_ID);
        videoTimeline = new VideoTimeline();
        int framerate = (int)Float.parseFloat(editVideoInfo.getFrameRate());


        videoView = new VideoView(this);

        mediaExtractor = new MediaExtractor();
        filterExecutor = new FilterExecutor(this);

        videoTimeline.setVideoInfo(editVideoInfo);

        playerController = new PlayerController(this,editVideoInfo.getPath());
        videoTimeline.setPlayerController(playerController);
        AddFragment(R.id.containerFrag,videoTimeline,"videoTimeline_cut");


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
        Intent intent = new Intent(this, VideoInfoPage.class);
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
    private void AddFragment(int containerViewId, Fragment fragment, String Tag)
    {
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        fragmentTransaction.add(containerViewId, fragment,
                Tag);
        fragmentTransaction.commit();
    }
    private void ChangeFragment(int containerViewId, Fragment fragment,String Tag)
    {
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        fragmentTransaction.replace(containerViewId, fragment,
                Tag);
        fragmentTransaction.commit();
    }
}
