package com.project.videoeditor;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.MediaInformation;

public class VideoInfo {

    private final Long startTime;
    private final Long height;
    private final Long width;
    private final Long index;
    private final Long sampleRate;
    private final String frameRate;
    private final String codec;
    private final String format;
    private final String aspectRatio;
    private Long duration;
    private Long bitrate;
    private String path;
    private FFmpeg ffmpeg;


    public VideoInfo(String path)
    {
        MediaInformation info = ffmpeg.getMediaInformation(path);
        this.path = path;
        this.bitrate = info.getBitrate();
        this.duration = info.getDuration();
        this.startTime = info.getStartTime();
        this.height = info.getStreams().get(0).getHeight();
        this.width = info.getStreams().get(0).getWidth();
        this.index = info.getStreams().get(0).getIndex();
        this.sampleRate = info.getStreams().get(0).getSampleRate();
        this.frameRate = info.getStreams().get(0).getAverageFrameRate();
        this.codec = info.getStreams().get(0).getCodec();
        this.format = info.getStreams().get(0).getFormat();
        this.aspectRatio = info.getStreams().get(0).getDisplayAspectRatio();

    }

    public Long getStartTime() {
        return startTime;
    }

    public Long getHeight() {
        return height;
    }

    public Long getWidth() {
        return width;
    }

    public Long getIndex() {
        return index;
    }

    public Long getSampleRate() {
        return sampleRate;
    }

    public String getFrameRate() {
        return frameRate;
    }

    public String getCodec() {
        return codec;
    }

    public String getFormat() {
        return format;
    }

    public String getAspectRatio() {
        return aspectRatio;
    }

    public Long getDuration() {
        return duration;
    }

    public Long getBitrate() {
        return bitrate;
    }

    public String getPath() {
        return path;
    }
}
