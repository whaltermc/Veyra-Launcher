package dev.whaltermc.veyra.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import dev.whaltermc.veyra.data.db.converters.AuthTypeConverter
import dev.whaltermc.veyra.data.db.converters.InstantConverter
import dev.whaltermc.veyra.data.db.converters.ModloaderTypeConverter
import dev.whaltermc.veyra.data.db.converters.RenderBackendConverter
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
enum class AuthType {
    MICROSOFT,
    ELY_BY,
    OFFLINE
}

@Serializable
enum class ModloaderType {
    VANILLA,
    FABRIC,
    QUILT,
    FORGE,
    NEOFORGE
}

@Serializable
enum class RenderBackend {
    AUTO,
    PANVK_NATIVE,
    ZINK_PANVK,
    RUSTGL,
    GL4ES
}

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val uuid: String,
    @TypeConverters(AuthTypeConverter::class) val authType: AuthType,
    val encryptedTokenBlob: String, // Encrypted JSON with access/refresh tokens
    val skinCachePath: String? = null,
    @TypeConverters(InstantConverter::class) val lastUsed: Instant = Instant.now(),
    val skinUrl: String? = null,
    val capeUrl: String? = null
)

@Entity(tableName = "instances")
data class InstanceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconPath: String?,
    val gameVersion: String,
    @TypeConverters(ModloaderTypeConverter::class) val modloaderType: ModloaderType,
    val modloaderVersion: String?,
    val javaMajorOverride: Int? = null, // null = auto
    val ramMinMb: Int,
    val ramMaxMb: Int,
    @TypeConverters(RenderBackendConverter::class) val renderBackendOverride: RenderBackend = RenderBackend.AUTO,
    val advancedJvmArgs: String? = null, // User-edited delta from generated default
    @TypeConverters(InstantConverter::class) val createdAt: Instant = Instant.now(),
    @TypeConverters(InstantConverter::class) val lastPlayedAt: Instant? = null,
    val totalPlayTimeSeconds: Long = 0,
    val isPinned: Boolean = false
)

@Entity(tableName = "benchmark_results")
data class BenchmarkResultEntity(
    @PrimaryKey val gpuHash: String, // Hash of GPU vendor/renderer + driver version
    val rankedBackends: String, // JSON array of RenderBackend in order
    val frametimesJson: String, // JSON map of backend -> avg frametime ms
    @TypeConverters(InstantConverter::class) val timestamp: Instant = Instant.now()
)

@Entity(tableName = "mods")
data class ModEntity(
    @PrimaryKey val id: String, // Composite: instanceId:modId
    val instanceId: String,
    val modId: String,
    val name: String,
    val version: String,
    val iconUrl: String?,
    val source: String, // "modrinth" | "curseforge" | "manual"
    val sourceUrl: String?,
    val filePath: String,
    val fileHash: String,
    val isEnabled: Boolean = true,
    val dependenciesJson: String? = null, // JSON array of declared dependencies
    val isDependency: Boolean = false
)

@Entity(tableName = "resource_packs")
data class ResourcePackEntity(
    @PrimaryKey val id: String, // Composite: instanceId:packId
    val instanceId: String,
    val packId: String,
    val name: String,
    val filePath: String,
    val thumbnailPath: String?,
    val isEnabled: Boolean = true,
    val priority: Int // Lower = higher priority (top of list)
)

@Entity(tableName = "shader_packs")
data class ShaderPackEntity(
    @PrimaryKey val id: String, // Composite: instanceId:packId
    val instanceId: String,
    val packId: String,
    val name: String,
    val filePath: String,
    val isActive: Boolean = false,
    val compatibleBackendsJson: String? = null // JSON array of RenderBackend
)

@Entity(tableName = "worlds")
data class WorldEntity(
    @PrimaryKey val id: String, // Composite: instanceId:worldId
    val instanceId: String,
    val worldId: String,
    val name: String,
    val thumbnailPath: String?,
    @TypeConverters(InstantConverter::class) val lastPlayedAt: Instant? = null,
    val gameMode: Int = 0, // 0=survival, 1=creative, 2=adventure, 3=spectator
    val difficulty: Int = 2, // 0=peaceful, 1=easy, 2=normal, 3=hard
    val isHardcore: Boolean = false
)

@Entity(tableName = "control_layouts")
data class ControlLayoutEntity(
    @PrimaryKey val id: String, // Composite: instanceId:layoutName
    val instanceId: String,
    val layoutName: String,
    val layoutJson: String, // Serialized ControlLayout
    val isDefault: Boolean = false
)

@Entity(tableName = "screenshots")
data class ScreenshotEntity(
    @PrimaryKey val id: String, // Composite: instanceId:screenshotId
    val instanceId: String,
    val screenshotId: String,
    val filePath: String,
    @TypeConverters(InstantConverter::class) val takenAt: Instant = Instant.now()
)