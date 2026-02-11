package io.celox.xclip.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.celox.xclip.data.ClipboardEntity
import io.celox.xclip.data.ClipboardRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ClipboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ClipboardRepository(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val clipboards: StateFlow<List<ClipboardEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllClipboards()
            } else {
                repository.searchClipboards(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _recentlyDeleted = MutableStateFlow<ClipboardEntity?>(null)
    val recentlyDeleted: StateFlow<ClipboardEntity?> = _recentlyDeleted.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun deleteEntry(entity: ClipboardEntity) {
        viewModelScope.launch {
            _recentlyDeleted.value = entity
            repository.delete(entity)
        }
    }

    fun undoDelete() {
        val entity = _recentlyDeleted.value ?: return
        viewModelScope.launch {
            repository.insert(entity)
            _recentlyDeleted.value = null
        }
    }

    fun clearRecentlyDeleted() {
        _recentlyDeleted.value = null
    }

    fun togglePin(entity: ClipboardEntity) {
        viewModelScope.launch {
            repository.setPinned(entity.id, !entity.isPinned)
        }
    }

    fun insert(entity: ClipboardEntity) {
        viewModelScope.launch {
            repository.insert(entity)
        }
    }
}
