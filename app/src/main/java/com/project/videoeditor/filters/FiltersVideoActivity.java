package com.project.videoeditor.filters;

import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.project.videoeditor.PlayerController;
import com.project.videoeditor.R;
import com.project.videoeditor.VideoFilteredView;
import com.project.videoeditor.VideoInfo;

public class FiltersVideoActivity extends Activity {

    public static final String EDIT_VIDEO_ID = "6001";
    private FrameLayout filteredVideoContainer;
    private FiltersFactory filtersFactory;
    private String videoPath;
    private VideoFilteredView videoFilteredView;
    private boolean isRecording = false;
    private VideoInfo editVideoInfo;
    private PlayerController playerController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        filteredVideoContainer = this.findViewById(R.id.videoContainer);
        editVideoInfo = ((VideoInfo) getIntent().getParcelableExtra(EDIT_VIDEO_ID));
        String path = editVideoInfo.getPath();
        int framerate = (int)Float.parseFloat(editVideoInfo.getFrameRate());
        playerController = new PlayerController(this,path);
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
         //   videoFilteredView.changeFragmentShader(SupportUtil.OpenRawResourcesAsString(this,R.raw.black_and_white));
           // videoFilteredView.
        } catch (Exception e) {
            e.printStackTrace();
        }
        filteredVideoContainer.addView(videoFilteredView);
        //videoFilteredView.changeFragmentShader(SupportUtil.OpenRawResourcesAsString(this,R.raw.black_and_white));
        videoPath = path;

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
