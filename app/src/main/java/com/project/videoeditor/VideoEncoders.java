package com.project.videoeditor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


public class VideoEncoders extends AppCompatActivity {

    final static String TAG_MPEG4 = "FRAGMENT_MPEG4";
    final static String TAG_H264 = "FRAGMENT_H264";
    final static String TAG_H265 = "FRAGMENT_H265";
    private FragmentManager fragmentManager;
    private Settings_h264Fragment settingsH264Fragment;
    private SettingsMPEG4Fragment settingsMPEG4Fragment;
    private VideoInfo videoInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_encoders);

        videoInfo = (VideoInfo) getIntent().getParcelableExtra(VideoInfo.class.getCanonicalName());
        Spinner spinner = (Spinner) findViewById(R.id.spinnerEncodersList);
        //WARNING BAD CODE!
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.Encoders, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        fragmentManager = getSupportFragmentManager();
        settingsH264Fragment = Settings_h264Fragment.newInstance(videoInfo);
        settingsMPEG4Fragment = SettingsMPEG4Fragment.newInstance(videoInfo);
         AddFragment(R.id.containerFrag,settingsH264Fragment,TAG_H264);

        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // Получаем выбранный объект
                String item = (String)parent.getItemAtPosition(position);

                switch (item)
                {
                    case "H264":
                        ChangeFragment(R.id.containerFrag,settingsH264Fragment,TAG_H264);
                        break;
                    case "MPEG4":
                        ChangeFragment(R.id.containerFrag,settingsMPEG4Fragment,TAG_MPEG4);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        spinner.setOnItemSelectedListener(itemSelectedListener);
    }
    private void AddFragment(int containerViewId, Fragment fragment,String Tag)
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
