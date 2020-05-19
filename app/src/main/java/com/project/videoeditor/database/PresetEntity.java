package com.project.videoeditor.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "presets")
public class PresetEntity {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    private int id;
    @NonNull
    @ColumnInfo(name = "nameFormat")
    private String nameFormat;
    @NonNull
    @ColumnInfo(name = "pixelResolution")
    private String pixelResolution;
    @NonNull
    @ColumnInfo(name = "imageProportion")
    private String imageProportion;
    @NonNull
    @ColumnInfo(name = "CBR")
    private float CBR;
    @NonNull
    @ColumnInfo(name = "minimumBitrate")
    private float minimumBitrate;
    @NonNull
    @ColumnInfo(name = "maximumBitrate")
    private float maximumBitrate;

    public PresetEntity(@NonNull String nameFormat, @NonNull String pixelResolution, @NonNull String imageProportion,
                        @NonNull float CBR, @NonNull float minimumBitrate, @NonNull float maximumBitrate) {
        this.nameFormat = nameFormat;
        this.pixelResolution = pixelResolution;
        this.imageProportion = imageProportion;
        this.CBR = CBR;
        this.minimumBitrate = minimumBitrate;
        this.maximumBitrate = maximumBitrate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNameFormat() {
        return nameFormat;
    }

    public void setNameFormat(@NonNull String nameFormat) {
        this.nameFormat = nameFormat;
    }

    public String getPixelResolution() {
        return pixelResolution;
    }

    public void setPixelResolution(@NonNull String pixelResolution) {
        this.pixelResolution = pixelResolution;
    }

    public String getImageProportion() {
        return imageProportion;
    }

    public void setImageProportion(@NonNull String imageProportion) {
        this.imageProportion = imageProportion;
    }
    public float getCBR() {
        return CBR;
    }

    public void setCBR(float CBR) {
        this.CBR = CBR;
    }

    public float getMinimumBitrate() {
        return minimumBitrate;
    }

    public void setMinimumBitrate(float minimumBitrate) {
        this.minimumBitrate = minimumBitrate;
    }

    public float getMaximumBitrate() {
        return maximumBitrate;
    }

    public void setMaximumBitrate(float maximumBitrate) {
        this.maximumBitrate = maximumBitrate;
    }
}
