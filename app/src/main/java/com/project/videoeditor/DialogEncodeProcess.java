package com.project.videoeditor;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.fragment.app.DialogFragment;

public class DialogEncodeProcess extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_loading, null))

                .setNegativeButton(R.string.сancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DialogEncodeProcess.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
    void showDialog() {
        // Create the fragment and show it as a dialog.
        this.show(getFragmentManager(), "dialog");
    }
}
