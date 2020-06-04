package com.project.videoeditor.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import com.project.videoeditor.FilenameDialogFragment;
import com.project.videoeditor.R;
import com.project.videoeditor.VideoInfo;
import com.project.videoeditor.VideoTimelineController;
import com.project.videoeditor.codecs.ActionEditor;
import com.project.videoeditor.support.UtilUri;

public class VideoEditPage extends AppCompatActivity implements FilenameDialogFragment.FilenameDialogFragmentListener {

    private VideoInfo videoInfo;
    private VideoView videoView;
    private VideoTimelineController videoTimelineController;
    private static final int FOLDERPICKER_CODE = 101;
    private String defaultSavePath;
    private TextView textViewSelectPath;
    private String filename;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_edit_page);
        videoInfo = (VideoInfo) getIntent().getParcelableExtra(VideoInfo.class.getCanonicalName());
        videoView = (VideoView) this.findViewById(R.id.videoView_EditVideo);
        videoTimelineController = (VideoTimelineController)getSupportFragmentManager().findFragmentById(R.id.fragment_Timeline);

        defaultSavePath = getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath();
    }

    @Override
    protected void onStart() {
        super.onStart();
       // textViewSelectPath = this.findViewById(R.id.textViewSelectPath);
        videoView.setVideoPath(videoInfo.getPath());
        videoView.seekTo(0);
        videoView.start();
    }
    public void ClickCutVideo(View view)
    {

            FilenameDialogFragment filenameDialog = new FilenameDialogFragment();
            filenameDialog.show(getSupportFragmentManager(),"filenameDialog");
    }
    /*public void ClickSelectPath(View view)
    {
        SelectPath();
    }*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case FOLDERPICKER_CODE:
                    Uri uri = data.getData();
                    if(uri!= null)
                    {
                        Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                                DocumentsContract.getTreeDocumentId(uri));
                        textViewSelectPath.setText(UtilUri.getPath(this,docUri));

                    }
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

    @Override
    public void onFinishEditDialog(String inputText) throws InterruptedException {
        filename = inputText;
       // if(filename != null)
            //ActionEditor.CutPathFromVideo(videoInfo.getPath(),defaultSavePath + "/" + filename + videoInfo.getExtension(),(long)videoTimelineController.getSBL().getProgress(),(long)videoTimelineController.getSBR().getProgress());
    }
}
