package com.project.videoeditor.filters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaExtractor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.VideoView;

import com.project.videoeditor.PlayerController;
import com.project.videoeditor.R;
import com.project.videoeditor.VideoFilteredView;
import com.project.videoeditor.VideoInfo;

import java.io.IOException;

public class FiltersVideoActivity extends Activity {

    public static final String EDIT_VIDEO_ID = "6001";
    private VideoView videoView;
    private FrameLayout filteredVideoContainer;
    private FiltersHandler filtersHandler;
    private String videoPath;
    private MediaExtractor mediaExtractor = null;
    private VideoFilteredView videoFilteredView;
    private boolean isRecording = false;
    private VideoInfo editVideoInfo;
    private PlayerController playerController;
    private FilterExecutor filterExecutor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_filters_video);
        //videoView = this.findViewById(R.id.videoView);
        filteredVideoContainer = this.findViewById(R.id.videoContainer);
        editVideoInfo = ((VideoInfo) getIntent().getParcelableExtra(EDIT_VIDEO_ID));
        String path = editVideoInfo.getPath();
        int framerate = (int)Float.parseFloat(editVideoInfo.getFrameRate());
        videoView.setVideoPath(path);
        mediaExtractor = new MediaExtractor();
        playerController = new PlayerController(this,path);
        filterExecutor = new FilterExecutor(this);


        try {
            mediaExtractor.setDataSource(path);

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            filterExecutor.setupSettings(editVideoInfo.getBitrate() * 1024,path,framerate,new BlackWhiteFilter(this));
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
            videoFilteredView = new VideoFilteredView(this,playerController);
         //   videoFilteredView.changeFragmentShader(UtilUri.OpenRawResourcesAsString(this,R.raw.black_and_white));
           // videoFilteredView.
        } catch (Exception e) {
            e.printStackTrace();
        }
        filteredVideoContainer.addView(videoFilteredView);
        //videoFilteredView.changeFragmentShader(UtilUri.OpenRawResourcesAsString(this,R.raw.black_and_white));
        videoPath = path;
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


    public void ClicksStartRecordVideoFilter(View view) throws IOException {
        videoFilteredView.changeFragmentShader(FiltersHandler.nameFilters.DEFAULT);
    }
    public void ClicksStopRecordVideoFilter(View view) throws IOException {
        videoFilteredView.changeFragmentShader(FiltersHandler.nameFilters.BLACK_AND_WHITE);
    }
    public void ClicksTestRecordVideoFilter(View view) throws Exception {

        filterExecutor.start();
    }
    private Bitmap getBitmapFromView(View view)
    {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        videoView.draw(canvas);
        return bitmap;
    }
}
