package dev.whaltermc.veyra.ui.screen.instance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.whaltermc.veyra.VeyraApplication
import dev.whaltermc.veyra.data.model.InstanceEntity
import dev.whaltermc.veyra.data.model.ModloaderType
import dev.whaltermc.veyra.data.model.RenderBackend
import dev.whaltermc.veyra.ui.component.Avatar
import dev.whaltermc.veyra.ui.theme.Theme.Veyra
import dev.whaltermc.veyra.viewmodel.NewInstanceViewModel
import dev.whaltermc.veyra.ui.component.ModloaderBadge
import dev.whaltermc.veyra.ui.component.BackendIndicator
import kotlinx.coroutines.launch
import dev.whaltermc.veyra.R
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.vector.icons.Icons
import androidx.compose.ui.graphics.vector.icons.filled.ArrowBack
import androidx.compose.ui.graphics.vector.icons.filled.Check
import androidx.compose.ui.graphics.vector.icons.filled.Close
import androidx.compose.ui.graphics.vector.icons.filled.Delete
import androidx.compose.ui.graphics.vector.icons.filled.Edit
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewInstanceScreen(
    instanceId: String?,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as VeyraApplication
    val viewModel: NewInstanceViewModel = viewModel(factory = NewInstanceViewModel.Factory(app, instanceId))
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isEditing = instanceId != null
    val name by viewModel.name
    val selectedVersion by viewModel.selectedVersion
    val selectedModloader by viewModel.selectedModloader
    val selectedModloaderVersion by viewModel.selectedModloaderVersion
    val ramMin by viewModel.ramMin
    val ramMax by viewModel.ramMax
    val selectedBackend by viewModel.selectedBackend
    val advancedArgs by viewModel.advancedArgs

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        TopAppBar(
            title = { Text(if (isEditing) "Edit Instance" else "New Instance") },
            navigationIcon = {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                Button(onClick = {
                    scope.launch {
                        viewModel.saveInstance()
                            .onSuccess { onSave() }
                            .onFailure { e ->
                                snackbarHostState.showSnackbar("Error: ${e.message}")
                            }
                    }
                }) {
                    Text(stringResource(R.string.save))
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
            )
        )

        SnackbarHost(snackbarHostState)

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(1) { item ->
                InstanceNameSection(name = name, onNameChange = { viewModel.name.value = it })
            }
            items(1) { item ->
                VersionPickerSection(
                    selectedVersion = selectedVersion,
                    onVersionClick = { viewModel.selectVersion(it) },
                    versions = viewModel.versions.collectAsStateWithLifecycle().value ?: emptyList(),
                    isLoading = viewModel.isLoadingVersions.collectAsStateWithLifecycle().value ?: false
                )
            }
            items(1) { item ->
                ModloaderPickerSection(
                    selectedModloader = selectedModloader,
                    selectedModloaderVersion = selectedModloaderVersion,
                    onModloaderChange = { viewModel.selectedModloader.value = it },
                    onModloaderVersionChange = { viewModel.selectedModloaderVersion.value = it },
                    modloaderVersions = viewModel.modloaderVersions.collectAsStateWithLifecycle().value ?: emptyList(),
                    isLoading = viewModel.isLoadingModloaderVersions.collectAsStateWithLifecycle().value ?: false
                )
            }
            items(1) { item ->
                RamAllocationSection(
                    ramMin = ramMin,
                    ramMax = ramMax,
                    onMinChange = { viewModel.ramMin.value = it },
                    onMaxChange = { viewModel.ramMax.value = it },
                    maxAllowed = viewModel.maxRamMb.collectAsStateWithLifecycle().value ?: 4096
                )
            }
            items(1) { item ->
                JavaVersionSection(
                    requiredJava = viewModel.requiredJavaVersion.collectAsStateWithLifecycle().value ?: 21,
                    javaOverride = viewModel.javaOverride,
                    onOverrideChange = { viewModel.javaOverride.value = it }
                )
            }
            items(1) { item ->
                RenderBackendSection(
                    selectedBackend = selectedBackend,
                    onBackendChange = { viewModel.selectedBackend.value = it }
                )
            }
            items(1) { item ->
                AdvancedArgsSection(
                    advancedArgs = advancedArgs,
                    onArgsChange = { viewModel.advancedArgs.value = it },
                    generatedArgs = viewModel.generatedArgs.collectAsStateWithLifecycle().value ?: ""
                )
            }
            items(1) { item ->
                IconPickerSection(
                    iconPath = viewModel.iconPath.collectAsStateWithLifecycle().value,
                    onIconSelect = { viewModel.iconPath.value = it }
                )
            }
        }
    }
}

@Composable
fun InstanceNameSection(
    name: String,
    onNameChange: (String) -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Instance Name", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("My Awesome Instance") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
fun VersionPickerSection(
    selectedVersion: String?,
    onVersionClick: (String) -> Unit,
    versions: List<VersionInfo>,
    isLoading: Boolean
) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Game Version", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                if (isLoading) androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(20.dp))
            }
            if (versions.isEmpty() && !isLoading) {
                Text("No versions found", fontSize = 14.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(versions.groupBy { it.type }.toList().sortedBy { it.first.ordinal }) { (type, list) ->
                        item {
                            Text(type.label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                        }
                        items(list) { version ->
                            androidx.compose.material3.ListItem(
                                onClick = { onVersionClick(version.id) },
                                headlineContent = { Text(version.id, fontSize = 14.sp) },
                                supportingContent = { Text(version.releaseDate, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) },
                                trailingContent = {
                                    if (selectedVersion == version.id) Icon(Icons.Default.Check, contentDescription = "Selected", tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (selectedVersion == version.id) androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer else Color.Transparent, shape = RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModloaderPickerSection(
    selectedModloader: ModloaderType,
    selectedModloaderVersion: String?,
    onModloaderChange: (ModloaderType) -> Unit,
    onModloaderVersionChange: (String?) -> Unit,
    modloaderVersions: List<String>,
    isLoading: Boolean
) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Modloader", fontSize = 16.sp, fontWeight = FontWeight.Medium)

            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModloaderType.values().forEach { loader ->
                    androidx.compose.material3.Chip(
                        onClick = { onModloaderChange(loader) },
                        selected = selectedModloader == loader,
                        onClick = { onModloaderChange(loader) },
                        label = { Text(loader.name) }
                    )
                }
            }

            if (selectedModloader != ModloaderType.VANILLA) {
                if (isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.fillMaxWidth().padding(16.dp))
                } else if (modloaderVersions.isEmpty()) {
                    Text("No compatible loader versions found", fontSize = 14.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    androidx.compose.foundation.layout.Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        modloaderVersions.forEach { version ->
                            androidx.compose.material3.Chip(
                                onClick = { onModloaderVersionChange(version) },
                                selected = selectedModloaderVersion == version,
                                onClick = { onModloaderVersionChange(version) },
                                label = { Text(version) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RamAllocationSection(
    ramMin: Int,
    ramMax: Int,
    onMinChange: (Int) -> Unit,
    onMaxChange: (Int) -> Unit,
    maxAllowed: Int
) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("RAM Allocation", fontSize = 16.sp, fontWeight = FontWeight.Medium)

            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Min: ${ramMin} MB", fontSize = 14.sp)
                    Slider(
                        value = ramMin.toFloat(),
                        onValueChange = { onMinChange(it.toInt()) },
                        valueRange = 512f..ramMax.toFloat(),
                        steps = (ramMax - 512) / 256
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Max: ${ramMax} MB", fontSize = 14.sp)
                    Slider(
                        value = ramMax.toFloat(),
                        onValueChange = { onMaxChange(it.toInt()) },
                        valueRange = ramMin.toFloat()..maxAllowed.toFloat(),
                        steps = (maxAllowed - ramMin) / 256
                    )
                }
            }
            Text("Safe range: 512 MB - $maxAllowed MB", fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun JavaVersionSection(
    requiredJava: Int,
    javaOverride: androidx.compose.runtime.MutableState<Int?>,
    onOverrideChange: (Int?) -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Java Version", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text("Required: Java $requiredJava", fontSize = 14.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
            }
            androidx.compose.material3.Chip(
                selected = javaOverride.value != null,
                onClick = { onOverrideChange(if (javaOverride.value != null) null else requiredJava) },
                label = {
                    Text(if (javaOverride.value != null) "Override: Java ${javaOverride.value}" else "Auto (Java $requiredJava)")
                }
            )
        }
    }
}

@Composable
fun RenderBackendSection(
    selectedBackend: RenderBackend,
    onBackendChange: (RenderBackend) -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Render Backend", fontSize = 16.sp, fontWeight = FontWeight.Medium)

            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RenderBackend.values().forEach { backend ->
                    androidx.compose.material3.Chip(
                        onClick = { onBackendChange(backend) },
                        selected = selectedBackend == backend,
                        onClick = { onBackendChange(backend) },
                        label = { Text(backend.label) }
                    )
                }
            }

            Text(backendDescription(selectedBackend), fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AdvancedArgsSection(
    advancedArgs: String,
    onArgsChange: (String) -> Unit,
    generatedArgs: String
) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Advanced JVM Args", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Button(onClick = { onArgsChange(generatedArgs) }) {
                    Text("Reset to Default")
                }
            }
            androidx.compose.material3.TextField(
                value = advancedArgs,
                onValueChange = onArgsChange,
                modifier = Modifier.fillMaxWidth().height(120.dp),
                singleLine = false,
                keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions.Default.copy(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                )
            )
            Text("User edits are preserved and merged with regenerated defaults", fontSize = 11.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun IconPickerSection(
    iconPath: String?,
    onIconSelect: (String?) -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Instance Icon", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Avatar(skinUrl = iconPath, size = 96.dp)
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { /* TODO: Open image picker */ }) {
                    Text("Choose Image")
                }
                Button(onClick = { onIconSelect(null) }) {
                    Text("Reset")
                }
            }
        }
    }
}

data class VersionInfo(
    val id: String,
    val type: VersionType,
    val releaseDate: String
)

enum class VersionType(val label: String, val ordinal: Int) {
    RELEASE("Release", 0),
    SNAPSHOT("Snapshot", 1),
    OLD_BETA("Old Beta", 2),
    OLD_ALPHA("Old Alpha", 3)
}

private fun backendDescription(backend: dev.whaltermc.veyra.data.model.RenderBackend): String = when (backend) {
    dev.whaltermc.veyra.data.model.RenderBackend.AUTO -> "Automatically select the best backend for your device (recommended)"
    dev.whaltermc.veyra.data.model.RenderBackend.PANVK_NATIVE -> "Native Vulkan via FristOneRR PanVK driver. Best on Mali GPUs with Vulkan-supporting mods."
    dev.whaltermc.veyra.data.model.RenderBackend.ZINK_PANVK -> "OpenGL-on-Vulkan via Zink + PanVK. Good for vanilla/modded Minecraft on Mali."
    dev.whaltermc.veyra.data.model.RenderBackend.RUSTGL -> "Custom OpenGL-to-GLES translation. Fallback for non-Mali or incompatible setups."
    dev.whaltermc.veyra.data.model.RenderBackend.GL4ES -> "Generic OpenGL-to-GLES translation. Maximum compatibility, lowest performance."
}