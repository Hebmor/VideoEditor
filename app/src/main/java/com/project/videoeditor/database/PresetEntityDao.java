package com.project.videoeditor.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PresetEntityDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(PresetEntity expansion);
    @Query("DELETE FROM presets")
    void deleteAll();
    @Query("SELECT * from presets")
    LiveData<List<PresetEntity>> getAllExpansion();
}
