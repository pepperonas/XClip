package io.celox.xclip.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClipboardRepository {

    private final ClipboardDao clipboardDao;
    private final LiveData<List<ClipboardEntity>> allClipboards;
    private final ExecutorService executorService;

    public ClipboardRepository(Application application) {
        ClipboardDatabase database = ClipboardDatabase.getInstance(application);
        clipboardDao = database.clipboardDao();
        allClipboards = clipboardDao.getAllClipboards();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(ClipboardEntity clipboardEntity) {
        executorService.execute(() -> clipboardDao.insert(clipboardEntity));
    }

    public void delete(ClipboardEntity clipboardEntity) {
        executorService.execute(() -> clipboardDao.delete(clipboardEntity));
    }

    public LiveData<List<ClipboardEntity>> getAllClipboards() {
        return allClipboards;
    }

    public int getCount() {
        try {
            return executorService.submit(() -> clipboardDao.getCount()).get();
        } catch (Exception e) {
            return 0;
        }
    }

    public void deleteAll() {
        executorService.execute(() -> clipboardDao.deleteAll());
    }
}
