package dev.whaltermc.veyra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.whaltermc.veyra.VeyraApplication
import dev.whaltermc.veyra.auth.AuthManager
import dev.whaltermc.veyra.data.repository.InstanceRepository
import dev.whaltermc.veyra.render.RenderBackendManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val app: VeyraApplication
) : ViewModel() {

    fun warmBoot() {
        viewModelScope.launch(Dispatchers.IO) {
            // Check for valid session token
            val activeAccount = app.authManager.getActiveAccount()
            if (activeAccount != null) {
                // Ensure token is valid (refresh if needed)
                app.authManager.ensureValidToken(activeAccount.id)
            }

            // Load instance list (happens via Flow in UI)

            // Run benchmark if never run
            app.renderBackendManager.ensureBenchmarkRun()

            // Pre-warm last used instance
            app.instanceRepository.getAllInstances()
                .first()
                .firstOrNull { it.isPinned || it.lastPlayedAt != null }
                ?.let { instance ->
                    // Pre-verify files in background
                    app.launchManager.preWarmInstance(instance.id)
                }
        }
    }
}