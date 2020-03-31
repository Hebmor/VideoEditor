package com.project.videoeditor;

import android.os.Parcel;
import android.os.Parcelable;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.FFprobe;
import com.arthenica.mobileffmpeg.MediaInformation;

import java.io.File;

public class VideoInfo implements Parcelable {

    private Long startTime = 0L;
    private Long height  = 0L;
    private Long width  = 0L;
    private Long index = 0L;
    //private final Long sampleRate;
    private String frameRate;
    private String codec;
    private String format;
    private String aspectRatio;
    private Long duration = 0L;
    private Long bitrate = 0L;
    private String path;
    private Long frameCount = 0L;
    private FFmpeg ffmpeg;
    private String pathFrameCollage;



    public VideoInfo(String path) {
        MediaInformation info = FFprobe.getMediaInformation(path);
        this.path = path;
        this.bitrate = info.getBitrate();
        this.duration = info.getDuration();
        this.startTime = info.getStartTime();
        this.height = info.getStreams().get(0).getHeight();
        this.width = info.getStreams().get(0).getWidth();
        this.index = info.getStreams().get(0).getIndex();
        //this.sampleRate = info.getStreams().get(0).getSampleRate();
        this.frameRate = info.getStreams().get(0).getAverageFrameRate();
        this.codec = info.getStreams().get(0).getCodec();
        this.format = info.getStreams().get(0).getFormat();
        this.aspectRatio = info.getStreams().get(0).getDisplayAspectRatio();
        //Приблизительное количество кадров
        this.frameCount = (long)((( this.duration / 1000) % 60) * Float.valueOf(frameRate));

    }

    public VideoInfo(Parcel parcel) {

        this.startTime = parcel.readLong();
        this.height = parcel.readLong();
        this.width = parcel.readLong();
        this.index = parcel.readLong();
        //this.sampleRate = parcel.readLong();
        this.duration = parcel.readLong();
        this.bitrate = parcel.readLong();

        this.frameRate = parcel.readString();
        this.codec = parcel.readString();
        this.format = parcel.readString();
        this.aspectRatio = parcel.readString();
        this.path = parcel.readString();
    }

    public void GeneratePreviewFrames()
    {

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

   /* public Long getSampleRate() {
        return sampleRate;
    }*/

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeLong(startTime);
        dest.writeLong(height);
        dest.writeLong(width);
        dest.writeLong(index);
       // dest.writeLong(sampleRate);
        dest.writeLong(duration);
        dest.writeLong(bitrate);
        dest.writeString(frameRate);
        dest.writeString(codec);
        dest.writeString(format);
        dest.writeString(aspectRatio);
        dest.writeString(path);

    }

    public static final Parcelable.Creator<VideoInfo> CREATOR = new Parcelable.Creator<VideoInfo>() {
        // распаковываем объект из Parcel
        public VideoInfo createFromParcel(Parcel in) {
            return new VideoInfo(in);
        }

        @Override
        public VideoInfo[] newArray(int size) {
            return new VideoInfo[size];
        }

    };


    public String getPathFrameCollage() {
        return pathFrameCollage;
    }

    public void setPathFrameCollage(String frameCollage) {
        this.pathFrameCollage = frameCollage;
    }

    public Long getFrameCount() {
        return frameCount;
    }
    public void DeleteFrameCollage()
    {
        if(this.pathFrameCollage != null) {
            File frameCollage = new File(this.pathFrameCollage);
            frameCollage.delete();
        }
    }
}
