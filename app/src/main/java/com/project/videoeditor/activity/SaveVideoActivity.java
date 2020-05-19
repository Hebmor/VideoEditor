package com.project.videoeditor.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.jaygoo.widget.RangeSeekBar;
import com.project.videoeditor.R;
import com.project.videoeditor.VideoInfo;
import com.project.videoeditor.codecs.ActionEditor;
import com.project.videoeditor.codecs.Codecs;
import com.project.videoeditor.database.PresetCollection;
import com.project.videoeditor.database.PresetEntity;
import com.project.videoeditor.database.PresetEntityViewModel;
import com.project.videoeditor.support.UtilUri;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import java.io.File;
import java.util.List;

public class SaveVideoActivity extends AppCompatActivity {

    private PresetEntityViewModel mExpansionViewModel;
    private PresetCollection expansionCollection;
    private Spinner videoResolutionSpinner;
    private Spinner formatSpinner;
    private Spinner framerateSpinner;
    private IndicatorSeekBar  bitrateIndicator;
    private TextView computeOutputFileSize;
    private VideoInfo editVideoInfo;
    private RadioGroup radioGroupCodecs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_encoders);
        videoResolutionSpinner = findViewById(R.id.spinner_videoResolution);
        formatSpinner = findViewById(R.id.spinner_format);
        bitrateIndicator = findViewById(R.id.bitrateIndicator);
        computeOutputFileSize = findViewById(R.id.textView_computeSizeValue);
        framerateSpinner = findViewById(R.id.spinner_framerate);
        radioGroupCodecs = findViewById(R.id.radioGroupCodecs);

        editVideoInfo = (VideoInfo) getIntent().getParcelableExtra(VideoInfo.class.getCanonicalName());
        bitrateIndicator.setIndicatorTextFormat("${PROGRESS} mb/s");
        mExpansionViewModel = new ViewModelProvider(this).get(PresetEntityViewModel.class);
        mExpansionViewModel.getAllPreset().observe(this, new Observer<List<PresetEntity>>() {
            @Override
            public void onChanged(@Nullable final List<PresetEntity> preset) {

                expansionCollection = new PresetCollection(preset);
                SpinnerAdapter adapterResolutionVideo = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_list_item_1,expansionCollection.getAllResolutionVideo());
                SpinnerAdapter adapterNameFormat = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_list_item_1,expansionCollection.getAllNameFormat());
                SpinnerAdapter adapterAspectRatio = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_list_item_1,expansionCollection.getAllAspectRatio());
                videoResolutionSpinner.setAdapter(adapterResolutionVideo);
                formatSpinner.setAdapter(adapterNameFormat);
            }
        });

        formatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                videoResolutionSpinner.setSelection(position);
                bitrateIndicator.setProgress(expansionCollection.getById(position).getCBR());
                computeOutputFileSize.setText(String.valueOf(computeOutputFileSize(expansionCollection.getById(position).getCBR(),
                        editVideoInfo.getDuration()) + " MB"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        bitrateIndicator.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                computeOutputFileSize.setText(String.valueOf(computeOutputFileSize(seekBar.getProgressFloat(),
                        editVideoInfo.getDuration()) + " MB"));
            }
        });
    }

    private float computeOutputFileSize(float cbrInMbit,float videoDurationInMs)
    {
        return cbrInMbit / 8 * videoDurationInMs / 1000;
    }
    public void clickSaveVideo(View view) throws Exception {

        String inputVideoPath = editVideoInfo.getPath();
        File folder = UtilUri.CreateFolder(this.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath() + "/" + "EncodeVideo");
        String outputVideoPath = folder.getAbsolutePath() + "/" + editVideoInfo.getFilename();
        float bitrateInMbit = bitrateIndicator.getProgressFloat();
        String framerate = (String) framerateSpinner.getSelectedItem();
        long fromTimeMS = 0;
        long toTimeMS = 0;
        Codecs.CodecsName codec = Codecs.fromString(getSelectedTextFromRadioGroup());
        String scaleResolution = ((String) videoResolutionSpinner.getSelectedItem()).replace("×","x");
        ActionEditor.executeCommand(inputVideoPath,outputVideoPath,bitrateInMbit,framerate,fromTimeMS,toTimeMS,codec,scaleResolution);
    }
    public String getSelectedTextFromRadioGroup()
    {
        int radioButtonID = radioGroupCodecs.getCheckedRadioButtonId();
        View radioButton = radioGroupCodecs.findViewById(radioButtonID);
        int idx = radioGroupCodecs.indexOfChild(radioButton);
        RadioButton r = (RadioButton) radioGroupCodecs.getChildAt(idx);
        return r.getText().toString();
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
