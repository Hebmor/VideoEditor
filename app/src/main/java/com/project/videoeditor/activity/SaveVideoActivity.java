package com.project.videoeditor.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.arthenica.mobileffmpeg.Config;
import com.project.videoeditor.dialogs.LoadingEncodeDialog;
import com.project.videoeditor.R;
import com.project.videoeditor.VideoInfo;
import com.project.videoeditor.codecs.ActionEditor;
import com.project.videoeditor.codecs.Codecs;
import com.project.videoeditor.database.PresetCollection;
import com.project.videoeditor.database.PresetEntity;
import com.project.videoeditor.database.PresetEntityViewModel;
import com.project.videoeditor.filters.BaseFilter;
import com.project.videoeditor.filters.FilterExecutor;
import com.project.videoeditor.filters.FiltersFactory;
import com.project.videoeditor.support.SupportUtil;
import com.project.videoeditor.support.TimeUtil;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SaveVideoActivity extends AppCompatActivity {

    private PresetEntityViewModel mExpansionViewModel;
    private PresetCollection expansionCollection;
    private Spinner videoResolutionSpinner;
    private Spinner formatValueSpinner;
    private Spinner framerateInfoSpinner;
    private Spinner expansionSpinner;
    private IndicatorSeekBar  bitrateIndicator;
    private TextView computeOutputFileSize;
    private VideoInfo editVideoInfo;
    private float beginValue = 0;
    private float endValue = 0;
    private LoadingEncodeDialog loadingEncodeDialog;
    private FilterExecutor filterExecutor;
    private BaseFilter currentFilter;
    private float videoSpeed = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_encoders);

        videoResolutionSpinner = findViewById(R.id.spinner_videoResolution);
        formatValueSpinner = findViewById(R.id.spinner_format);
        bitrateIndicator = findViewById(R.id.bitrateIndicator);
        computeOutputFileSize = findViewById(R.id.textView_computeSizeValue);
        framerateInfoSpinner = findViewById(R.id.spinner_framerate);
        expansionSpinner = findViewById(R.id.spinner_expansion);



        editVideoInfo = (VideoInfo) getIntent().getParcelableExtra(VideoInfo.class.getCanonicalName());

        beginValue = getIntent().getFloatExtra("beginValue",0f);
        endValue = getIntent().getFloatExtra("endValue",0f);
        currentFilter = getIntent().getParcelableExtra("filter");
        videoSpeed = getIntent().getFloatExtra("videoSpeed",1f);

        filterExecutor = new FilterExecutor(this);

        //currentFilter = getIntent().get

        if(beginValue >= endValue)
            endValue = beginValue = 0;

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
                formatValueSpinner.setAdapter(adapterNameFormat);
            }
        });

        formatValueSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        String filename = editVideoInfo.getFilename();
        int idx = filename.indexOf(".");
        String name = filename.substring(0,idx);
        String extension = (String) expansionSpinner.getSelectedItem();
        String outputVideoPath = SupportUtil.getSettingEncodeVideoPath(this) + "/" + name + "_" +  TimeUtil.getTimeInString() + "." + extension;
        float bitrateInMbit = bitrateIndicator.getProgressFloat();
        String framerate = (String) framerateInfoSpinner.getSelectedItem();
        long fromTimeMS = (long) beginValue;
        long toTimeMS = (long) endValue;
        String[] resolution = ((String)videoResolutionSpinner.getSelectedItem()).split("[×]");
        int width = Integer.parseInt(resolution[0]);
        int height = Integer.parseInt(resolution[1]);

        String scaleResolution = ((String) videoResolutionSpinner.getSelectedItem()).replace("×","x");
        FiltersFactory.NameFilters nameFilter = currentFilter.getFilterName();

        if(nameFilter != FiltersFactory.NameFilters.DEFAULT)
        {
            currentFilter.setContext(this);
            filterExecutor.setupSettings(width, height, (long) (bitrateInMbit * 1000 * 1000), inputVideoPath, Integer.parseInt(framerate), currentFilter);
            filterExecutor.launchApplyFilterToVideo((int)fromTimeMS,(int)toTimeMS);
        }
        else {


            Codecs.CodecsName codec = Codecs.fromString(SupportUtil.getSettingCodecEncode(this));
            ActionEditor.executeCommand(inputVideoPath, outputVideoPath , bitrateInMbit, framerate, fromTimeMS, toTimeMS, codec, scaleResolution,videoSpeed);
            Handler handler = new Handler();
            loadingEncodeDialog = new LoadingEncodeDialog(this);
            loadingEncodeDialog.startLoadingDialog();

            handler.post(new Runnable() {
                long durationChunk = toTimeMS - fromTimeMS - 30;
                long ms = 0;
                long secs = 0;
                long hours = 0;
                long minutes = 0;

                @Override
                public void run() {
                    if (durationChunk == 0)
                        durationChunk = editVideoInfo.getDuration();
                    double encodeSpeed = Config.getLastReceivedStatistics().getSpeed();
                   // Log.d("ERROR", String.valueOf(Config.getLastReceivedStatistics().));

                     ms = (long) ((durationChunk - Config.getLastReceivedStatistics().getTime()));
                     secs = TimeUnit.MILLISECONDS.toSeconds(ms) % 60;
                     hours = TimeUnit.MILLISECONDS.toHours(ms) % 24;
                     minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60;

                     if(ms == 0)
                         loadingEncodeDialog.updateCountdown("Ожидание данных...");
                     else
                         loadingEncodeDialog.updateCountdown(String.format(Locale.getDefault(),
                            "%d:%02d:%02d", hours, minutes, secs));
                    if (ms > 0 && Config.getLastReturnCode() == 0) {
                        handler.postDelayed(this, 10);
                    } else {
                        loadingEncodeDialog.dismissDialog();
                    }
                }
            });
        }
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
