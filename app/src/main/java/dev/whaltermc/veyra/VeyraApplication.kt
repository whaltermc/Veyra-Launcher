package dev.whaltermc.veyra

import android.app.Application
import dev.whaltermc.veyra.data.db.VeyraDatabase
import dev.whaltermc.veyra.data.repository.AccountRepository
import dev.whaltermc.veyra.data.repository.InstanceRepository
import dev.whaltermc.veyra.data.repository.ModRepository
import dev.whaltermc.veyra.data.repository.ResourcePackRepository
import dev.whaltermc.veyra.data.repository.ShaderPackRepository
import dev.whaltermc.veyra.data.repository.WorldRepository
import dev.whaltermc.veyra.data.repository.ControlLayoutRepository
import dev.whaltermc.veyra.data.repository.ScreenshotRepository
import dev.whaltermc.veyra.data.repository.BenchmarkRepository
import dev.whaltermc.veyra.render.RenderBackendManager
import dev.whaltermc.veyra.jvm.JreManager
import dev.whaltermc.veyra.launch.LaunchManager
import dev.whaltermc.veyra.auth.AuthManager

class VeyraApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize database
        VeyraDatabase.getDatabase(this)
    }

    val database: VeyraDatabase by lazy { VeyraDatabase.getDatabase(this) }

    val accountRepository: AccountRepository by lazy { AccountRepository(database.accountDao(), this) }
    val instanceRepository: InstanceRepository by lazy { InstanceRepository(database.instanceDao(), this) }
    val modRepository: ModRepository by lazy { ModRepository(database.modDao(), this) }
    val resourcePackRepository: ResourcePackRepository by lazy { ResourcePackRepository(database.resourcePackDao(), this) }
    val shaderPackRepository: ShaderPackRepository by lazy { ShaderPackRepository(database.shaderPackDao(), this) }
    val worldRepository: WorldRepository by lazy { WorldRepository(database.worldDao(), this) }
    val controlLayoutRepository: ControlLayoutRepository by lazy { ControlLayoutRepository(database.controlLayoutDao(), this) }
    val screenshotRepository: ScreenshotRepository by lazy { ScreenshotRepository(database.screenshotDao(), this) }
    val benchmarkRepository: BenchmarkRepository by lazy { BenchmarkRepository(database.benchmarkDao(), this) }

    val authManager: AuthManager by lazy { AuthManager(this, accountRepository) }
    val jreManager: JreManager by lazy { JreManager(this) }
    val renderBackendManager: RenderBackendManager by lazy { RenderBackendManager(this, benchmarkRepository) }
    val launchManager: LaunchManager by lazy { LaunchManager(this, instanceRepository, authManager, jreManager, renderBackendManager) }
}