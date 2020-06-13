package com.project.videoeditor.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.project.videoeditor.R;
import com.project.videoeditor.filters.ImageParamFilter;
import com.project.videoeditor.filters.PixelationFilter;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import static com.project.videoeditor.filters.PixelationFilter.DEFAULT_PIXEL_SIZE;

public class PixelationShaderDialog {

    private Activity activity;
    private AlertDialog alertDialog;
    private IndicatorSeekBar indicatorSeekBarPixelSize;

    private float pixelSizeValue = 0;
    private float beginPixelSizeValue = 0;
    private TextView pixelSizeTextView;
    private PixelationFilter pixelationFilter;
    private Button buttonCancelDialog;
    private Button buttonSaveDialog;
    private Button buttonDropDialog;


    public PixelationShaderDialog(Activity activity) {
        this.activity = activity;
    }
    
    public void startLoadingDialog(PixelationFilter pixelationFilter)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);

        LayoutInflater layoutInflater = activity.getLayoutInflater();
        builder.setView(layoutInflater.inflate(R.layout.pixelation_dialog,null));
        builder.setCancelable(true);
        alertDialog = builder.create();
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        alertDialog.getWindow().getAttributes().verticalMargin = 0.3F;
        alertDialog.show();

        indicatorSeekBarPixelSize = alertDialog.findViewById(R.id.indicatorSeekBarPixelSize);
        pixelSizeTextView = alertDialog.findViewById(R.id.textViewPixelSizeValue);
        beginPixelSizeValue = pixelationFilter.getPixelSize();

        buttonCancelDialog = alertDialog.findViewById(R.id.buttonCancelDialog);
        buttonSaveDialog = alertDialog.findViewById(R.id.buttonSaveDialog);
        buttonDropDialog = alertDialog.findViewById(R.id.buttonDropDialog);
        pixelSizeTextView.setText(String.valueOf(pixelationFilter.getPixelSize()));
        indicatorSeekBarPixelSize.setProgress(pixelationFilter.getPixelSize());

        indicatorSeekBarPixelSize.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                if(seekParams.fromUser)
                {
                    pixelSizeValue = seekParams.progressFloat;
                    pixelSizeTextView.setText(String.valueOf(pixelSizeValue));
                    indicatorSeekBarPixelSize.setProgress(pixelSizeValue);
                    pixelationFilter.setPixelSize(pixelSizeValue);

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
                pixelationFilter.setPixelSize(beginPixelSizeValue);
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
                pixelSizeTextView.setText(String.valueOf(DEFAULT_PIXEL_SIZE));
                indicatorSeekBarPixelSize.setProgress(DEFAULT_PIXEL_SIZE);
                pixelationFilter.setPixelSize(DEFAULT_PIXEL_SIZE);
            }
        });
    }

    public void dismissDialog()
    {
        alertDialog.dismiss();
    }

}
