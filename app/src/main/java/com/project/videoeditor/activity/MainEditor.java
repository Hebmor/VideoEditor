package com.project.videoeditor.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.project.videoeditor.R;
import com.project.videoeditor.VideoInfo;
import com.project.videoeditor.VideoInfoPage;
import com.project.videoeditor.filters.FiltersVideoActivity;

public class MainEditor extends AppCompatActivity {

    public static final String EDIT_VIDEO_ID = "6001";
    private VideoInfo editVideoInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_editor);

        editVideoInfo = (VideoInfo) getIntent().getParcelableExtra(EDIT_VIDEO_ID);
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

}
