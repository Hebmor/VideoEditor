package com.project.videoeditor;

import android.content.Intent;
import android.net.Uri;
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
 * Use the {@link SettingsMPEG4Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsMPEG4Fragment extends Fragment {
    private View viewPointer;
    private static final int FOLDERPICKER_CODE = 101;
    private String selectedFormat;
    static private VideoInfo videoInfo;
    public SettingsMPEG4Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment SettingsMPEG4Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsMPEG4Fragment newInstance(VideoInfo _videoInfo) {
        SettingsMPEG4Fragment fragment = new SettingsMPEG4Fragment();
        videoInfo = _videoInfo;
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
        View view = inflater.inflate(R.layout.fragment_settings_mpeg4, container, false);
        view.findViewById(R.id.buttonSelectFolderPath).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectPath();
            }
        });
        Spinner spinner = (Spinner) view.findViewById(R.id.Spinner_FormatVideoFile);

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
                    Uri uri = data.getData();
                    Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                            DocumentsContract.getTreeDocumentId(uri));
                    String path2 = getPath(getContext(), docUri);
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
