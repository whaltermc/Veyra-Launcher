package dev.whaltermc.veyra.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.whaltermc.veyra.data.db.converters.AuthTypeConverter
import dev.whaltermc.veyra.data.db.converters.InstantConverter
import dev.whaltermc.veyra.data.db.converters.ModloaderTypeConverter
import dev.whaltermc.veyra.data.db.converters.RenderBackendConverter
import dev.whaltermc.veyra.data.model.AccountEntity
import dev.whaltermc.veyra.data.model.BenchmarkResultEntity
import dev.whaltermc.veyra.data.model.ControlLayoutEntity
import dev.whaltermc.veyra.data.model.InstanceEntity
import dev.whaltermc.veyra.data.model.ModEntity
import dev.whaltermc.veyra.data.model.ResourcePackEntity
import dev.whaltermc.veyra.data.model.ScreenshotEntity
import dev.whaltermc.veyra.data.model.ShaderPackEntity
import dev.whaltermc.veyra.data.model.WorldEntity

@Database(
    entities = [
        AccountEntity::class,
        InstanceEntity::class,
        BenchmarkResultEntity::class,
        ModEntity::class,
        ResourcePackEntity::class,
        ShaderPackEntity::class,
        WorldEntity::class,
        ControlLayoutEntity::class,
        ScreenshotEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    InstantConverter::class,
    AuthTypeConverter::class,
    ModloaderTypeConverter::class,
    RenderBackendConverter::class
)
abstract class VeyraDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun instanceDao(): InstanceDao
    abstract fun benchmarkDao(): BenchmarkDao
    abstract fun modDao(): ModDao
    abstract fun resourcePackDao(): ResourcePackDao
    abstract fun shaderPackDao(): ShaderPackDao
    abstract fun worldDao(): WorldDao
    abstract fun controlLayoutDao(): ControlLayoutDao
    abstract fun screenshotDao(): ScreenshotDao

    companion object {
        @Volatile private var INSTANCE: VeyraDatabase? = null

        fun getDatabase(context: Context): VeyraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VeyraDatabase::class.java,
                    "veyra.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}