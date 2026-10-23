package dev.whaltermc.veyra.render

import android.content.Context
import android.opengl.GLES20
import dev.whaltermc.veyra.data.model.RenderBackend
import dev.whaltermc.veyra.data.repository.BenchmarkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest

class RenderBackendManager(
    private val context: Context,
    private val benchmarkRepository: BenchmarkRepository
) {

    private val benchmarkPrefs = context.getSharedPreferences("veyra_benchmark", Context.MODE_PRIVATE)

    // Preference order for backends (used as fallback chain)
    private val backendPreferenceOrder = listOf(
        RenderBackend.PANVK_NATIVE,
        RenderBackend.ZINK_PANVK,
        RenderBackend.RUSTGL,
        RenderBackend.GL4ES
    )

    suspend fun ensureBenchmarkRun() {
        val gpuHash = getGpuHash()
        val cached = benchmarkPrefs.getString("gpu_hash", "") ?: ""
        val cachedResult = benchmarkRepository.getResult(gpuHash).await()

        if (cached != gpuHash || cachedResult == null) {
            runBenchmark(gpuHash)
        }
    }

    private fun getGpuHash(): String {
        val vendor = GLES20.glGetString(GLES20.GL_VENDOR) ?: "unknown"
        val renderer = GLES20.glGetString(GLES20.GL_RENDERER) ?: "unknown"
        val version = GLES20.glGetString(GLES20.GL_VERSION) ?: "unknown"
        val input = "$vendor|$renderer|$version"
        return MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(16)
    }

    suspend fun runBenchmark(gpuHash: String): Result<BenchmarkResult> = kotlinx.coroutines.CoroutineScope(Dispatchers.IO).async {
        try {
            val results = mutableMapOf<RenderBackend, Double>()
            val successfulBackends = mutableListOf<RenderBackend>()

            for (backend in backendPreferenceOrder) {
                val initResult = tryInitializeBackend(backend)
                if (initResult) {
                    val frametime = runSyntheticWorkload(backend)
                    results[backend] = frametime
                    successfulBackends.add(backend)
                }
                cleanupBackend(backend)
            }

            val ranked = successfulBackends.sortedBy { results[it]!! }
            val benchmarkResult = BenchmarkResult(
                gpuHash = gpuHash,
                rankedBackends = ranked,
                frametimes = results,
                timestamp = Instant.now()
            )

            benchmarkRepository.saveResult(benchmarkResult.toEntity()).await()
            benchmarkPrefs.edit { putString("gpu_hash", gpuHash) }.apply()

            Result.success(benchmarkResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }.await()

    private fun tryInitializeBackend(backend: RenderBackend): Boolean {
        return try {
            when (backend) {
                RenderBackend.PANVK_NATIVE -> initializePanVK()
                RenderBackend.ZINK_PANVK -> initializeZinkPanVK()
                RenderBackend.RUSTGL -> initializeRustGL()
                RenderBackend.GL4ES -> initializeGL4ES()
                RenderBackend.AUTO -> false // Handled by ranked list
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun initializePanVK(): Boolean {
        // TODO: Load PanVK ICD via System.loadLibrary or dlopen
        // Check if PanVK .so exists and can be loaded as Vulkan ICD
        return false // Placeholder
    }

    private fun initializeZinkPanVK(): Boolean {
        // TODO: Initialize Zink with PanVK as underlying driver
        return false // Placeholder
    }

    private fun initializeRustGL(): Boolean {
        // TODO: Load RustGL plugin
        return false // Placeholder
    }

    private fun initializeGL4ES(): Boolean {
        // TODO: Initialize GL4ES
        return true // Always available as fallback
    }

    private fun cleanupBackend(backend: RenderBackend) {
        // TODO: Cleanup backend resources
    }

    private fun runSyntheticWorkload(backend: RenderBackend): Double {
        // TODO: Run a short synthetic workload representative of Minecraft chunk rendering
        // Return average frametime in ms
        return Math.random() * 16 + 8 // Placeholder: 8-24ms
    }

    fun getDeviceDefaultBackend(): RenderBackend {
        val gpuHash = getGpuHash()
        val cachedResult = benchmarkRepository.getResult(gpuHash).awaitOrNull()
        return cachedResult?.rankedBackends?.firstOrNull() ?: RenderBackend.GL4ES
    }

    fun getRankedBackends(): List<RenderBackend> {
        val gpuHash = getGpuHash()
        val cachedResult = benchmarkRepository.getResult(gpuHash).awaitOrNull()
        return cachedResult?.rankedBackends ?: backendPreferenceOrder
    }

    suspend fun initRenderBackend(instanceId: String, instanceOverride: RenderBackend?): Result<RenderBackend> {
        val override = instanceOverride ?: RenderBackend.AUTO
        val ranked = if (override == RenderBackend.AUTO) getRankedBackends() else listOf(override) + getRankedBackends().filter { it != override }

        for (backend in ranked) {
            if (tryInitializeBackend(backend)) {
                return Result.success(backend)
            }
        }
        return Result.failure(Exception("All render backends failed to initialize"))
    }
}

data class BenchmarkResult(
    val gpuHash: String,
    val rankedBackends: List<RenderBackend>,
    val frametimes: Map<RenderBackend, Double>,
    val timestamp: Instant
) {
    fun toEntity() = dev.whaltermc.veyra.data.model.BenchmarkResultEntity(
        gpuHash = gpuHash,
        rankedBackends = Json.encodeToString(rankedBackends.map { it.name }),
        frametimesJson = Json.encodeToString(frametimes),
        timestamp = timestamp
    )
}