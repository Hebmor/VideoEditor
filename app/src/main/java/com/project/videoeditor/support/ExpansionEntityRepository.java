package com.project.videoeditor.support;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.project.videoeditor.ExpansionEntity;
import com.project.videoeditor.ExpansionEntityDao;
import com.project.videoeditor.ExpansionEntityRoomDatabase;

import java.util.List;

public class ExpansionEntityRepository {

    private ExpansionEntityDao mExpansionDao;
    private LiveData<List<ExpansionEntity>> mAllExpansion;

    public ExpansionEntityRepository(Application application) {
        ExpansionEntityRoomDatabase db = ExpansionEntityRoomDatabase.getDatabase(application);
        mExpansionDao = db.expansionDao();
        mAllExpansion = mExpansionDao.getAlphabetizedWords();
    }

    public LiveData<List<ExpansionEntity>> getAllWords() {
        return mAllExpansion;
    }

    public void insert(ExpansionEntity word) {
        ExpansionEntityRoomDatabase.databaseWriteExecutor.execute(() -> {
            mExpansionDao.insert(word);
        });
    }
}
