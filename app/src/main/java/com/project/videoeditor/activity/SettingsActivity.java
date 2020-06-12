package com.project.videoeditor.activity;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.project.videoeditor.R;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements PreferenceManager.OnPreferenceTreeClickListener {

        private FilePickerDialog dialog;
        private String lastKeyPreferenceClick;
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            DialogProperties properties = new DialogProperties();
            properties.selection_mode = DialogConfigs.SINGLE_MODE;
            properties.selection_type = DialogConfigs.DIR_SELECT;
            properties.root = new File(DialogConfigs.DEFAULT_DIR);
            properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
            properties.offset = null;
            properties.extensions = null;

            dialog = new FilePickerDialog(getContext(),properties);
            dialog.setTitle("Выберете директорию");
            dialog.setDialogSelectionListener(new DialogSelectionListener() {
                @Override
                public void onSelectedFilePaths(String[] files) {
                    if(files.length == 1) {
                        String dirPath = files[0];
                        findPreference(lastKeyPreferenceClick).setSummary(dirPath);
                    }
                }
            });

            findPreference("dir_encode_preference").setSummary( this.getAbsoluteFile("EncodeVideo",getContext()).getAbsolutePath());
            findPreference("dir_filter_preference").setSummary( this.getAbsoluteFile("FilteredVideo",getContext()).getAbsolutePath());
            findPreference("dir_frames_extract_preference").setSummary( this.getAbsoluteFile("ExtractFrames",getContext()).getAbsolutePath());
            findPreference("dir_audio_extract_preference").setSummary( this.getAbsoluteFile("ExtractAudio",getContext()).getAbsolutePath());


        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            String key = preference.getKey();
            switch (key)
            {
                case "dir_encode_preference":
                case "dir_audio_extract_preference":
                case "dir_frames_extract_preference":
                case "dir_filter_preference":
                    dialog.show();
                    break;
                default:
                    return false;

            }
            lastKeyPreferenceClick = key;
            return true;
        }

        private File getAbsoluteFile(String relativePath, Context context) {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                return new File(context.getExternalFilesDir(null), relativePath);
            } else {
                return new File(context.getFilesDir(), relativePath);
            }
        }
    }
}