package io.celox.xclip.data

import android.app.Application
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.Executors

class ClipboardRepository(application: Application) {

    private val clipboardDao: ClipboardDao
    private val executor = Executors.newSingleThreadExecutor()

    init {
        val database = ClipboardDatabase.getInstance(application)
        clipboardDao = database.clipboardDao()
    }

    fun getAllClipboards(): Flow<List<ClipboardEntity>> = clipboardDao.getAllClipboards()

    fun searchClipboards(query: String): Flow<List<ClipboardEntity>> = clipboardDao.searchClipboards(query)

    suspend fun insert(entity: ClipboardEntity) = clipboardDao.insert(entity)

    suspend fun delete(entity: ClipboardEntity) = clipboardDao.delete(entity)

    suspend fun getById(id: Long): ClipboardEntity? = clipboardDao.getById(id)

    suspend fun setPinned(id: Long, pinned: Boolean) = clipboardDao.setPinned(id, pinned)

    suspend fun deleteAll() = clipboardDao.deleteAll()

    suspend fun deleteAllUnpinned() = clipboardDao.deleteAllUnpinned()

    // Synchronous methods for ClipboardService (runs on background thread)
    fun insertSync(entity: ClipboardEntity) {
        executor.execute { clipboardDao.insertSync(entity) }
    }

    fun getCount(): Int {
        return try {
            executor.submit<Int> { clipboardDao.getCountInternal() }.get()
        } catch (e: Exception) {
            0
        }
    }
}
