package dev.whaltermc.veyra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.whaltermc.veyra.VeyraApplication
import dev.whaltermc.veyra.data.model.InstanceEntity
import dev.whaltermc.veyra.data.model.ModloaderType
import dev.whaltermc.veyra.data.model.RenderBackend
import dev.whaltermc.veyra.data.repository.InstanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

class NewInstanceViewModel(
    private val repository: InstanceRepository,
    private val instanceId: String?
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name: MutableStateFlow<String> = _name

    private val _selectedVersion = MutableStateFlow<String?>(null)
    val selectedVersion: MutableStateFlow<String?> = _selectedVersion

    private val _selectedModloader = MutableStateFlow(ModloaderType.VANILLA)
    val selectedModloader: MutableStateFlow<ModloaderType> = _selectedModloader

    private val _selectedModloaderVersion = MutableStateFlow<String?>(null)
    val selectedModloaderVersion: MutableStateFlow<String?> = _selectedModloaderVersion

    private val _ramMin = MutableStateFlow(2048)
    val ramMin: MutableStateFlow<Int> = _ramMin

    private val _ramMax = MutableStateFlow(4096)
    val ramMax: MutableStateFlow<Int> = _ramMax

    private val _selectedBackend = MutableStateFlow(RenderBackend.AUTO)
    val selectedBackend: MutableStateFlow<RenderBackend> = _selectedBackend

    private val _advancedArgs = MutableStateFlow("")
    val advancedArgs: MutableStateFlow<String> = _advancedArgs

    private val _iconPath = MutableStateFlow<String?>(null)
    val iconPath: MutableStateFlow<String?> = _iconPath

    private val _javaOverride = MutableStateFlow<Int?>(null)
    val javaOverride: MutableStateFlow<Int?> = _javaOverride

    val isLoadingVersions = MutableStateFlow(false)
    val versions = MutableStateFlow<List<VersionInfo>>(emptyList())

    val isLoadingModloaderVersions = MutableStateFlow(false)
    val modloaderVersions = MutableStateFlow<List<String>>(emptyList())

    val requiredJavaVersion = MutableStateFlow(21)
    val maxRamMb = MutableStateFlow(4096)

    val generatedArgs = MutableStateFlow("")

    init {
        loadVersions()
        if (instanceId != null) {
            loadExistingInstance(instanceId)
        } else {
            updateGeneratedArgs()
        }
        updateMaxRam()
    }

    private fun loadExistingInstance(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getInstanceById(id)?.let { instance ->
                _name.value = instance.name
                _selectedVersion.value = instance.gameVersion
                _selectedModloader.value = instance.modloaderType
                _selectedModloaderVersion.value = instance.modloaderVersion
                _ramMin.value = instance.ramMinMb
                _ramMax.value = instance.ramMaxMb
                _selectedBackend.value = instance.renderBackendOverride
                _advancedArgs.value = instance.advancedJvmArgs ?: ""
                _iconPath.value = instance.iconPath
                _javaOverride.value = instance.javaMajorOverride
                loadModloaderVersions(instance.gameVersion, instance.modloaderType)
                updateGeneratedArgs()
            }
        }
    }

    private fun loadVersions() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoadingVersions.value = true
            try {
                // TODO: Fetch from Mojang's version manifest
                // For now, mock data
                _versions.value = listOf(
                    VersionInfo("1.21.1", VersionType.RELEASE, "2024-08-08"),
                    VersionInfo("1.21", VersionType.RELEASE, "2024-06-13"),
                    VersionInfo("1.20.6", VersionType.RELEASE, "2024-04-17"),
                    VersionInfo("1.20.4", VersionType.RELEASE, "2023-12-07"),
                    VersionInfo("1.20.1", VersionType.RELEASE, "2023-06-12"),
                    VersionInfo("24w33a", VersionType.SNAPSHOT, "2024-08-14"),
                    VersionInfo("24w32a", VersionType.SNAPSHOT, "2024-08-07")
                )
            } finally {
                isLoadingVersions.value = false
            }
        }
    }

    fun selectVersion(version: String) {
        _selectedVersion.value = version
        _selectedModloaderVersion.value = null
        loadModloaderVersions(version, _selectedModloader.value)
        updateRequiredJava(version, _selectedModloader.value)
        updateGeneratedArgs()
    }

    private fun loadModloaderVersions(gameVersion: String, modloader: ModloaderType) {
        if (modloader == ModloaderType.VANILLA) {
            _modloaderVersions.value = emptyList()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            isLoadingModloaderVersions.value = true
            try {
                // TODO: Fetch from modloader's metadata API
                // Mock data for now
                _modloaderVersions.value = when (modloader) {
                    ModloaderType.FABRIC -> listOf("0.16.10", "0.16.9", "0.16.8")
                    ModloaderType.QUILT -> listOf("0.22.0", "0.21.0")
                    ModloaderType.FORGE -> listOf("47.2.0", "47.1.0")
                    ModloaderType.NEOFORGE -> listOf("21.1.0", "21.0.0")
                    else -> emptyList()
                }
            } finally {
                isLoadingModloaderVersions.value = false
            }
        }
    }

    private fun updateRequiredJava(gameVersion: String, modloader: ModloaderType) {
        // Java version resolution logic
        val major = when {
            gameVersion.startsWith("1.20.5") || gameVersion.startsWith("1.21") -> 21
            gameVersion.startsWith("1.18") || gameVersion.startsWith("1.19") || gameVersion.startsWith("1.20") -> 17
            gameVersion.startsWith("1.17") -> 16
            else -> 8
        }
        _requiredJavaVersion.value = major
    }

    private fun updateMaxRam() {
        // TODO: Query ActivityManager.getMemoryInfo()
        _maxRamMb.value = 4096 // placeholder
    }

    private fun updateGeneratedArgs() {
        // TODO: Generate actual JVM args
        _generatedArgs.value = "-Xms${_ramMin.value}M -Xmx${_ramMax.value}M -XX:+UseG1GC"
    }

    suspend fun saveInstance(): Result<Unit> {
        return kotlinx.coroutines.CoroutineScope(Dispatchers.IO).async {
            try {
                val id = instanceId ?: UUID.randomUUID().toString()
                val instance = InstanceEntity(
                    id = id,
                    name = _name.value,
                    iconPath = _iconPath.value,
                    gameVersion = _selectedVersion.value ?: "1.21.1",
                    modloaderType = _selectedModloader.value,
                    modloaderVersion = _selectedModloaderVersion.value,
                    javaMajorOverride = _javaOverride.value,
                    ramMinMb = _ramMin.value,
                    ramMaxMb = _ramMax.value,
                    renderBackendOverride = _selectedBackend.value,
                    advancedJvmArgs = _advancedArgs.value.ifBlank { null },
                    createdAt = if (instanceId != null) repository.getInstanceById(id)?.createdAt ?: Instant.now() else Instant.now()
                )
                repository.insertInstance(instance)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }.await()
    }

    companion object {
        class Factory(
            private val app: VeyraApplication,
            private val instanceId: String?
        ) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NewInstanceViewModel(app.instanceRepository, instanceId) as T
            }
        }
    }
}

data class VersionInfo(
    val id: String,
    val type: VersionType,
    val releaseDate: String
)

enum class VersionType(val label: String, val ordinal: Int) {
    RELEASE("Release", 0),
    SNAPSHOT("Snapshot", 1),
    OLD_BETA("Old Beta", 2),
    OLD_ALPHA("Old Alpha", 3)
}

enum class RenderBackend {
    AUTO,
    PANVK_NATIVE,
    ZINK_PANVK,
    RUSTGL,
    GL4ES;

    val label: String get() = when (this) {
        AUTO -> "Auto"
        PANVK_NATIVE -> "PanVK"
        ZINK_PANVK -> "Zink+PanVK"
        RUSTGL -> "RustGL"
        GL4ES -> "GL4ES"
    }
}