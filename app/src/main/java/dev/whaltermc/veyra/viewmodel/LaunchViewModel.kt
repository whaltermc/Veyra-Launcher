package dev.whaltermc.veyra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.whaltermc.veyra.VeyraApplication
import dev.whaltermc.veyra.launch.LaunchManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LaunchViewModel(
    private val launchManager: LaunchManager,
    private val instanceId: String
) : ViewModel() {

    private val _currentStage = MutableStateFlow<LaunchManager.LaunchStage>(LaunchManager.LaunchStage.VERIFY_FILES)
    val currentStage = _currentStage.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress = _progress.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isComplete = MutableStateFlow(false)
    val isComplete = _isComplete.asStateFlow()

    fun startLaunch() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = launchManager.launchInstance(instanceId) { stage, p ->
                _currentStage.value = stage
                _progress.value = (stage.order - 1 + p) / LaunchManager.LaunchStage.values().size
            }

            result.onSuccess {
                _isComplete.value = true
                _progress.value = 1f
            }.onFailure {
                _error.value = it.message ?: "Unknown error"
            }
        }
    }

    companion object {
        class Factory(
            private val app: VeyraApplication,
            private val instanceId: String
        ) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LaunchViewModel(app.launchManager, instanceId) as T
            }
        }
    }
}