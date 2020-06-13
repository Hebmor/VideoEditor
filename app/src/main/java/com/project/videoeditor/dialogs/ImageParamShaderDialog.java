package com.project.videoeditor.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.jaygoo.widget.RangeSeekBar;
import com.project.videoeditor.R;
import com.project.videoeditor.filters.ImageParamFilter;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

public class ImageParamShaderDialog {

    private Activity activity;
    private AlertDialog alertDialog;
    private IndicatorSeekBar indicatorSeekBarContrast;
    private IndicatorSeekBar indicatorSeekBarBrightness;
    private float contrastValue = 0;
    private float brightnessValue = 0;
    private float beginBrightnessValue = 0;
    private float beginContrastValue = 0;
    private TextView contrastTextView;
    private TextView brightnessTextView;
    private ImageParamFilter imageParamFilter;
    private Button buttonCancelDialog;
    private Button buttonSaveDialog;
    private Button buttonDropDialog;


    public ImageParamShaderDialog(Activity activity) {
        this.activity = activity;
    }

    public void startLoadingDialog(ImageParamFilter imageParamFilter)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);

        LayoutInflater layoutInflater = activity.getLayoutInflater();
        builder.setView(layoutInflater.inflate(R.layout.image_param_dialog,null));
        builder.setCancelable(true);
        alertDialog = builder.create();
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        alertDialog.getWindow().getAttributes().verticalMargin = 0.3F;
        alertDialog.show();

        indicatorSeekBarContrast = alertDialog.findViewById(R.id.indicatorSeekBarContrast);
        indicatorSeekBarBrightness = alertDialog.findViewById(R.id.indicatorSeekBarBrightness);
        contrastTextView = alertDialog.findViewById(R.id.textViewContrastValue);
        brightnessTextView = alertDialog.findViewById(R.id.textViewBrightness);
        buttonCancelDialog = alertDialog.findViewById(R.id.buttonCancelDialog);
        buttonSaveDialog = alertDialog.findViewById(R.id.buttonSaveDialog);
        buttonDropDialog = alertDialog.findViewById(R.id.buttonDropDialog);

        indicatorSeekBarContrast.setProgress(imageParamFilter.getContrast());
        indicatorSeekBarBrightness.setProgress(imageParamFilter.getBrightness());
        contrastTextView.setText(String.valueOf(imageParamFilter.getContrast()));
        brightnessTextView.setText(String.valueOf(imageParamFilter.getBrightness()));

        beginBrightnessValue = imageParamFilter.getBrightness();
        beginContrastValue = imageParamFilter.getContrast();

        buttonCancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageParamFilter.setContrast(beginContrastValue);
                imageParamFilter.setBrightness(beginBrightnessValue);
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
                contrastValue = ImageParamFilter.DEFAULT_CONTRAST;
                imageParamFilter.setContrast(contrastValue);
                contrastTextView.setText(String.valueOf(contrastValue));
                brightnessValue = ImageParamFilter.DEFAULT_BRIGHTNESS;
                imageParamFilter.setBrightness(brightnessValue);
                brightnessTextView.setText(String.valueOf(brightnessValue));
                indicatorSeekBarContrast.setProgress(contrastValue);
                indicatorSeekBarBrightness.setProgress(brightnessValue);
            }
        });

        indicatorSeekBarContrast.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                if(seekParams.fromUser)
                {
                    contrastValue = seekParams.progressFloat;
                    imageParamFilter.setContrast(contrastValue);
                    contrastTextView.setText(String.valueOf(contrastValue));
                }
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });
        indicatorSeekBarBrightness.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                if(seekParams.fromUser)
                {
                    brightnessValue = seekParams.progressFloat;
                    imageParamFilter.setBrightness(brightnessValue);
                    brightnessTextView.setText(String.valueOf(brightnessValue));
                }
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });
    }

    public void dismissDialog()
    {
        alertDialog.dismiss();
    }


    public float getBrightnessValue() {
        return brightnessValue;
    }

    public void setBrightnessValue(float brightnessValue) {
        this.brightnessValue = brightnessValue;
    }

    public float getContrastValue() {
        return contrastValue;
    }

    public void setContrastValue(float contrastValue) {
        this.contrastValue = contrastValue;
    }
}
