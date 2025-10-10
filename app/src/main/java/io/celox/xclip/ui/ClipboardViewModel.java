package io.celox.xclip.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import io.celox.xclip.data.ClipboardEntity;
import io.celox.xclip.data.ClipboardRepository;

public class ClipboardViewModel extends AndroidViewModel {

    private final ClipboardRepository repository;
    private final LiveData<List<ClipboardEntity>> allClipboards;

    public ClipboardViewModel(@NonNull Application application) {
        super(application);
        repository = new ClipboardRepository(application);
        allClipboards = repository.getAllClipboards();
    }

    public void insert(ClipboardEntity clipboardEntity) {
        repository.insert(clipboardEntity);
    }

    public void delete(ClipboardEntity clipboardEntity) {
        repository.delete(clipboardEntity);
    }

    public LiveData<List<ClipboardEntity>> getAllClipboards() {
        return allClipboards;
    }

    public int getCount() {
        return repository.getCount();
    }
}
