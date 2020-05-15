package com.project.videoeditor.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ExpansionEntityDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(ExpansionEntity expansion);

    @Query("DELETE FROM expansion")
    void deleteAll();

    @Query("SELECT * from expansion ORDER BY nameFormat ASC")
    LiveData<List<ExpansionEntity>> getAlphabetizedWords();
}
