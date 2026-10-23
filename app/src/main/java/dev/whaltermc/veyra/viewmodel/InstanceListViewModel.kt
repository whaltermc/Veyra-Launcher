package dev.whaltermc.veyra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.whaltermc.veyra.VeyraApplication
import dev.whaltermc.veyra.data.model.InstanceEntity
import dev.whaltermc.veyra.data.model.RenderBackend
import dev.whaltermc.veyra.data.repository.AppSettingsRepository
import dev.whaltermc.veyra.data.repository.InstanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class InstanceListViewModel(
    private val repository: InstanceRepository,
    private val settings: AppSettingsRepository
) : ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText: MutableStateFlow<String> = _searchText

    private val _sortBy = MutableStateFlow(SortBy.LAST_PLAYED)
    val sortBy = _sortBy

    private val _isGridView = MutableStateFlow(true)
    val isGridView = _isGridView

    val filteredInstances = combine(
        repository.getAllInstances(),
        _searchText.distinctUntilChanged(),
        _sortBy.distinctUntilChanged()
    ) { instances, query, sort ->
        instances
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) || it.gameVersion.contains(query, ignoreCase = true) }
            .sortedWith(sort.comparator)
    }

    fun toggleView() {
        _isGridView.value = !_isGridView.value
        viewModelScope.launch { settings.setInstanceListGridView(_isGridView.value) }
    }

    fun cycleSort() {
        val current = _sortBy.value
        val values = SortBy.values()
        val nextIndex = (current.ordinal + 1) % values.size
        _sortBy.value = values[nextIndex]
    }

    fun setSortBy(sort: SortBy) {
        _sortBy.value = sort
    }

    suspend fun deleteInstance(instanceId: String, wipeSaves: Boolean) {
        repository.deleteInstance(instanceId)
        if (wipeSaves) {
            // TODO: Delete instance directory
        }
    }

    suspend fun exportInstance(instanceId: String) {
        // TODO: Implement export
    }

    fun preWarmInstance(instanceId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // TODO: Pre-warm instance
        }
    }

    companion object {
        class Factory(private val app: VeyraApplication) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return InstanceListViewModel(app.instanceRepository, app.accountRepository as? AppSettingsRepository ?: AppSettingsRepository(app)) as T
            }
        }
    }
}

enum class SortBy(val label: String) {
    NAME("Name") {
        override val comparator: Comparator<InstanceEntity> = Comparator { a, b -> a.name.compareTo(b.name, ignoreCase = true) }
    },
    LAST_PLAYED("Last Played") {
        override val comparator: Comparator<InstanceEntity> = Comparator { a, b ->
            val aTime = a.lastPlayedAt?.toEpochMilliseconds() ?: Long.MIN_VALUE
            val bTime = b.lastPlayedAt?.toEpochMilliseconds() ?: Long.MIN_VALUE
            bTime.compareTo(aTime) // Descending
        }
    },
    PLAY_TIME("Play Time") {
        override val comparator: Comparator<InstanceEntity> = Comparator { a, b ->
            b.totalPlayTimeSeconds.compareTo(a.totalPlayTimeSeconds) // Descending
        }
    },
    DATE_CREATED("Date Created") {
        override val comparator: Comparator<InstanceEntity> = Comparator { a, b ->
            b.createdAt.toEpochMilliseconds().compareTo(a.createdAt.toEpochMilliseconds()) // Descending
        }
    };

    abstract val comparator: Comparator<InstanceEntity>
}