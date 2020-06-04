package com.project.videoeditor.codecs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.project.videoeditor.VideoInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class ActionEditor {

    private static VideoInfo videoInfo;
    public ActionEditor() {
    }
    public static void EncodeProcess(String codec,String filePath,String new_filePath) throws Exception {
        switch (codec)
        {
            case "MPEG4":
            {
                String command = "-y -i \"" + filePath + "\" -c:v mpeg4 "+new_filePath;
                FFmpeg.execute(command);
                break;
            }
            default:
            {
                throw new Exception("Неизвестный кодек");
            }

        }
    }
    public static void EncodeMPEG4(String filePath,String new_filePath,int qscale_video,int qscale_audio,String bitrate,String framerate) throws Exception {

        if(qscale_video < 1 && qscale_video > 31)
            throw new IllegalArgumentException("Ошибка, параметр qscale_video должен быть в диапазоне от 1 до 31");
        if(qscale_audio < 1 && qscale_audio > 31)
            throw new IllegalArgumentException("Ошибка, параметр qscale_audio должен быть в диапазоне от 1 до 31");
        String command = "-y -i \"" + filePath + "\" -c:v mpeg4 -qscale:v "+qscale_video+" -qscale:a "+qscale_audio+" -b:v "+bitrate+"k -slices 4 -r "+framerate+" "+new_filePath;
        RunCommandExecuteFFMPEG(command,false);
    }
    public static void EncodeLIBX264(String filePath,String new_filePath,Long bitrate,int framerate,String preset,String tune,int crf) throws Exception {
        if(crf < 0 && crf > 100)
            throw new IllegalArgumentException("Ошибка, параметр crf должен быть в диапазоне от 0 до 100");
        String command = "-y -i \"" + filePath + "\" -c:v libx264 -crf "+crf+" -preset "+preset+" -tune "+tune+ " -b:v "+bitrate+"k  -slices 4 -r"+framerate+" "+new_filePath;
        RunCommandExecuteFFMPEG(command,false);
    }
    public static void EncodeH265(String filePath,String new_filePath,String bitrate,String framerate,String preset,String tune,int crf) throws Exception {
        if(crf < 0 && crf > 100)
            throw new IllegalArgumentException("Ошибка, параметр crf должен быть в диапазоне от 0 до 100");
        String command = "-y -i \"" + filePath + "\" -c:v libx265 -crf "+crf+" -preset "+preset+" -tune "+tune+" -b:v "+bitrate+"k -slices 4 -r "+framerate+" "+new_filePath;
        RunCommandExecuteFFMPEG(command,false);
    }
    public static  Bitmap GetEncodeSettingsPreview(String filePath,String bitrate,String framerate,String preset,String tune,int crf, Context context) throws InterruptedException {
        Bitmap framePreview;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        String tempCachePath = context.getCacheDir().toString();
        String nameTempSettingsPreview = "/tempFrames.png";
        String oneFrameVideo = "/tempFrames.mp4";
         float frameNumber = (10 + (int)(Math.random() * videoInfo.getFrameCount())) / Float.parseFloat(videoInfo.getFrameRate());
        if(crf < 0 && crf > 100)
            throw new IllegalArgumentException("Ошибка, параметр crf должен быть в диапазоне от 0 до 100");
        String command = "-y -i \"" + filePath + "\" -frames:v 1 -vsync vfr -c:v libx265 -crf "+crf+" -acodec copy  -preset  "+preset+" -tune "+tune+" -ss "+frameNumber +" -b:v "+bitrate+"k -slices 4 -r "+framerate+" -an "+ tempCachePath + oneFrameVideo;
        RunCommandExecuteFFMPEG(command,true);
        Log.d("TEST_ERROR", String.valueOf(Config.getLastReturnCode()));
        command = "-y -i \"" + tempCachePath + oneFrameVideo + "\" -frames:v 1 -an "+ tempCachePath + nameTempSettingsPreview;
        RunCommandExecuteFFMPEG(command,true);
        Log.d("TEST_ERROR", String.valueOf(Config.getLastReturnCode()));

        framePreview = BitmapFactory.decodeFile(new File(tempCachePath + nameTempSettingsPreview).getAbsolutePath(),bmOptions);
        return framePreview;

    }
    public static String GetNamePresetEncodeByNumber(int presetNumber)
    {
        switch (presetNumber)
        {
            case 0:
                return "ultrafast";
            case 1:
                return "superfast";
            case 2:
                return "veryfast";
            case 3:
                return "faster";
            case 4:
                return "fast";
            case 5:
                return "medium";
            case 6:
                return "slow";
            case 7:
                return "slower";
            case 8:
                return "veryslow";
        }
        return "NoValue";
    }
    public static String GetNameTuneEncodeByNumber(int tuneNumber)
    {
        switch (tuneNumber)
        {
            case 0:
                return "film";
            case 1:
                return "animation";
            case 2:
                return "grain";
            case 3:
                return "fastdecode";
            case 4:
                return "zerolatency";
        }
        return "NoValue";
    }
    public static void setVideoInfo(VideoInfo videoInfo) {
        ActionEditor.videoInfo = videoInfo;
    }
    public static void RunCommandExecuteFFMPEG(String command,boolean isJoin) throws InterruptedException {
        Thread ffmpegExecuteThread = new Thread(new Runnable() {
            @Override
            public void run() {
                FFmpeg.execute(command);
            }
        });
        ffmpegExecuteThread.start();
        if(isJoin)
            ffmpegExecuteThread.join();
    }
    public static void CutPathFromVideo(String inputVideoPath,String outputVideoPath,long fromTimeMS,long toTimeMS) throws InterruptedException {

        long msFrom = fromTimeMS % 1000;
        long secsFrom = TimeUnit.MILLISECONDS.toSeconds(fromTimeMS)  % 60;
        long hoursFrom = TimeUnit.MILLISECONDS.toHours(fromTimeMS)  % 24;
        long minutesFrom = TimeUnit.MILLISECONDS.toMinutes(fromTimeMS)  % 60;

        long durationMS = toTimeMS - fromTimeMS;
        long msDuration = durationMS % 1000;
        long secsDuration = TimeUnit.MILLISECONDS.toSeconds(durationMS)  % 60;
        long hoursDuration = TimeUnit.MILLISECONDS.toHours(durationMS)  % 24;
        long minutesDuration = TimeUnit.MILLISECONDS.toMinutes(durationMS)  % 60;
        @SuppressLint("DefaultLocale") String command = String.format("-y -i \"%s\" -ss %d:%d:%d -t %d:%d:%d -vcodec copy -acodec copy \"%s\"",inputVideoPath,hoursFrom,minutesFrom,secsFrom
                ,hoursDuration,minutesDuration,secsDuration,outputVideoPath);
        RunCommandExecuteFFMPEG(command,false);
    }
    public static void addAudioFromVideoToVideo(String fromAudioVideo,String toAudioVideo,String pathResultVideo) throws InterruptedException {
        String command = String.format("-y -i  \"%s\" -i  \"%s\" -c copy -map 0:1 -map 1:0 -shortest  \"%s\"",fromAudioVideo,toAudioVideo,toAudioVideo);
        RunCommandExecuteFFMPEG(command,false);
    }
    public static void executeCommand(String inputVideoPath,String outputVideoPath,float bitrateInMbit,String framerate,long fromTimeMS,long toTimeMS,Codecs.CodecsName codec,String scaleResolution) throws Exception {

        String command = "";
        long msFrom = fromTimeMS % 1000;
        long secsFrom = TimeUnit.MILLISECONDS.toSeconds(fromTimeMS)  % 60;
        long hoursFrom = TimeUnit.MILLISECONDS.toHours(fromTimeMS)  % 24;
        long minutesFrom = TimeUnit.MILLISECONDS.toMinutes(fromTimeMS)  % 60;

        long durationMS = toTimeMS - fromTimeMS;
        long msDuration = durationMS % 1000;
        long secsDuration = TimeUnit.MILLISECONDS.toSeconds(durationMS)  % 60;
        long hoursDuration = TimeUnit.MILLISECONDS.toHours(durationMS)  % 24;
        long minutesDuration = TimeUnit.MILLISECONDS.toMinutes(durationMS)  % 60;
        String currentCodec = Codecs.toStringFFMPEGName(codec);
        String framerateNorm = framerate.replace(",",".");

        if(fromTimeMS == toTimeMS || fromTimeMS < 0 || fromTimeMS < 0)
            command = String.format("-y -i \"%s\" -c:v %s -c:a %s -b:v %fM -vf scale=%s -r %s -strict -2 \"%s\"",
                    inputVideoPath,currentCodec,"libopus",bitrateInMbit,scaleResolution,framerateNorm,outputVideoPath);
        else
            command = String.format("-y -i \"%s\" -ss %d:%d:%d -t %d:%d:%d -c:v %s -c:a %s -b:v %fM -vf scale=%s -r %s -strict -2 \"%s\"",
                    inputVideoPath,hoursFrom,minutesFrom,secsFrom,hoursDuration,minutesDuration,secsDuration,
                    currentCodec,"libopus",bitrateInMbit,scaleResolution,framerateNorm,outputVideoPath);
        RunCommandExecuteFFMPEG(command,false);
    }
    public static Bitmap getFrameFromVideo(String path, int frametimeMs)
    {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        return mediaMetadataRetriever.getFrameAtTime(frametimeMs * 1000,MediaMetadataRetriever.OPTION_CLOSEST);
    }
    public static Bitmap getFrameCollage(String path,int durationVideoMs,int widthFrameInPx,int heightFrameInPx,int countFrame)
    {
        Bitmap frameCollage = null;
        int timestep = durationVideoMs / countFrame;
        for(int i = 0,frametimeMs = 0;i < countFrame && frametimeMs <= durationVideoMs;
            i++,frametimeMs+=timestep)
        {
            Bitmap tempFrame = getFrameFromVideo(path,frametimeMs);
            tempFrame = scaleBitmap(tempFrame,widthFrameInPx,heightFrameInPx);

            if(frameCollage == null)
                frameCollage = tempFrame;
            else
                frameCollage = stickBitmap(frameCollage,tempFrame);
        }

        return frameCollage;
    }

    private static Bitmap stickBitmap(Bitmap bmp1, Bitmap bmp2)
    {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth() + bmp2.getWidth(),
                bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, bmp1.getWidth(), 0, null);
        return bmOverlay;
    }

    private static Bitmap scaleBitmap(Bitmap bmp, int scaleWidthInPx, int scaleHeightInPx)
    {
        return Bitmap.createScaledBitmap(bmp, scaleWidthInPx, scaleHeightInPx, false);
    }

    public static void extractFrames(String videopath,String outfile,long fromTimeMS,long toTimeMS,int countFrame) throws InterruptedException {
        String command = "";
        long msFrom = fromTimeMS % 1000;
        long secsFrom = TimeUnit.MILLISECONDS.toSeconds(fromTimeMS)  % 60;
        long hoursFrom = TimeUnit.MILLISECONDS.toHours(fromTimeMS)  % 24;
        long minutesFrom = TimeUnit.MILLISECONDS.toMinutes(fromTimeMS)  % 60;

        long durationMS = toTimeMS - fromTimeMS;
        if(durationMS < 0)
            durationMS = 0;
        long msDuration = durationMS % 1000;
        long secsDuration = TimeUnit.MILLISECONDS.toSeconds(durationMS)  % 60;
        long hoursDuration = TimeUnit.MILLISECONDS.toHours(durationMS)  % 24;
        long minutesDuration = TimeUnit.MILLISECONDS.toMinutes(durationMS)  % 60;
        if(countFrame > 0)
            command = String.format("-ss %d:%d:%d -t %d:%d:%d -i \"%s\" -frames:v %d \"%s\"",hoursFrom,
                minutesFrom,secsFrom, hoursDuration,minutesDuration,secsDuration,videopath,countFrame,outfile);
        else
            command = String.format("-ss %d:%d:%d -t %d:%d:%d -i \"%s\" \"%s\"",hoursFrom,
                    minutesFrom,secsFrom, hoursDuration,minutesDuration,secsDuration,videopath,outfile);


        RunCommandExecuteFFMPEG(command,false);
    }

}
