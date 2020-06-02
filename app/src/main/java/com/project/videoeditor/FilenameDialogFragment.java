package com.project.videoeditor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

public class FilenameDialogFragment extends DialogFragment {

    public interface FilenameDialogFragmentListener {
        void onFinishEditDialog(String inputText) throws InterruptedException;
    }
    private FilenameDialogFragmentListener mListener;
    private View fragmentView;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        fragmentView = getActivity().getLayoutInflater().inflate(R.layout.filename_dialog, null);
        builder.setView(inflater.inflate(R.layout.filename_dialog, null))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText editText = (EditText)((AlertDialog)dialog).findViewById(R.id.editText_FilenameDialog);
                        try {
                            mListener.onFinishEditDialog(editText.getText().toString());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FilenameDialogFragment.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }
    public void onAttach(Context context) {
        super.onAttach(context);
            // Verify that the host activity implements the playerControllerCallback interface
            try {
                // Instantiate the NoticeDialogListener so we can send events to the host
                mListener = (FilenameDialogFragmentListener) context;
            } catch (ClassCastException e) {
                // The activity doesn't implement the interface, throw exception
                throw new ClassCastException(context.toString()
                        + " must implement NoticeDialogListener");
            }

    }
}
