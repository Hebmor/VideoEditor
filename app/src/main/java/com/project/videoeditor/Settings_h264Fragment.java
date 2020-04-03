package com.project.videoeditor;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import static android.app.Activity.RESULT_OK;
import static com.project.videoeditor.ConvertUriToFilePath.getPath;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Settings_h264Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Settings_h264Fragment extends Fragment {
    private static final int FOLDERPICKER_CODE = 101;

    private View viewPointer;
    private Uri selectedFolderUri;
    private String selectedFormat;

    public Settings_h264Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment Settings_libx264Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Settings_h264Fragment newInstance() {
        Settings_h264Fragment fragment = new Settings_h264Fragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings_h264, container, false);
        view.findViewById(R.id.buttonSelectFolderPath).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectPath();
            }
        });
        Spinner spinner = (Spinner) view.findViewById(R.id.ListFormat);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.Formats, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        selectedFormat = "mp4";

        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // Получаем выбранный объект
                selectedFormat = (String)parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        spinner.setOnItemSelectedListener(itemSelectedListener);
        viewPointer = view;
        return view;
    }
    @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case FOLDERPICKER_CODE:
                    selectedFolderUri = data.getData();
                    Uri uri = data.getData();
                    Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                            DocumentsContract.getTreeDocumentId(uri));
                    String path2 = docUri.getPath();
                    ((EditText)viewPointer.findViewById(R.id.editText_FolderPath)).setText(path2);
                    break;
            }
        }
    }
    private void SelectPath()
    {
            try {
                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                DocumentFile file = DocumentFile.fromFile( Environment.getDataDirectory());
                //i.putExtra(EXTRA_INITIAL_URI,file.getUri());
                startActivityForResult(Intent.createChooser(i, "Choose directory"), FOLDERPICKER_CODE);
            } catch (Exception e) {

                e.printStackTrace();
            }
    }
}
