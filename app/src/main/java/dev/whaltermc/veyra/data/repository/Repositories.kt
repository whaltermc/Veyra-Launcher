package dev.whaltermc.veyra.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.room.RoomDatabase
import com.google.common.util.concurrent.ListenableFuture
import dev.whaltermc.veyra.data.db.Daos
import dev.whaltermc.veyra.data.model.AccountEntity
import dev.whaltermc.veyra.data.model.InstanceEntity
import dev.whaltermc.veyra.data.model.ModEntity
import dev.whaltermc.veyra.data.model.ResourcePackEntity
import dev.whaltermc.veyra.data.model.ShaderPackEntity
import dev.whaltermc.veyra.data.model.WorldEntity
import dev.whaltermc.veyra.data.model.ControlLayoutEntity
import dev.whaltermc.veyra.data.model.ScreenshotEntity
import dev.whaltermc.veyra.data.model.BenchmarkResultEntity
import dev.whaltermc.veyra.data.model.AuthType
import dev.whaltermc.veyra.data.model.ModloaderType
import dev.whaltermc.veyra.data.model.RenderBackend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UTC
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

// ============ Repository Base ============

abstract class BaseRepository<T>(protected val dao: T, protected val context: Context) {
    protected val ioScope = CoroutineScope(Dispatchers.IO)
    protected val mainScope = CoroutineScope(Dispatchers.Main)
}

// ============ Account Repository ============

class AccountRepository(
    private val dao: Daos.AccountDao,
    context: Context
) : BaseRepository(dao, context) {

    fun getAllAccounts(): Flow<List<AccountEntity>> = dao.getAllAccounts()

    suspend fun getAccountById(id: String): AccountEntity? = dao.getAccountById(id)

    suspend fun insertAccount(account: AccountEntity) = dao.insert(account)

    suspend fun updateAccount(account: AccountEntity) = dao.update(account)

    suspend fun deleteAccount(id: String) {
        dao.getAccountById(id)?.let { dao.delete(it) }
    }

    suspend fun updateLastUsed(id: String) {
        dao.getAccountById(id)?.let { account ->
            dao.update(account.copy(lastUsed = Instant.now()))
        }
    }

    fun generateOfflineUuid(username: String): String {
        // Deterministic UUID from username (same as PojavLauncher/other launchers)
        val namespace = UUID.fromString("00000000-0000-0000-0000-000000000000")
        return UUID.nameUUIDFromBytes(("OfflinePlayer:$username").toByteArray()).toString()
    }
}

// ============ Instance Repository ============

class InstanceRepository(
    private val dao: Daos.InstanceDao,
    context: Context
) : BaseRepository(dao, context) {

    fun getAllInstances(): Flow<List<InstanceEntity>> = dao.getAllInstances()

    fun getInstanceFlow(id: String): Flow<InstanceEntity?> = dao.getInstanceByIdFlow(id)

    suspend fun getInstanceById(id: String): InstanceEntity? = dao.getInstanceById(id)

    suspend fun insertInstance(instance: InstanceEntity) = dao.insert(instance)

    suspend fun updateInstance(instance: InstanceEntity) = dao.update(instance)

    suspend fun deleteInstance(id: String) = dao.deleteById(id)

    suspend fun updateLastPlayed(id: String, playTimeSeconds: Long) {
        dao.getInstanceById(id)?.let { instance ->
            val now = Instant.now()
            dao.update(instance.copy(
                lastPlayedAt = now,
                totalPlayTimeSeconds = instance.totalPlayTimeSeconds + playTimeSeconds
            ))
        }
    }

    suspend fun togglePin(id: String) {
        dao.getInstanceById(id)?.let { instance ->
            dao.update(instance.copy(isPinned = !instance.isPinned))
        }
    }

    fun generateInstanceId(): String = UUID.randomUUID().toString()

    fun getInstanceDir(instanceId: String): File = File(context.filesDir, "instances/$instanceId")

    fun getSharedCachesDir(): File = File(context.filesDir, "caches")

    fun getJreDir(major: Int): File = File(getSharedCachesDir(), "jre/$major")

    fun getLibrariesDir(): File = File(getSharedCachesDir(), "libraries")

    fun getAssetsDir(): File = File(getSharedCachesDir(), "assets")

    fun getNativesDir(backend: String): File = File(getSharedCachesDir(), "natives/$backend")

    fun getPipelineCacheDir(): File = File(getSharedCachesDir(), "pipeline-cache")
}

// ============ Benchmark Repository ============

class BenchmarkRepository(
    private val dao: Daos.BenchmarkDao,
    context: Context
) : BaseRepository(dao, context) {

    suspend fun saveResult(result: BenchmarkResultEntity) = dao.insert(result)

    suspend fun getResult(gpuHash: String): BenchmarkResultEntity? = dao.getByGpuHash(gpuHash)

    suspend fun getLatestResult(): BenchmarkResultEntity? = dao.getLatest()
}

// ============ Mod Repository ============

class ModRepository(
    private val dao: Daos.ModDao,
    context: Context
) : BaseRepository(dao, context) {

    fun getModsForInstance(instanceId: String): Flow<List<ModEntity>> = dao.getModsForInstance(instanceId)

    suspend fun getMod(instanceId: String, modId: String): ModEntity? = dao.getMod(instanceId, modId)

    suspend fun insertMod(mod: ModEntity) = dao.insert(mod)

    suspend fun insertMods(mods: List<ModEntity>) = dao.insertAll(mods)

    suspend fun updateMod(mod: ModEntity) = dao.update(mod)

    suspend fun deleteMod(instanceId: String, modId: String) {
        dao.getMod(instanceId, modId)?.let { dao.delete(it) }
    }

    suspend fun deleteAllForInstance(instanceId: String) = dao.deleteAllForInstance(instanceId)

    suspend fun toggleEnabled(instanceId: String, modId: String) {
        dao.getMod(instanceId, modId)?.let { mod ->
            dao.update(mod.copy(isEnabled = !mod.isEnabled))
        }
    }
}

// ============ Resource Pack Repository ============

class ResourcePackRepository(
    private val dao: Daos.ResourcePackDao,
    context: Context
) : BaseRepository(dao, context) {

    fun getPacksForInstance(instanceId: String): Flow<List<ResourcePackEntity>> = dao.getPacksForInstance(instanceId)

    suspend fun insertPack(pack: ResourcePackEntity) = dao.insert(pack)

    suspend fun updatePack(pack: ResourcePackEntity) = dao.update(pack)

    suspend fun deletePack(instanceId: String, packId: String) {
        val id = "$instanceId:$packId"
        dao.getPacksForInstance(instanceId).first().firstOrNull { it.id == id }?.let { dao.delete(it) }
    }

    suspend fun deleteAllForInstance(instanceId: String) = dao.deleteAllForInstance(instanceId)

    suspend fun reorderPacks(instanceId: String, packIds: List<String>) {
        val packs = dao.getPacksForInstance(instanceId).first().toMutableMap { it.packId to it }
        packIds.forEachIndexed { index, packId ->
            packs[packId]?.let { dao.update(it.copy(priority = index)) }
        }
    }
}

// ============ Shader Pack Repository ============

class ShaderPackRepository(
    private val dao: Daos.ShaderPackDao,
    context: Context
) : BaseRepository(dao, context) {

    fun getPacksForInstance(instanceId: String): Flow<List<ShaderPackEntity>> = dao.getPacksForInstance(instanceId)

    suspend fun getActivePack(instanceId: String): ShaderPackEntity? = dao.getActivePack(instanceId)

    suspend fun insertPack(pack: ShaderPackEntity) = dao.insert(pack)

    suspend fun updatePack(pack: ShaderPackEntity) = dao.update(pack)

    suspend fun deletePack(instanceId: String, packId: String) {
        val id = "$instanceId:$packId"
        dao.getPacksForInstance(instanceId).first().firstOrNull { it.id == id }?.let { dao.delete(it) }
    }

    suspend fun deleteAllForInstance(instanceId: String) = dao.deleteAllForInstance(instanceId)

    suspend fun setActivePack(instanceId: String, packId: String) {
        val packs = dao.getPacksForInstance(instanceId).first()
        packs.forEach { pack ->
            dao.update(pack.copy(isActive = pack.packId == packId))
        }
    }
}

// ============ World Repository ============

class WorldRepository(
    private val dao: Daos.WorldDao,
    context: Context
) : BaseRepository(dao, context) {

    fun getWorldsForInstance(instanceId: String): Flow<List<WorldEntity>> = dao.getWorldsForInstance(instanceId)

    suspend fun insertWorld(world: WorldEntity) = dao.insert(world)

    suspend fun updateWorld(world: WorldEntity) = dao.update(world)

    suspend fun deleteWorld(instanceId: String, worldId: String) {
        val id = "$instanceId:$worldId"
        dao.getWorldsForInstance(instanceId).first().firstOrNull { it.id == id }?.let { dao.delete(it) }
    }

    suspend fun deleteAllForInstance(instanceId: String) = dao.deleteAllForInstance(instanceId)
}

// ============ Control Layout Repository ============

class ControlLayoutRepository(
    private val dao: Daos.ControlLayoutDao,
    context: Context
) : BaseRepository(dao, context) {

    fun getLayoutsForInstance(instanceId: String): Flow<List<ControlLayoutEntity>> = dao.getLayoutsForInstance(instanceId)

    suspend fun getDefaultLayout(instanceId: String): ControlLayoutEntity? = dao.getDefaultLayout(instanceId)

    suspend fun insertLayout(layout: ControlLayoutEntity) = dao.insert(layout)

    suspend fun updateLayout(layout: ControlLayoutEntity) = dao.update(layout)

    suspend fun deleteLayout(instanceId: String, layoutName: String) {
        val id = "$instanceId:$layoutName"
        dao.getLayoutsForInstance(instanceId).first().firstOrNull { it.id == id }?.let { dao.delete(it) }
    }

    suspend fun deleteAllForInstance(instanceId: String) = dao.deleteAllForInstance(instanceId)

    suspend fun setDefaultLayout(instanceId: String, layoutName: String) {
        val layouts = dao.getLayoutsForInstance(instanceId).first()
        layouts.forEach { layout ->
            dao.update(layout.copy(isDefault = layout.layoutName == layoutName))
        }
    }
}

// ============ Screenshot Repository ============

class ScreenshotRepository(
    private val dao: Daos.ScreenshotDao,
    context: Context
) : BaseRepository(dao, context) {

    fun getScreenshotsForInstance(instanceId: String): Flow<List<ScreenshotEntity>> = dao.getScreenshotsForInstance(instanceId)

    suspend fun insertScreenshot(screenshot: ScreenshotEntity) = dao.insert(screenshot)

    suspend fun deleteScreenshot(instanceId: String, screenshotId: String) {
        val id = "$instanceId:$screenshotId"
        dao.getScreenshotsForInstance(instanceId).first().firstOrNull { it.id == id }?.let { dao.delete(it) }
    }

    suspend fun deleteAllForInstance(instanceId: String) = dao.deleteAllForInstance(instanceId)
}

// ============ App Settings (DataStore) ============

private val KEY_RENDER_DEFAULT_BACKEND = preferencesKey<String>("render_default_backend")
private val KEY_CONTROL_DEFAULT_LAYOUT = preferencesKey<String>("control_default_layout")
private val KEY_GLOBAL_SENSITIVITY = preferencesKey<Float>("global_sensitivity")
private val KEY_PHYSICAL_INPUT_AUTO_SWITCH = preferencesKey<Boolean>("physical_input_auto_switch")
private val KEY_DOWNLOAD_THROTTLE = preferencesKey<Boolean>("download_throttle")
private val KEY_PAUSE_MOBILE_DATA = preferencesKey<Boolean>("pause_mobile_data")
private val KEY_DEFAULT_RENDER_DISTANCE = preferencesKey<Int>("default_render_distance")
private val KEY_DEFAULT_GRAPHICS_PRESET = preferencesKey<String>("default_graphics_preset")
private val KEY_INSTANCE_LIST_VIEW = preferencesKey<Boolean>("instance_list_grid_view") // true = grid, false = list
private val KEY_DEBUG_OVERLAY_DEFAULT = preferencesKey<Boolean>("debug_overlay_default")
private val KEY_EXTERNAL_STORAGE_PATH = preferencesKey<String>("external_storage_path")

class AppSettingsRepository(context: Context) {
    private val dataStore = RxPreferenceDataStoreBuilder(context, "settings").build()
    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun getRenderDefaultBackend(): RenderBackend = dataStore.data
        .map { it[KEY_RENDER_DEFAULT_BACKEND]?.let { RenderBackend.valueOf(it) } ?: RenderBackend.AUTO }
        .first()

    suspend fun setRenderDefaultBackend(backend: RenderBackend) {
        dataStore.edit { it[KEY_RENDER_DEFAULT_BACKEND] = backend.name }
    }

    suspend fun getInstanceListGridView(): Boolean = dataStore.data
        .map { it[KEY_INSTANCE_LIST_VIEW] ?: true }
        .first()

    suspend fun setInstanceListGridView(isGrid: Boolean) {
        dataStore.edit { it[KEY_INSTANCE_LIST_VIEW] = isGrid }
    }

    suspend fun getDebugOverlayDefault(): Boolean = dataStore.data
        .map { it[KEY_DEBUG_OVERLAY_DEFAULT] ?: false }
        .first()

    suspend fun setDebugOverlayDefault(enabled: Boolean) {
        dataStore.edit { it[KEY_DEBUG_OVERLAY_DEFAULT] = enabled }
    }

    // ... other settings getters/setters follow same pattern
}