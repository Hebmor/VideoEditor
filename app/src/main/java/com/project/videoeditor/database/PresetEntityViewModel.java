package com.project.videoeditor.database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class PresetEntityViewModel extends AndroidViewModel {

    private PresetEntityRepository mRepository;
    private LiveData<List<PresetEntity>> mAllPreset;

    public PresetEntityViewModel(@NonNull Application application) {
        super(application);
        mRepository = new PresetEntityRepository(application);
        mAllPreset = mRepository.getAllExpansion();
    }

    public LiveData<List<PresetEntity>> getAllPreset() { return mAllPreset; }

    public void insert(PresetEntity word) { mRepository.insert(word);}
}
