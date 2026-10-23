package dev.whaltermc.veyra.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.whaltermc.veyra.VeyraApplication
import dev.whaltermc.veyra.data.model.RenderBackend
import dev.whaltermc.veyra.ui.theme.Theme.Veyra
import dev.whaltermc.veyra.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import dev.whaltermc.veyra.R
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.vector.icons.Icons
import androidx.compose.ui.graphics.vector.icons.filled.ArrowBack
import androidx.compose.ui.graphics.vector.icons.filled.Delete
import androidx.compose.ui.graphics.vector.icons.filled.Download
import androidx.compose.ui.graphics.vector.icons.filled.Info
import androidx.compose.ui.graphics.vector.icons.filled.Refresh
import androidx.compose.ui.graphics.vector.icons.filled.Storage
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as VeyraApplication
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(app))
    val scope = rememberCoroutineScope()

    var expandedMenu by remember { mutableStateOf<RenderBackend?>(null) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.settings)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Render section
            items(1) { SettingsSection(title = "Render", icon = Icons.Default.Info) {
                SettingRow(
                    title = "Default Render Backend",
                    summary = "Automatically select the best backend for your device",
                    trailing = {
                        DropdownMenuButton(
                            selected = viewModel.defaultBackend.collectAsStateWithLifecycle().value ?: RenderBackend.AUTO,
                            options = RenderBackend.values(),
                            onSelect = { viewModel.setDefaultBackend(it) }
                        )
                    }
                )
                SettingRow(
                    title = "Re-run Device Benchmark",
                    summary = "Re-benchmark all render backends on this device",
                    trailing = {
                        Button(onClick = { scope.launch { viewModel.runBenchmark() } }) {
                            Text("Run Benchmark")
                        }
                    }
                )
                SettingRow(
                    title = "Debug Overlay Default",
                    summary = "Show FPS/backend overlay by default in-game",
                    trailing = {
                        Switch(
                            checked = viewModel.debugOverlayDefault.collectAsStateWithLifecycle().value ?: false,
                            onCheckedChange = { viewModel.setDebugOverlayDefault(it) }
                        )
                    }
                )
                SettingRow(
                    title = "Shader Pipeline Cache",
                    summary = "Manage compiled shader cache",
                    trailing = {
                        Button(onClick = { scope.launch { viewModel.clearPipelineCache() } }) {
                            Text("Clear Cache")
                        }
                    }
                )
            }}

            // Performance section
            items(1) { SettingsSection(title = "Performance", icon = Icons.Default.Download) {
                SettingRow(
                    title = "Background Download Throttling",
                    summary = "Limit download speed while game is running",
                    trailing = {
                        Switch(
                            checked = viewModel.downloadThrottle.collectAsStateWithLifecycle().value ?: true,
                            onCheckedChange = { viewModel.setDownloadThrottle(it) }
                        )
                    }
                )
                SettingRow(
                    title = "Pause Downloads on Mobile Data",
                    summary = "Don't download assets/mods on metered connections",
                    trailing = {
                        Switch(
                            checked = viewModel.pauseMobileData.collectAsStateWithLifecycle().value ?: true,
                            onCheckedChange = { viewModel.setPauseMobileData(it) }
                        )
                    }
                )
                SettingRow(
                    title = "Default Render Distance",
                    summary = "Applied to new instances",
                    trailing = {
                        OutlinedTextField(
                            value = "${viewModel.defaultRenderDistance.collectAsStateWithLifecycle().value ?: 12}",
                            onValueChange = { viewModel.setDefaultRenderDistance(it.toIntOrNull() ?: 12) },
                            modifier = Modifier.width(80.dp),
                            singleLine = true
                        )
                    }
                )
            }}

            // Storage section
            items(1) { SettingsSection(title = "Storage", icon = Icons.Default.Storage) {
                SettingRow(
                    title = "Clear Unused Assets",
                    summary = "Remove cached files not used by any instance",
                    trailing = {
                        Button(onClick = { scope.launch { viewModel.clearUnusedAssets() } }) {
                            Text("Clear")
                        }
                    }
                )
                SettingRow(
                    title = "Clear Logs",
                    summary = "Delete all instance log files",
                    trailing = {
                        Button(onClick = { scope.launch { viewModel.clearLogs() } }) {
                            Text("Clear")
                        }
                    }
                )
            }}

            // Controls section
            items(1) { SettingsSection(title = "Controls", icon = Icons.Default.Info) {
                SettingRow(
                    title = "Physical Input Auto-Switch",
                    summary = "Auto-switch to keyboard/mouse when connected",
                    trailing = {
                        Switch(
                            checked = viewModel.physicalInputAutoSwitch.collectAsStateWithLifecycle().value ?: true,
                            onCheckedChange = { viewModel.setPhysicalInputAutoSwitch(it) }
                        )
                    }
                )
            }}

            // Diagnostics section
            items(1) { SettingsSection(title = "Diagnostics", icon = Icons.Default.Info) {
                SettingRow(
                    title = "Export Diagnostic Bundle",
                    summary = "Create a shareable diagnostic file for bug reports",
                    trailing = {
                        Button(onClick = { scope.launch { viewModel.exportDiagnosticBundle() } }) {
                            Text("Export")
                        }
                    }
                )
            }}

            // About section
            items(1) { SettingsSection(title = "About", icon = Icons.Default.Info) {
                SettingRow(
                    title = "Version",
                    summary = "1.0.0",
                    trailing = {}
                )
                SettingRow(
                    title = "Licenses",
                    summary = "View open-source licenses",
                    trailing = {
                        Button(onClick = { /* Show licenses */ }) {
                            Text("View")
                        }
                    }
                )
            }}
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Divider()
            content()
        }
    }
}

@Composable
fun SettingRow(
    title: String,
    summary: String,
    trailing: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(title, fontSize = 16.sp)
            Text(summary, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing()
    }
}

@Composable
fun DropdownMenuButton(
    selected: RenderBackend,
    options: Array<RenderBackend>,
    onSelect: (RenderBackend) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    androidx.compose.material3.Menu(
        onClick = { expanded = !expanded },
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        options.forEach { option ->
            androidx.compose.material3.DropdownMenuItem(
                onClick = { onSelect(option); expanded = false },
                content = { Text(option.label) }
            )
        }
    }
}