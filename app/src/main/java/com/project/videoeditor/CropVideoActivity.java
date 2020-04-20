package com.project.videoeditor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.view.GestureCropImageView;
import com.yalantis.ucrop.view.UCropView;

import java.io.File;

public class CropVideoActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 2222;
    private UCropView cropView;
    public static final String FRAME_BITMAP_URI = "frame_bitmap";
    private Bitmap currentFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_video);

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        String path =  getIntent().getStringExtra(FRAME_BITMAP_URI);
        mediaMetadataRetriever.setDataSource(path);
        currentFrame = mediaMetadataRetriever.getFrameAtTime(20000);
        cropView = (UCropView) findViewById(R.id.CropView);
        cropView.getCropImageView().setImageBitmap(currentFrame);
        cropView.getCropImageView().showContextMenu();
        cropView.getOverlayView().setShowCropFrame(false);
        cropView.getOverlayView().setShowCropGrid(false);
        cropView.getOverlayView().setDimmedColor(Color.TRANSPARENT);
        UCrop.of(null,null)
                .withAspectRatio(16, 9)
                .withMaxResultSize(1920, 1080)
                .start(this);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE_WRITE_ACCESS_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
