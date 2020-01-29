package com.project.videoeditor;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.fragment.app.Fragment;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.LogCallback;
import com.arthenica.mobileffmpeg.LogMessage;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.arthenica.mobileffmpeg.FFmpeg.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.FFmpeg.RETURN_CODE_SUCCESS;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_WRITE = 1001;
    private static final int REQUEST_TAKE_GALLERY_VIDEO = 100;

    private VideoView videoView;
    private VideoInfo info;
    private Uri selectedVideoUri;
    private DialogEncodeProcess dialogEncodeProcess;
    private VideoTimeline videoEditBarFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoView = findViewById(R.id.videoView);
        dialogEncodeProcess = new DialogEncodeProcess();
        videoEditBarFragment =  (VideoTimeline)getSupportFragmentManager().findFragmentById(R.id.fragment);
        //FFmpeg.execute("-encoders");
        //Config.setLogLevel(Level.AV_LOG_FATAL);
    }
    private void OpenFile(String filename)
    {
        if(!checkPermissions()){
            return;
        }
        File file = new File(Environment.getExternalStorageDirectory(), filename);
        if(!file.exists()) Toast.makeText(this, "Файл не найдет!", Toast.LENGTH_LONG).show();
        Toast.makeText(this, "Файл успешно найдет!", Toast.LENGTH_LONG).show();
        String newFile = file.getAbsolutePath().substring(0,file.getAbsolutePath().lastIndexOf(File.separator)) + File.separator + "hl2.avi";
        FFmpeg.execute("-i "+ file.getAbsolutePath() + " " + newFile);

    }
    private boolean checkPermissions(){

        if(!isExternalStorageReadable() || !isExternalStorageWriteable()){
            Toast.makeText(this, "Внешнее хранилище не доступно", Toast.LENGTH_LONG).show();
            return false;
        }
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE);
            return false;
        }
        return true;
    }
    // проверяем, доступно ли внешнее хранилище для чтения и записи
    public boolean isExternalStorageWriteable(){
        String state = Environment.getExternalStorageState();
        return  Environment.MEDIA_MOUNTED.equals(state);
    }
    // проверяем, доступно ли внешнее хранилище хотя бы только для чтения
    public boolean isExternalStorageReadable(){
        String state = Environment.getExternalStorageState();
        return  (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }

    private void UploadVideo()
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
    public void OpenVideoInfoPage(View view)
    {

        Intent intent = new Intent(this, VideoInfoPage.class);
        intent.putExtra(VideoInfo.class.getCanonicalName(),info);
        startActivity(intent);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                selectedVideoUri = data.getData();
                videoView.setVideoURI(selectedVideoUri);
                MediaController mediaController = new MediaController(this);
                videoView.setMediaController(mediaController);
                mediaController.setMediaPlayer(videoView);
                videoView.start();
                String path = ConvertUriToFilePath.getPath(this,selectedVideoUri);
                boolean a = isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE);
                boolean b = isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                File file = new File(path);
                if(!file.exists()) Toast.makeText(this, "Файл не найдет!", Toast.LENGTH_LONG).show();
                info = new VideoInfo(path);
                info.DeleteFrameCollage();
                ActionEditor.setVideoInfo(info);
                try {
                    Path patht = Paths.get(path);


                    Runnable task = () -> {
                        try {
                            ActionEditor.EncodeProcess("MPEG4",path,patht.getParent()+"/encode.mp4");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    };
                    Thread thread = new Thread(task);
                    Bundle args = new Bundle();
                    args.putParcelable("VideoInfo",info);
                    videoEditBarFragment.putArguments(args);
                    videoEditBarFragment.setFramesFromVideo(ActionEditor.GenFrameCollage(path,this));
                    //thread.start();
                    //dialogEncodeProcess.show(getSupportFragmentManager(), "custom");


                    //thread.join();
                    //progressBar.setVisibility(View.INVISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }
    private void getPermission() {
        String[] params = null;
        String writeExternalStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        String readExternalStorage = Manifest.permission.READ_EXTERNAL_STORAGE;

        int hasWriteExternalStoragePermission = ActivityCompat.checkSelfPermission(this, writeExternalStorage);
        int hasReadExternalStoragePermission = ActivityCompat.checkSelfPermission(this, readExternalStorage);
        List<String> permissions = new ArrayList<String>();

        if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(writeExternalStorage);
        if (hasReadExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(readExternalStorage);

        if (!permissions.isEmpty()) {
            params = permissions.toArray(new String[permissions.size()]);
        }
        if (params != null && params.length > 0) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    params,
                    100);
        } else
            UploadVideo();
    }
    public void ClickUploadVideo(View view) {
        if (Build.VERSION.SDK_INT >= 28)
            getPermission();
        else
            UploadVideo();
    }
    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            Log.e("FunctionError", "getRealPathFromURI Exception : " + e.toString());
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    public void enableLogCallback() {
        Config.enableLogCallback(new LogCallback() {
            public void apply(LogMessage message) {
                Log.d(Config.TAG, message.getText());
            }
        });
    }
    private boolean isPermissionGranted(String permission) {
        // проверяем разрешение - есть ли оно у нашего приложения
        int permissionCheck = ActivityCompat.checkSelfPermission(this, permission);
        // true - если есть, false - если нет
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    public void TestCallBack(View view) {

        Config.enableLogCallback(new LogCallback() {
            public void apply(LogMessage message) {
                Log.d(Config.TAG, message.getText());
            }
        });
        Config.enableStatisticsCallback(new StatisticsCallback() {
            public void apply(Statistics newStatistics) {
                Log.d(Config.TAG, String.format("frame: %d, time: %d", newStatistics.getVideoFrameNumber(), newStatistics.getTime()));
            }
        });
        //int rc = FFmpeg.execute(" -hide_banner -f lavfi -i nullsrc -c:v libx264 -preset help -f mp4 -");
        int rc = FFmpeg.execute(" -encoders");
        if (rc == RETURN_CODE_SUCCESS) {
            Log.i(Config.TAG, "Command execution completed successfully.");
        } else if (rc == RETURN_CODE_CANCEL) {
            Log.i(Config.TAG, "Command execution cancelled by user.");
        } else {
            Log.i(Config.TAG, String.format("Command execution failed with rc=%d and the output below.", rc));
            //Config.printLastCommandOutput(Log.INFO);
        }
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        try {
            trimCache(this);
            // Toast.makeText(this,"onDestroy " ,Toast.LENGTH_LONG).show();
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
