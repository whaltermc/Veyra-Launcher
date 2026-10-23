package dev.whaltermc.veyra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.whaltermc.veyra.VeyraApplication
import dev.whaltermc.veyra.data.model.InstanceEntity
import dev.whaltermc.veyra.data.repository.InstanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class InstanceDetailViewModel(
    private val repository: InstanceRepository,
    private val instanceId: String
) : ViewModel() {

    private val _instance = MutableStateFlow<InstanceEntity?>(null)
    val instance = _instance.asStateFlow()

    init {
        loadInstance()
    }

    private fun loadInstance() {
        viewModelScope.launch(Dispatchers.IO) {
            _instance.value = repository.getInstanceById(instanceId).await()
        }
    }

    companion object {
        class Factory(
            private val app: VeyraApplication,
            private val instanceId: String
        ) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return InstanceDetailViewModel(app.instanceRepository, instanceId) as T
            }
        }
    }
}