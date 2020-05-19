package com.project.videoeditor.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class PresetEntityRepository {

    private PresetEntityDao mExpansionDao;
    private LiveData<List<PresetEntity>> mAllExpansion;

    public PresetEntityRepository(Application application) {
        PresetEntityRoomDatabase db = PresetEntityRoomDatabase.getDatabase(application);
        mExpansionDao = db.presetDao();
        mAllExpansion = mExpansionDao.getAllExpansion();
    }

    public LiveData<List<PresetEntity>> getAllExpansion() {
        return mAllExpansion;
    }

    public void insert(PresetEntity word) {
        PresetEntityRoomDatabase.databaseWriteExecutor.execute(() -> {
            mExpansionDao.insert(word);
        });
    }
}
