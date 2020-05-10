package com.project.videoeditor;

import android.os.Parcel;
import android.os.Parcelable;

import com.arthenica.mobileffmpeg.FFprobe;
import com.arthenica.mobileffmpeg.MediaInformation;
import com.arthenica.mobileffmpeg.StreamInformation;

import java.io.File;

public class VideoInfo implements Parcelable {

    private String path;
    private Long bitrate;
    private Long startTime;
    private Long duration;
    private Long width;
    private Long height;
    private String format;
    private Long index;
    private String frameRate;
    private String codec;
    private String aspectRatio;
    private long frameCount;
    private String pathFrameCollage;
    private String extension;
    private String filename;
    private Long sizeInBytes;


    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
    public Long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(Long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }



    public VideoInfo()
    {

    }

    public VideoInfo(String path)
    {
        parseInfoFromPath(path);

    }

    public void parseInfoFromPath(String path)
    {
        StreamInformation mainVideoStream = null;
        StreamInformation mainAudioStream;
        MediaInformation info = FFprobe.getMediaInformation(path);
        for(StreamInformation stream : info.getStreams())
        {
            if(stream.getType() == "video")
                mainVideoStream = stream;
            if(stream.getType() == "audio")
                mainAudioStream = stream;
        }
        this.duration = info.getDuration();
        this.startTime = info.getStartTime();
        //this.extension = path.substring(path.lastIndexOf("."));

        if(mainVideoStream != null) {
            this.path = path;
            this.bitrate = info.getBitrate();
            if(this.bitrate == null)
                this.bitrate = computeBitrate();

            this.height = mainVideoStream.getHeight();
            this.width = mainVideoStream.getWidth();
            this.index = mainVideoStream.getIndex();
            //this.sampleRate = info.getStreams().get(0).getSampleRate();
            this.frameRate = mainVideoStream.getAverageFrameRate();
            this.codec = mainVideoStream.getCodec();
            this.format = mainVideoStream.getFormat();
            this.aspectRatio = mainVideoStream.getDisplayAspectRatio();
            //Приблизительное количество кадров
            this.frameCount = (long) (((this.duration / 1000) % 60) * Float.parseFloat(frameRate));
        }
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
        this.extension = parcel.readString();
        //this.sizeInBytes = parcel.readLong();
        this.filename = parcel.readString();
    }
    private Long computeBitrate()
    {
        if(this.duration != null && this.sizeInBytes != null)
        {
            return (sizeInBytes / 1024) / (duration / 1000);
        }
        return null;
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
        dest.writeString(extension);
        //dest.writeLong(sizeInBytes);
        dest.writeString(filename);
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

    public String getExtension() {
        return extension;
    }
}
