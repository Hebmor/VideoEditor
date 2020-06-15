package com.project.videoeditor.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.exoplayer2.PlaybackParameters;
import com.project.videoeditor.PlayerController;
import com.project.videoeditor.R;
import com.project.videoeditor.filters.PixelationFilter;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import static com.project.videoeditor.filters.PixelationFilter.DEFAULT_PIXEL_SIZE;

public class SpeedDialog {

    private Activity activity;
    private AlertDialog alertDialog;
    private IndicatorSeekBar indicatorSeekBarSpeed;

    private float speedValue = 0;
    private float beginSpeedValue = 0;
    private TextView speedTextView;

    private Button buttonCancelDialog;
    private Button buttonSaveDialog;
    private Button buttonDropDialog;

    public SpeedDialog(Activity activity) {
        this.activity = activity;
    }
    PlayerController playerController;
    
    public void startLoadingDialog(PlayerController playerController)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);
        this.playerController = playerController;
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        builder.setView(layoutInflater.inflate(R.layout.speed_dialog,null));
        builder.setCancelable(true);
        alertDialog = builder.create();
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        alertDialog.getWindow().getAttributes().verticalMargin = 0.3F;
        alertDialog.show();

        beginSpeedValue = speedValue;
        indicatorSeekBarSpeed = alertDialog.findViewById(R.id.indicatorSeekBarSpeed);


        buttonCancelDialog = alertDialog.findViewById(R.id.buttonCancelDialog);
        buttonSaveDialog = alertDialog.findViewById(R.id.buttonSaveDialog);
        buttonDropDialog = alertDialog.findViewById(R.id.buttonDropDialog);
        speedTextView = alertDialog.findViewById(R.id.textViewSpeed);

        indicatorSeekBarSpeed.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                if(seekParams.fromUser)
                    speedValue = seekParams.progressFloat;
                    speedTextView.setText(String.valueOf(speedValue));
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                PlaybackParameters param = new PlaybackParameters(seekBar.getProgressFloat());
                playerController.getPlayer().setPlaybackParameters(param);

            }
        });
        buttonCancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speedValue = beginSpeedValue;
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
                speedValue = 0;
                indicatorSeekBarSpeed.setProgress(0);
            }
        });
    }

    public void dismissDialog()
    {
        alertDialog.dismiss();
    }

    public float getSpeedValue() {
        return speedValue;
    }

}
