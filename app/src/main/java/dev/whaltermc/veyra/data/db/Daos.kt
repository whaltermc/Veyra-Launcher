package dev.whaltermc.veyra.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow
import dev.whaltermc.veyra.data.model.AccountEntity
import dev.whaltermc.veyra.data.model.InstanceEntity
import dev.whaltermc.veyra.data.model.BenchmarkResultEntity
import dev.whaltermc.veyra.data.model.ModEntity
import dev.whaltermc.veyra.data.model.ResourcePackEntity
import dev.whaltermc.veyra.data.model.ShaderPackEntity
import dev.whaltermc.veyra.data.model.WorldEntity
import dev.whaltermc.veyra.data.model.ControlLayoutEntity
import dev.whaltermc.veyra.data.model.ScreenshotEntity

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity)

    @Update
    suspend fun update(account: AccountEntity)

    @Delete
    suspend fun delete(account: AccountEntity)

    @Query("SELECT * FROM accounts ORDER BY lastUsed DESC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE uuid = :uuid AND authType = :authType")
    suspend fun getAccountByUuidAndAuthType(uuid: String, authType: String): AccountEntity?
}

@Dao
interface InstanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(instance: InstanceEntity)

    @Update
    suspend fun update(instance: InstanceEntity)

    @Delete
    suspend fun delete(instance: InstanceEntity)

    @Query("SELECT * FROM instances ORDER BY CASE WHEN isPinned THEN 0 ELSE 1 END, lastPlayedAt DESC NULLS LAST")
    fun getAllInstances(): Flow<List<InstanceEntity>>

    @Query("SELECT * FROM instances WHERE id = :id")
    suspend fun getInstanceById(id: String): InstanceEntity?

    @Query("SELECT * FROM instances WHERE id = :id")
    fun getInstanceByIdFlow(id: String): Flow<InstanceEntity?>

    @Query("DELETE FROM instances WHERE id = :id")
    suspend fun deleteById(id: String): Int
}

@Dao
interface BenchmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: BenchmarkResultEntity)

    @Query("SELECT * FROM benchmark_results WHERE gpuHash = :gpuHash")
    suspend fun getByGpuHash(gpuHash: String): BenchmarkResultEntity?

    @Query("SELECT * FROM benchmark_results ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(): BenchmarkResultEntity?
}

@Dao
interface ModDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mod: ModEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mods: List<ModEntity>)

    @Update
    suspend fun update(mod: ModEntity)

    @Delete
    suspend fun delete(mod: ModEntity)

    @Query("SELECT * FROM mods WHERE instanceId = :instanceId ORDER BY name ASC")
    fun getModsForInstance(instanceId: String): Flow<List<ModEntity>>

    @Query("SELECT * FROM mods WHERE instanceId = :instanceId AND modId = :modId")
    suspend fun getMod(instanceId: String, modId: String): ModEntity?

    @Query("DELETE FROM mods WHERE instanceId = :instanceId")
    suspend fun deleteAllForInstance(instanceId: String): Int
}

@Dao
interface ResourcePackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pack: ResourcePackEntity)

    @Update
    suspend fun update(pack: ResourcePackEntity)

    @Delete
    suspend fun delete(pack: ResourcePackEntity)

    @Query("SELECT * FROM resource_packs WHERE instanceId = :instanceId ORDER BY priority ASC")
    fun getPacksForInstance(instanceId: String): Flow<List<ResourcePackEntity>>

    @Query("DELETE FROM resource_packs WHERE instanceId = :instanceId")
    suspend fun deleteAllForInstance(instanceId: String): Int
}

@Dao
interface ShaderPackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pack: ShaderPackEntity)

    @Update
    suspend fun update(pack: ShaderPackEntity)

    @Delete
    suspend fun delete(pack: ShaderPackEntity)

    @Query("SELECT * FROM shader_packs WHERE instanceId = :instanceId ORDER BY name ASC")
    fun getPacksForInstance(instanceId: String): Flow<List<ShaderPackEntity>>

    @Query("SELECT * FROM shader_packs WHERE instanceId = :instanceId AND isActive = 1")
    suspend fun getActivePack(instanceId: String): ShaderPackEntity?

    @Query("DELETE FROM shader_packs WHERE instanceId = :instanceId")
    suspend fun deleteAllForInstance(instanceId: String): Int
}

@Dao
interface WorldDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(world: WorldEntity)

    @Update
    suspend fun update(world: WorldEntity)

    @Delete
    suspend fun delete(world: WorldEntity)

    @Query("SELECT * FROM worlds WHERE instanceId = :instanceId ORDER BY lastPlayedAt DESC NULLS LAST")
    fun getWorldsForInstance(instanceId: String): Flow<List<WorldEntity>>

    @Query("DELETE FROM worlds WHERE instanceId = :instanceId")
    suspend fun deleteAllForInstance(instanceId: String): Int
}

@Dao
interface ControlLayoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(layout: ControlLayoutEntity)

    @Update
    suspend fun update(layout: ControlLayoutEntity)

    @Delete
    suspend fun delete(layout: ControlLayoutEntity)

    @Query("SELECT * FROM control_layouts WHERE instanceId = :instanceId ORDER BY layoutName ASC")
    fun getLayoutsForInstance(instanceId: String): Flow<List<ControlLayoutEntity>>

    @Query("SELECT * FROM control_layouts WHERE instanceId = :instanceId AND isDefault = 1")
    suspend fun getDefaultLayout(instanceId: String): ControlLayoutEntity?

    @Query("DELETE FROM control_layouts WHERE instanceId = :instanceId")
    suspend fun deleteAllForInstance(instanceId: String): Int
}

@Dao
interface ScreenshotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(screenshot: ScreenshotEntity)

    @Delete
    suspend fun delete(screenshot: ScreenshotEntity)

    @Query("SELECT * FROM screenshots WHERE instanceId = :instanceId ORDER BY takenAt DESC")
    fun getScreenshotsForInstance(instanceId: String): Flow<List<ScreenshotEntity>>

    @Query("DELETE FROM screenshots WHERE instanceId = :instanceId")
    suspend fun deleteAllForInstance(instanceId: String): Int
}