package com.project.videoeditor.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "expansion")
public class ExpansionEntity {

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

    public ExpansionEntity(@NonNull String nameFormat, @NonNull String pixelResolution, @NonNull String imageProportion) {
        this.nameFormat = nameFormat;
        this.pixelResolution = pixelResolution;
        this.imageProportion = imageProportion;
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
}
