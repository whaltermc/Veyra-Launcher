package dev.whaltermc.veyra.jvm

import android.content.Context
import dev.whaltermc.veyra.data.model.ModloaderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import java.io.File
import java.net.URL
import java.util.concurrent.Executors

class JreManager(private val context: Context) {

    private val jreCacheDir: File = File(context.filesDir, "caches/jre")
    private val ioExecutor = Executors.newSingleThreadExecutor()
    private val ioScope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO)

    // JRE major version requirements per Minecraft version
    fun resolveRequiredJreMajor(gameVersion: String, modloader: ModloaderType): Int {
        return when {
            // Java 21 for 1.20.5+
            gameVersion.startsWith("1.20.5") || gameVersion.startsWith("1.21") || gameVersion.startsWith("1.22") -> 21
            // Java 17 for 1.18 - 1.20.4
            gameVersion.startsWith("1.18") || gameVersion.startsWith("1.19") || gameVersion.startsWith("1.20") -> 17
            // Java 16 for 1.17
            gameVersion.startsWith("1.17") -> 16
            // Java 8 for <=1.16
            else -> 8
        }
    }

    suspend fun ensureJreInstalled(major: Int): Result<File> = ioScope.async {
        try {
            val jreDir = File(jreCacheDir, "$major")
            if (isJreValid(jreDir)) {
                Result.success(jreDir)
            } else {
                downloadAndExtractJre(major, jreDir)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }.await()

    private fun isJreValid(jreDir: File): Boolean {
        val javaExe = File(jreDir, "bin/java")
        return javaExe.exists() && javaExe.canExecute()
    }

    private fun downloadAndExtractJre(major: Int, targetDir: File): Result<File> {
        // TODO: Download Termux-compatible JRE from Adoptium or other trusted source
        // For Termux builds, we might use `pkg install openjdk-$major` instead
        return try {
            targetDir.mkdirs()
            // Placeholder - in reality would download and extract
            // For Termux, the JVM might be provided by the Termux environment
            Result.failure(Exception("JRE download not implemented for major $major"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getJrePath(major: Int): File = File(jreCacheDir, "$major")

    fun getJavaExecutable(major: Int): File = File(getJrePath(major), "bin/java")

    // For Termux builds, we might use the system JVM
    fun getTermuxJavaPath(): String? {
        return try {
            val process = Runtime.getRuntime().exec("which java")
            process.waitFor()
            val input = process.inputStream.readBytes().decodeToString().trim()
            if (input.isNotBlank()) input else null
        } catch (e: Exception) {
            null
        }
    }
}