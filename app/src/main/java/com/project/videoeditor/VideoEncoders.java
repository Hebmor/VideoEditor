package com.project.videoeditor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class VideoEncoders extends AppCompatActivity {

    private FragmentManager myFragmentManager;
    final static String TAG_MPEG4 = "FRAGMENT_MPEG4";
    final static String TAG_H264 = "FRAGMENT_H264";
    final static String TAG_H265 = "FRAGMENT_H265";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_encoders);

        Spinner spinner = (Spinner) findViewById(R.id.spinnerEncodersList);
        //WARNING BAD CODE!

        Spinner spinner2 = (Spinner) findViewById(R.id.ListFormat);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.Encoders, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.Formats, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner2.setAdapter(adapter2);
        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // Получаем выбранный объект
                String item = (String)parent.getItemAtPosition(position);

                switch (item)
                {
                    case "H264":
                        ChangeFragment(R.id.fragmentSettingsEncoders,new Settings_h264Fragment(),TAG_H264);
                        break;
                    case "MPEG4":
                        ChangeFragment(R.id.fragmentSettingsEncoders,new SettingsMPEG4Fragment(),TAG_MPEG4);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        myFragmentManager = getFragmentManager();;
        spinner.setOnItemSelectedListener(itemSelectedListener);
    }
    private void ChangeFragment(int containerViewId, Fragment fragment,String Tag)
    {
        FragmentTransaction fragmentTransaction = myFragmentManager
                .beginTransaction();
        fragmentTransaction.replace(R.id.fragmentSettingsEncoders, fragment,Tag);
        fragmentTransaction.commit();
    }
}
