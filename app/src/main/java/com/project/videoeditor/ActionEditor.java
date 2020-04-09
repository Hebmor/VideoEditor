package com.project.videoeditor;

import android.app.Activity;
import android.content.Context;
import android.widget.ArrayAdapter;

import com.arthenica.mobileffmpeg.FFmpeg;

import java.util.Objects;


public class ActionEditor {

    private static FFmpeg ffmpeg;
    private static VideoInfo videoInfo;
    public final String availableEncodeList[] = {"MPEG4","libx264","H.265","libtheora","mpeg2","libxvid"};

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
        RunCommandExecuteFFMPEG(command);
    }
    public static void EncodeLIBX264(String filePath,String new_filePath,Long bitrate,int framerate,String preset,String tune,int crf) throws Exception {
        if(crf < 0 && crf > 100)
            throw new IllegalArgumentException("Ошибка, параметр crf должен быть в диапазоне от 0 до 100");
        String command = "-y -i \"" + filePath + "\" -c:v libx264 -crf "+crf+" -preset "+preset+" -tune "+tune+ " -b:v "+bitrate+"k  -slices 4 -r"+framerate+" "+new_filePath;
        RunCommandExecuteFFMPEG(command);
    }
    public static void EncodeH265(String filePath,String new_filePath,String bitrate,String framerate,String preset,String tune,int crf) throws Exception {
        if(crf < 0 && crf > 100)
            throw new IllegalArgumentException("Ошибка, параметр crf должен быть в диапазоне от 0 до 100");
        String command = "-y -i \"" + filePath + "\" -c:v libx265 -crf "+crf+" -preset "+preset+" -tune "+tune+" -b:v "+bitrate+"k -slices 4 -r "+framerate+" "+new_filePath;
        RunCommandExecuteFFMPEG(command);
    }
    public static String GenFrameCollage(String filePath, Context context)
    {
        String tempCachePath = context.getCacheDir() + "/tempCollage.png";
        String command = "-y -i \"" + filePath + "\" -frames:v 1 -q:v 1 -vsync vfr -vf \"select=not(mod(n\\,"+ (int)(videoInfo.getFrameCount() / 8)+")),scale=-1:120,tile=8x1\" "+tempCachePath;
        FFmpeg.execute(command);
        videoInfo.setPathFrameCollage(tempCachePath);
        return  tempCachePath;
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
                return "stillimage";
            case 4:
                return "fastdecode";
            case 5:
                return "zerolatency";
        }
        return "NoValue";
    }
    public static void setVideoInfo(VideoInfo videoInfo) {
        ActionEditor.videoInfo = videoInfo;
    }
    private static void RunCommandExecuteFFMPEG(String command)
    {
        Thread ffmpegExecuteThread = new Thread(new Runnable() {
            @Override
            public void run() {
                FFmpeg.execute(command);
            }
        });
        ffmpegExecuteThread.start();
    }
}
