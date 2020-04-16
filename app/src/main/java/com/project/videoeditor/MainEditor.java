package com.project.videoeditor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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

}
