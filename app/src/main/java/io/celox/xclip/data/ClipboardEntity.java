package io.celox.xclip.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "clipboard_history")
public class ClipboardEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String text;

    private long timestamp;

    public ClipboardEntity(String text, long timestamp) {
        this.text = text;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
