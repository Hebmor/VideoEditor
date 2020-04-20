package com.project.videoeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;

public class Filters {

    private final String NameFrameBuffer = "filteredBuffer.png";
    private Context context;

    public Filters(Context context) {
        this.context = context;
    }


    public Bitmap SetNegativeToFrame(String videoUrl,long timeMarkMs)
    {

        String pathBuffer = context.getCacheDir().getPath() +"/"+ NameFrameBuffer;
        String command = String.format("-y -i \"%s\" -ss %s -v:frames 1 -vf  \"curves=negative\" -c:a copy %s",videoUrl,TimeUtil.GetFormattedTimeFromMilliseconds(timeMarkMs),pathBuffer);
        try {
            ActionEditor.RunCommandExecuteFFMPEG(command,true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        File readBuffer = new File(pathBuffer);
        if(readBuffer.canRead())
        {
            Bitmap bitmap = BitmapFactory.decodeFile(pathBuffer);
            return bitmap;
        }
        return null;
    }
}
