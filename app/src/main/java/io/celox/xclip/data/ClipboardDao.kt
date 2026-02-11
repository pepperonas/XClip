package io.celox.xclip.data

import kotlinx.coroutines.flow.Flow
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ClipboardDao {

    @Insert
    fun insertSync(entity: ClipboardEntity)

    @Insert
    suspend fun insert(entity: ClipboardEntity)

    @Delete
    suspend fun delete(entity: ClipboardEntity)

    @Query("SELECT * FROM clipboard_history ORDER BY is_pinned DESC, timestamp DESC")
    fun getAllClipboards(): Flow<List<ClipboardEntity>>

    @Query("SELECT * FROM clipboard_history WHERE text LIKE '%' || :query || '%' ORDER BY is_pinned DESC, timestamp DESC")
    fun searchClipboards(query: String): Flow<List<ClipboardEntity>>

    @Query("SELECT * FROM clipboard_history WHERE id = :id")
    suspend fun getById(id: Long): ClipboardEntity?

    @Query("UPDATE clipboard_history SET is_pinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean)

    @Query("SELECT COUNT(*) FROM clipboard_history")
    fun getCountInternal(): Int

    @Query("DELETE FROM clipboard_history")
    suspend fun deleteAll()

    @Query("DELETE FROM clipboard_history WHERE is_pinned = 0")
    suspend fun deleteAllUnpinned()
}
