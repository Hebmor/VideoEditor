package com.project.videoeditor.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.project.videoeditor.R;
import com.project.videoeditor.filters.CelShadingFilter;
import com.project.videoeditor.filters.PixelationFilter;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import static com.project.videoeditor.filters.CelShadingFilter.DEFAULT_COLOR_COUNT;
import static com.project.videoeditor.filters.PixelationFilter.DEFAULT_PIXEL_SIZE;

public class CelShadingShaderDialog {

    private Activity activity;
    private AlertDialog alertDialog;
    private IndicatorSeekBar indicatorSeekBarColorCount;

    private int colorCountValue = 0;
    private int beginCountValue = 0;

    private TextView celShadingTextView;
    private CelShadingFilter celShadingFilter;
    private Button buttonCancelDialog;
    private Button buttonSaveDialog;
    private Button buttonDropDialog;


    public CelShadingShaderDialog(Activity activity) {
        this.activity = activity;
    }
    
    public void startLoadingDialog(CelShadingFilter celShadingFilter)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);

        LayoutInflater layoutInflater = activity.getLayoutInflater();
        builder.setView(layoutInflater.inflate(R.layout.cel_shading_dialog,null));
        builder.setCancelable(true);
        alertDialog = builder.create();
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        alertDialog.getWindow().getAttributes().verticalMargin = 0.3F;
        alertDialog.show();

        indicatorSeekBarColorCount = alertDialog.findViewById(R.id.indicatorSeekBarColorCount);
        celShadingTextView = alertDialog.findViewById(R.id.textViewColorSizeValue);
        beginCountValue = celShadingFilter.getColorsCount();

        buttonCancelDialog = alertDialog.findViewById(R.id.buttonCancelDialog);
        buttonSaveDialog = alertDialog.findViewById(R.id.buttonSaveDialog);
        buttonDropDialog = alertDialog.findViewById(R.id.buttonDropDialog);
        celShadingTextView.setText(String.valueOf(celShadingFilter.getColorsCount()));
        indicatorSeekBarColorCount.setProgress(celShadingFilter.getColorsCount());

        indicatorSeekBarColorCount.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                if(seekParams.fromUser)
                {
                    colorCountValue = seekParams.progress;
                    celShadingTextView.setText(String.valueOf(colorCountValue));
                    indicatorSeekBarColorCount.setProgress(colorCountValue);
                    celShadingFilter.setColorsCount(colorCountValue);
                }
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });
        buttonCancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                celShadingFilter.setColorsCount(beginCountValue);
                dismissDialog();

            }
        });

        buttonSaveDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialog();
            }
        });

        buttonDropDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                celShadingTextView.setText(String.valueOf(DEFAULT_COLOR_COUNT));
                indicatorSeekBarColorCount.setProgress(DEFAULT_COLOR_COUNT);
                celShadingFilter.setColorsCount(DEFAULT_COLOR_COUNT);
            }
        });
    }

    public void dismissDialog()
    {
        alertDialog.dismiss();
    }

}
