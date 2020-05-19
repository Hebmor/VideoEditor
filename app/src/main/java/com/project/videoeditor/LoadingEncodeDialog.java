package com.project.videoeditor;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.project.videoeditor.R;

public class LoadingEncodeDialog {

    private Activity activity;
    private AlertDialog alertDialog;
    private TextView countdownView;

    public LoadingEncodeDialog(Activity activity) {
        this.activity = activity;
    }
    public void startLoadingDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);

        LayoutInflater layoutInflater = activity.getLayoutInflater();
        builder.setView(layoutInflater.inflate(R.layout.dialog_loading,null));
        builder.setCancelable(false);

        alertDialog = builder.create();
        alertDialog.show();
        countdownView = alertDialog.findViewById(R.id.textView_Countdown);
    }
    public void dismissDialog()
    {
        alertDialog.dismiss();
    }
    public void updateCountdown(String text)
    {
        countdownView.setText(text);
    }
}
