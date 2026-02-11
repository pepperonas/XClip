package io.celox.xclip.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clipboard_history")
data class ClipboardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val timestamp: Long,
    @ColumnInfo(name = "is_pinned", defaultValue = "0")
    val isPinned: Boolean = false
)
