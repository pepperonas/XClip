package io.celox.xclip.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ClipboardEntity.class}, version = 1, exportSchema = false)
public abstract class ClipboardDatabase extends RoomDatabase {

    private static ClipboardDatabase instance;

    public abstract ClipboardDao clipboardDao();

    public static synchronized ClipboardDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            ClipboardDatabase.class, "clipboard_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
