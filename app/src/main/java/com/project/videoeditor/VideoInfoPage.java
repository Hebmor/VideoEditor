package com.project.videoeditor;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class VideoInfoPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_info_page);
        VideoInfo videoInf = (VideoInfo) getIntent().getParcelableExtra(VideoInfo.class.getCanonicalName());
        TextView tv1 = findViewById(R.id.textViewVideoAspect);
        TextView tv2 = findViewById(R.id.textViewVideoWidth);
        TextView tv3 = findViewById(R.id.textViewVideoStartTime);
        TextView tv4 = findViewById(R.id.textViewVideoSize);
        TextView tv5 = findViewById(R.id.textViewVideoPath);
        TextView tv6 = findViewById(R.id.textViewVideoHeight);
        TextView tv7 = findViewById(R.id.textViewVideoFramerate);
        TextView tv8 = findViewById(R.id.textViewVideoFormat);
        TextView tv9 = findViewById(R.id.textViewVideoDuration);
        TextView tv10 = findViewById(R.id.textViewVideoCodec);
        TextView tv11 = findViewById(R.id.textViewVideoBitrate);
        tv1.setText(videoInf.getAspectRatio());
        tv2.setText(videoInf.getWidth().toString());
        tv3.setText(videoInf.getStartTime().toString());
        tv4.setText("--------");
        tv5.setText(videoInf.getPath());
        tv6.setText(videoInf.getHeight().toString());
        tv7.setText(videoInf.getFrameRate().toString() + " кадров/с");
        tv8.setText(videoInf.getFormat());
        tv9.setText(videoInf.getDuration().toString());
        tv10.setText(videoInf.getCodec());
        tv11.setText(videoInf.getBitrate().toString() + " кб/с");
    }
}
