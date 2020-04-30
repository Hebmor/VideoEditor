package com.project.videoeditor;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaExtractor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;

public class FiltersVideoActivity extends Activity {

    public static final String EDIT_VIDEO_ID = "6001";
    private VideoView videoView;
    private FrameLayout filteredVideoContainer;
    private Filters filters;
    private String videoPath;
    private MediaExtractor mediaExtractor = null;
    private VideoFilteredView videoFilteredView;
    private boolean isRecording = false;
    private VideoInfo editVideoInfo;
    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters_video);
        videoView = this.findViewById(R.id.videoView);
        filteredVideoContainer = this.findViewById(R.id.filteredVideoContainer);
        editVideoInfo = ((VideoInfo) getIntent().getParcelableExtra(EDIT_VIDEO_ID));
        String path = editVideoInfo.getPath();
        int framerate = (int)Float.parseFloat(editVideoInfo.getFrameRate());
        videoView.setVideoPath(path);
        mediaExtractor = new MediaExtractor();
        mediaPlayer = new MediaPlayer();
        try {
            mediaExtractor.setDataSource(path);
            mediaPlayer.setDataSource(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        try {
//            decodeEditEncodeTest.testVideoEdit720p();
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
      /*  try {
          //  videoFilteredView = new VideoFilteredView(this,mediaExtractor,editVideoInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        try {
            videoFilteredView = new VideoFilteredView(this,mediaPlayer);
         //   videoFilteredView.changeFragmentShader(UtilUri.OpenRawResourcesAsString(this,R.raw.black_and_white));
           // videoFilteredView.
        } catch (Exception e) {
            e.printStackTrace();
        }
        filteredVideoContainer.addView(videoFilteredView);
        //videoFilteredView.changeFragmentShader(UtilUri.OpenRawResourcesAsString(this,R.raw.black_and_white));
        videoPath = path;
        filters = new Filters(this);
        videoView.start();

    }

    @Override
    protected void onResume() {
        super.onResume();
       // videoFilteredView.resume();
        android.graphics.Point size = new android.graphics.Point();
        getWindowManager().getDefaultDisplay().getRealSize(size);

//       try {
//
//            int Height = Math.toIntExact(editVideoInfo.getHeight());
//            int Width = Math.toIntExact(editVideoInfo.getWidth());
//
//          // videoFilteredView.initRecorder(mOutputFile,Width,Height,null,null);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


    public void ClicksStartRecordVideoFilter(View view)
    {
        videoFilteredView.changeFragmentShader(UtilUri.OpenRawResourcesAsString(this,R.raw.black_and_white));
    }
    public void ClicksStopRecordVideoFilter(View view)
    {
        videoFilteredView.changeFragmentShader(UtilUri.OpenRawResourcesAsString(this,R.raw.default_state));
    }
    private Bitmap getBitmapFromView(View view)
    {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        videoView.draw(canvas);
        return bitmap;
    }
}
