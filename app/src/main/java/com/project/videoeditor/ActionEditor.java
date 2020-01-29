package com.project.videoeditor;

import android.content.Context;

import com.arthenica.mobileffmpeg.FFmpeg;


public class ActionEditor {

    private static FFmpeg ffmpeg;
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
    public static String GenFrameCollage(String filePath, Context context)
    {
        String tempCachePath = context.getCacheDir() + "/tempCollage.png";
        String command = "-y -i \"" + filePath + "\" -frames:v 1 -q:v 1 -vsync vfr -vf \"select=not(mod(n\\,"+ (int)(videoInfo.getFrameCount() / 8)+")),scale=-1:120,tile=8x1\" "+tempCachePath;
        FFmpeg.execute(command);
        videoInfo.setPathFrameCollage(tempCachePath);
        return  tempCachePath;
    }

    public static void setVideoInfo(VideoInfo videoInfo) {
        ActionEditor.videoInfo = videoInfo;
    }
}
