package com.project.videoeditor.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.provider.OpenableColumns;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.project.videoeditor.R;
import com.project.videoeditor.VideoInfo;
import com.project.videoeditor.codecs.ActionEditor;
import com.project.videoeditor.support.SupportUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_GALLERY_VIDEO = 100;
    private FilePickerDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = new String[] {"mp4","avi","webm","ogg"};

        dialog = new FilePickerDialog(this,properties);
        dialog.setTitle("Выберете видео");
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                String filePath = files[0];
                intentMainEditor(filePath);
            }
        });
    }

    private boolean checkPermissions(){

        if(isExternalStorageReadable() || isExternalStorageWriteable() || isMICReadable()){
            //Toast.makeText(this, "Внешнее хранилище не доступно", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
    // проверяем, доступно ли внешнее хранилище для чтения и записи
    public boolean isExternalStorageWriteable(){
        
        return  ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
    }
    // проверяем, доступно ли внешнее хранилище хотя бы только для чтения
    public boolean isExternalStorageReadable(){

        return  ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
    }

    public boolean isMICReadable(){

        return  ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED;
    }

    private void getPermission() {
        String[] params = null;
        String writeExternalStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        String readExternalStorage = Manifest.permission.READ_EXTERNAL_STORAGE;
        String readMIC = Manifest.permission.RECORD_AUDIO;

        int hasWriteExternalStoragePermission = ActivityCompat.checkSelfPermission(this, writeExternalStorage);
        int hasReadExternalStoragePermission = ActivityCompat.checkSelfPermission(this, readExternalStorage);
        int hasReadMIC = ActivityCompat.checkSelfPermission(this, readMIC);
        List<String> permissions = new ArrayList<String>();

        if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(writeExternalStorage);
        if (hasReadExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(readExternalStorage);
        if (hasReadMIC != PackageManager.PERMISSION_GRANTED)
            permissions.add(readMIC);

        if (!permissions.isEmpty()) {
            params = permissions.toArray(new String[permissions.size()]);
        }

        if (params != null && params.length > 0) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    params,
                    100);
        }
    }

    private void intentMainEditor(String path)
    {
        VideoInfo info = new VideoInfo();
        info.parseInfoFromPath(path);
        info.setFilename(SupportUtil.getFilenameByPath(path));
        ActionEditor.setVideoInfo(info);
        Intent intent = new Intent(this, MainEditor.class);
        intent.putExtra(MainEditor.EDIT_VIDEO_ID,info);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_TAKE_GALLERY_VIDEO:
                    VideoInfo info = new VideoInfo();
                    Uri selectedVideoUri = data.getData();
                    String ffmpegPath;
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        ffmpegPath = SupportUtil.safUriToFFmpegPath(this,selectedVideoUri);
                    else
                        ffmpegPath = SupportUtil.getPath(this,selectedVideoUri);

                    String displayName = SupportUtil.getInfoByUri(this,selectedVideoUri,OpenableColumns.DISPLAY_NAME);
                    String filesize = SupportUtil.getInfoByUri(this,selectedVideoUri,OpenableColumns.SIZE);

                    info.setFilename(displayName);
                    info.setSizeInBytes(Long.parseLong(filesize));
                    info.parseInfoFromPath(ffmpegPath);
                    ActionEditor.setVideoInfo(info);
                    Intent intent = new Intent(this, MainEditor.class);
                    intent.putExtra(MainEditor.EDIT_VIDEO_ID,info);
                    startActivity(intent);
                    break;

            }
        }
    }

    private void uploadVideo()
    {
        try {
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void clickUploadVideo(View view) {
        if (!checkPermissions())
                    getPermission();
        else
            dialog.show();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try {
            trimCache(this);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

}
