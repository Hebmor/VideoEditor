package com.project.videoeditor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import java.io.IOException;

import jp.co.cyberagent.android.gpuimage.GPUImage;

public class FiltersVideoActivity extends AppCompatActivity {

    public static final String EDIT_VIDEO_ID = "6001";
    private VideoView videoView;
    private FrameLayout filteredVideoContainer;
    private Filters filters;
    private String videoPath;
    private MediaPlayer mMediaPlayer = null;
    private VideoSurfaceView mVideoSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters_video);
        videoView = this.findViewById(R.id.videoView);
        filteredVideoContainer = this.findViewById(R.id.filteredVideoContainer);
        VideoInfo editVideoInfo = ((VideoInfo) getIntent().getParcelableExtra(EDIT_VIDEO_ID));
        String path = editVideoInfo.getPath();
        int framerate = (int)Float.parseFloat(editVideoInfo.getFrameRate());
        videoView.setVideoPath(path);
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(this,Uri.parse(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        mVideoSurfaceView = new VideoSurfaceView(this,mMediaPlayer);
        filteredVideoContainer.addView(mVideoSurfaceView);
        videoPath = path;
        filters = new Filters(this);
        videoView.start();
    }
    public void ClickSetFilter(View view)
    {

        filters.SetNegativeToFrame(videoPath,10000);

    }
    private Bitmap getBitmapFromView(View view)
    {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        videoView.draw(canvas);
        return bitmap;
    }
}
