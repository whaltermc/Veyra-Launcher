package dev.whaltermc.veyra.launch

import android.content.Context
import dev.whaltermc.veyra.VeyraApplication
import dev.whaltermc.veyra.auth.AuthManager
import dev.whaltermc.veyra.data.model.InstanceEntity
import dev.whaltermc.veyra.data.model.RenderBackend
import dev.whaltermc.veyra.data.repository.InstanceRepository
import dev.whaltermc.veyra.jvm.JreManager
import dev.whaltermc.veyra.render.RenderBackendManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import java.io.File

class LaunchManager(
    private val context: Context,
    private val instanceRepository: InstanceRepository,
    private val authManager: AuthManager,
    private val jreManager: JreManager,
    private val renderBackendManager: RenderBackendManager
) {

    private val launchCacheDir = File(context.filesDir, "caches/launch-graphs")

    // Launch stages for progress reporting
    enum class LaunchStage(
        val label: String,
        val order: Int
    ) {
        VERIFY_FILES("Verifying files", 1),
        DOWNLOAD_ASSETS("Downloading missing assets", 2),
        PREPARE_JRE("Preparing Java runtime", 3),
        INIT_RENDER_BACKEND("Initializing render backend", 4),
        START_MINECRAFT("Starting Minecraft", 5)
    }

    suspend fun launchInstance(
        instanceId: String,
        onProgress: (LaunchStage, Float) -> Unit
    ): Result<LaunchResult> {
        return kotlinx.coroutines.CoroutineScope(Dispatchers.IO).async {
            try {
                val instance = instanceRepository.getInstanceById(instanceId).await()
                    ?: return@async Result.failure(Exception("Instance not found"))

                val account = authManager.getActiveAccount()
                    ?: return@async Result.failure(Exception("No active account"))

                // Ensure valid token
                authManager.ensureValidToken(account.id)

                // Stage 1: Verify files
                onProgress(LaunchStage.VERIFY_FILES, 0f)
                verifyInstanceFiles(instance)
                onProgress(LaunchStage.VERIFY_FILES, 1f)

                // Stage 2: Download missing assets
                onProgress(LaunchStage.DOWNLOAD_ASSETS, 0f)
                downloadMissingAssets(instance)
                onProgress(LaunchStage.DOWNLOAD_ASSETS, 1f)

                // Stage 3: Prepare JRE
                onProgress(LaunchStage.PREPARE_JRE, 0f)
                val javaPath = prepareJre(instance)
                onProgress(LaunchStage.PREPARE_JRE, 1f)

                // Stage 4: Init render backend
                onProgress(LaunchStage.INIT_RENDER_BACKEND, 0f)
                val backendResult = renderBackendManager.initRenderBackend(instanceId, instance.renderBackendOverride).await()
                if (backendResult.isFailure) {
                    return@async Result.failure(backendResult.exceptionOrNull()!!)
                }
                val backend = backendResult.getOrThrow()
                onProgress(LaunchStage.INIT_RENDER_BACKEND, 1f)

                // Stage 5: Start Minecraft
                onProgress(LaunchStage.START_MINECRAFT, 0f)
                val launchCommand = buildLaunchCommand(instance, account, javaPath, backend)
                val process = startMinecraftProcess(launchCommand, instance)
                onProgress(LaunchStage.START_MINECRAFT, 1f)

                // Cache launch graph for warm boot
                cacheLaunchGraph(instanceId, launchCommand)

                Result.success(LaunchResult(process = process, backend = backend))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }.await()
    }

    private fun verifyInstanceFiles(instance: InstanceEntity) {
        // TODO: Check required libraries, natives, and asset objects against expected hashes
        // From Mojang's version manifest
    }

    private fun downloadMissingAssets(instance: InstanceEntity) {
        // TODO: Download only what's missing/mismatched on background thread pool
    }

    private fun prepareJre(instance: InstanceEntity): File {
        val requiredMajor = jreManager.resolveRequiredJreMajor(instance.gameVersion, instance.modloaderType)
        val effectiveMajor = instance.javaMajorOverride ?: requiredMajor
        val jrePath = jreManager.ensureJreInstalled(effectiveMajor).getOrThrow()
        return jreManager.getJavaExecutable(effectiveMajor)
    }

    private fun buildLaunchCommand(
        instance: InstanceEntity,
        account: dev.whaltermc.veyra.data.model.AccountEntity,
        javaPath: File,
        backend: RenderBackend
    ): List<String> {
        val args = mutableListOf<String>()

        // Java executable
        args.add(javaPath.absolutePath)

        // Memory flags
        args.add("-Xms${instance.ramMinMb}M")
        args.add("-Xmx${instance.ramMaxMb}M")

        // GC flags
        args.add("-XX:+UseG1GC")
        args.add("-XX:MaxGCPauseMillis=50")
        args.add("-XX:+ParallelRefProcEnabled")
        args.add("-XX:+UnlockExperimentalVMOptions")

        // Native library paths
        val nativesDir = instanceRepository.getNativesDir(backend.name)
        args.add("-Djava.library.path=${nativesDir.absolutePath}")

        // Render backend system properties
        when (backend) {
            RenderBackend.PANVK_NATIVE -> {
                args.add("-Dorg.lwjgl.vulkan.libname=panvk")
                // Force Vulkan ICD path
            }
            RenderBackend.ZINK_PANVK -> {
                args.add("-Dorg.lwjgl.opengl.libname=zink")
                // Zink config
            }
            RenderBackend.RUSTGL -> {
                args.add("-Dorg.lwjgl.opengl.libname=rustgl")
            }
            RenderBackend.GL4ES -> {
                args.add("-Dorg.lwjgl.opengl.libname=gl4es")
            }
            RenderBackend.AUTO -> {}
        }

        // Minecraft game arguments
        args.add("-Dminecraft.client.jar=${getClientJarPath(instance)}")
        args.add("-Dminecraft.launcher.brand=veyra")
        args.add("-Dminecraft.launcher.version=1.0.0")

        // Main class (depends on modloader)
        val mainClass = getMainClass(instance)
        args.add(mainClass)

        // Game arguments
        args.add("--username")
        args.add(account.displayName)
        args.add("--uuid")
        args.add(account.uuid)
        args.add("--accessToken")
        args.add(extractAccessToken(account))
        args.add("--gameDir")
        args.add(instanceRepository.getInstanceDir(instance.id).absolutePath)
        args.add("--assetsDir")
        args.add(instanceRepository.getAssetsDir().absolutePath)
        args.add("--assetIndex")
        args.add(getAssetIndex(instance))
        args.add("--version")
        args.add(instance.gameVersion)
        args.add("--userType")
        args.add(account.authType.name.toLowerCase())

        // Add user's advanced args
        instance.advancedJvmArgs?.let { args.addAll(it.split(" ").filter { it.isNotBlank() }) }

        return args
    }

    private fun getMainClass(instance: InstanceEntity): String {
        return when (instance.modloaderType) {
            dev.whaltermc.veyra.data.model.ModloaderType.VANILLA -> "net.minecraft.client.main.Main"
            dev.whaltermc.veyra.data.model.ModloaderType.FABRIC -> "net.fabricmc.loader.impl.launch.knot.KnotClient"
            dev.whaltermc.veyra.data.model.ModloaderType.QUILT -> "org.quiltmc.loader.impl.launch.knot.KnotClient"
            dev.whaltermc.veyra.data.model.ModloaderType.FORGE -> "cpw.mods.modlauncher.Launcher"
            dev.whaltermc.veyra.data.model.ModloaderType.NEOFORGE -> "cpw.mods.modlauncher.Launcher"
        }
    }

    private fun getClientJarPath(instance: InstanceEntity): String {
        // TODO: Return path to client.jar for this version
        return instanceRepository.getLibrariesDir().resolve("com/mojang/minecraft/${instance.gameVersion}/minecraft-${instance.gameVersion}-client.jar").toString()
    }

    private fun getAssetIndex(instance: InstanceEntity): String {
        // TODO: Return asset index ID for this version
        return instance.gameVersion
    }

    private fun extractAccessToken(account: dev.whaltermc.veyra.data.model.AccountEntity): String {
        // TODO: Decrypt and extract access token from encryptedTokenBlob
        return "dummy_token"
    }

    private fun startMinecraftProcess(command: List<String>, instance: InstanceEntity): Process {
        val instanceDir = instanceRepository.getInstanceDir(instance.id)
        val processBuilder = ProcessBuilder(command)
            .directory(instanceDir)
            .redirectErrorStream(true)

        // Set environment variables for render backend
        processBuilder.environment()["VK_ICD_FILENAMES"] = getVkIcdPath()
        processBuilder.environment()["MESA_LOADER_DRIVER_OVERRIDE"] = "kbase"
        processBuilder.environment()["LIBGL_ALWAYS_SOFTWARE"] = "0"

        return processBuilder.start()
    }

    private fun getVkIcdPath(): String {
        // TODO: Return path to PanVK ICD JSON
        return ""
    }

    private fun cacheLaunchGraph(instanceId: String, launchCommand: List<String>) {
        // TODO: Persist fully-resolved library/asset dependency graph
        launchCacheDir.mkdirs()
        val cacheFile = File(launchCacheDir, "$instanceId.json")
        // Write cache
    }

    fun preWarmInstance(instanceId: String) {
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            // TODO: Load cached launch graph and pre-verify files
        }
    }

    data class LaunchResult(
        val process: Process,
        val backend: RenderBackend
    )
}