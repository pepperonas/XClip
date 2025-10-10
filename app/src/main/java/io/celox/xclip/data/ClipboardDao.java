package io.celox.xclip.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ClipboardDao {

    @Insert
    void insert(ClipboardEntity clipboardEntity);

    @Delete
    void delete(ClipboardEntity clipboardEntity);

    @Query("SELECT * FROM clipboard_history ORDER BY timestamp DESC")
    LiveData<List<ClipboardEntity>> getAllClipboards();

    @Query("SELECT COUNT(*) FROM clipboard_history")
    int getCount();

    @Query("DELETE FROM clipboard_history")
    void deleteAll();
}
