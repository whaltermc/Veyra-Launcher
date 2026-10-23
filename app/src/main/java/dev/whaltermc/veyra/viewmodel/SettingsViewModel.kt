package dev.whaltermc.veyra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.whaltermc.veyra.VeyraApplication
import dev.whaltermc.veyra.data.model.RenderBackend
import dev.whaltermc.veyra.data.repository.AppSettingsRepository
import dev.whaltermc.veyra.render.RenderBackendManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settings: AppSettingsRepository,
    private val renderBackendManager: RenderBackendManager
) : ViewModel() {

    private val _defaultBackend = MutableStateFlow<RenderBackend?>(null)
    val defaultBackend = _defaultBackend.asStateFlow()

    private val _debugOverlayDefault = MutableStateFlow<Boolean?>(null)
    val debugOverlayDefault = _debugOverlayDefault.asStateFlow()

    private val _downloadThrottle = MutableStateFlow<Boolean?>(null)
    val downloadThrottle = _downloadThrottle.asStateFlow()

    private val _pauseMobileData = MutableStateFlow<Boolean?>(null)
    val pauseMobileData = _pauseMobileData.asStateFlow()

    private val _defaultRenderDistance = MutableStateFlow<Int?>(null)
    val defaultRenderDistance = _defaultRenderDistance.asStateFlow()

    private val _physicalInputAutoSwitch = MutableStateFlow<Boolean?>(null)
    val physicalInputAutoSwitch = _physicalInputAutoSwitch.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            _defaultBackend.value = settings.getRenderDefaultBackend().await()
            _debugOverlayDefault.value = settings.getDebugOverlayDefault().await()
            // Load other settings...
        }
    }

    fun setDefaultBackend(backend: RenderBackend) {
        _defaultBackend.value = backend
        viewModelScope.launch(Dispatchers.IO) {
            settings.setRenderDefaultBackend(backend)
        }
    }

    fun setDebugOverlayDefault(enabled: Boolean) {
        _debugOverlayDefault.value = enabled
        viewModelScope.launch(Dispatchers.IO) {
            settings.setDebugOverlayDefault(enabled)
        }
    }

    fun setDownloadThrottle(enabled: Boolean) {
        _downloadThrottle.value = enabled
        // TODO: Persist
    }

    fun setPauseMobileData(enabled: Boolean) {
        _pauseMobileData.value = enabled
        // TODO: Persist
    }

    fun setDefaultRenderDistance(distance: Int) {
        _defaultRenderDistance.value = distance
        // TODO: Persist
    }

    fun setPhysicalInputAutoSwitch(enabled: Boolean) {
        _physicalInputAutoSwitch.value = enabled
        // TODO: Persist
    }

    suspend fun runBenchmark() {
        viewModelScope.launch(Dispatchers.IO) {
            renderBackendManager.runBenchmark(renderBackendManager.getGpuHash()).await()
        }
    }

    suspend fun clearPipelineCache() {
        // TODO: Clear pipeline cache
    }

    suspend fun clearUnusedAssets() {
        // TODO: Clear unused assets
    }

    suspend fun clearLogs() {
        // TODO: Clear logs
    }

    suspend fun exportDiagnosticBundle() {
        // TODO: Export diagnostic bundle
    }

    companion object {
        class Factory(private val app: VeyraApplication) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(
                    app.accountRepository as? AppSettingsRepository ?: AppSettingsRepository(app),
                    app.renderBackendManager
                ) as T
            }
        }
    }
}