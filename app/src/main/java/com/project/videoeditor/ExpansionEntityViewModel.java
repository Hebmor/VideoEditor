package com.project.videoeditor;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.project.videoeditor.support.ExpansionEntityRepository;

import java.util.List;

public class ExpansionEntityViewModel extends AndroidViewModel {

    private ExpansionEntityRepository mRepository;
    private LiveData<List<ExpansionEntity>> mAllWords;

    public ExpansionEntityViewModel(@NonNull Application application) {
        super(application);
        mRepository = new ExpansionEntityRepository(application);
        mAllWords = mRepository.getAllWords();
    }

    public LiveData<List<ExpansionEntity>> getAllWords() { return mAllWords; }

    public void insert(ExpansionEntity word) { mRepository.insert(word); }
}
