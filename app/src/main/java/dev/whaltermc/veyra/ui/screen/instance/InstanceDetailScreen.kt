package dev.whaltermc.veyra.ui.screen.instance

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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import dev.whaltermc.veyra.data.model.InstanceEntity
import dev.whaltermc.veyra.ui.component.Avatar
import dev.whaltermc.veyra.ui.theme.Theme.Veyra
import dev.whaltermc.veyra.viewmodel.InstanceDetailViewModel
import kotlinx.coroutines.launch
import dev.whaltermc.veyra.R
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.vector.icons.Icons
import androidx.compose.ui.graphics.vector.icons.filled.ArrowBack
import androidx.compose.ui.graphics.vector.icons.filled.Delete
import androidx.compose.ui.graphics.vector.icons.filled.Edit
import androidx.compose.ui.graphics.vector.icons.filled.Folder
import androidx.compose.ui.graphics.vector.icons.filled.FolderOpen
import androidx.compose.ui.graphics.vector.icons.filled.Layers
import androidx.compose.ui.graphics.vector.icons.filled.PlayArrow
import androidx.compose.ui.graphics.vector.icons.filled.Settings
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstanceDetailScreen(
    instanceId: String,
    isDetailView: Boolean,
    onLaunch: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as VeyraApplication
    val viewModel: InstanceDetailViewModel = viewModel(factory = InstanceDetailViewModel.Factory(app, instanceId))
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) }

    val instance = viewModel.instance.collectAsStateWithLifecycle().value

    instance?.let { inst ->
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            TopAppBar(
                title = { Text(inst.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isDetailView) {
                        Button(onClick = onLaunch) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                            Text("Play")
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                    }
                    IconButton(onClick = { /* Edit */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { /* Menu */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
                )
            )

            if (isDetailView) {
                // Tab row
                TabRow(
                    selectedTabIndex = selectedTab,
                    onTabClick = { selectedTab = it },
                    modifier = Modifier.fillMaxWidth(),
                    indicator = { tabPositions ->
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .offset(x = tabPositions[selectedTab].left)
                                .width(tabPositions[selectedTab].width)
                                .height(3.dp)
                                .background(androidx.compose.material3.MaterialTheme.colorScheme.primary)
                        )
                    }
                ) {
                    tabs.forEach { (title, icon) ->
                        Tab(
                            text = { Text(title) },
                            icon = { Icon(icon, contentDescription = null) },
                            selected = false,
                            onClick = { }
                        )
                    }
                }

                // Tab content
                androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                    when (selectedTab) {
                        0 -> OverviewTab(instance = inst, onLaunch = onLaunch)
                        1 -> ModsTab(instanceId = inst.id)
                        2 -> ResourcePacksTab(instanceId = inst.id)
                        3 -> ShaderPacksTab(instanceId = inst.id)
                        4 -> WorldsTab(instanceId = inst.id)
                        5 -> LogsTab(instanceId = inst.id)
                        6 -> ScreenshotsTab(instanceId = inst.id)
                    }
                }
            } else {
                // Quick overview + play button
                OverviewTab(instance = inst, onLaunch = onLaunch, showPlayButton = true)
            }
        }
    } ?: androidx.compose.runtime.ComposableSingletons$InstanceDetailScreenKt1.EmptyPlaceholder()
}

@Composable
fun OverviewTab(
    instance: InstanceEntity,
    onLaunch: () -> Unit,
    showPlayButton: Boolean = false
) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Avatar(skinUrl = instance.iconPath, size = 80.dp)

                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(instance.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(instance.gameVersion, fontSize = 16.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                        androidx.compose.foundation.layout.Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ModloaderBadge(modloader = instance.modloaderType, version = instance.modloaderVersion)
                            BackendIndicator(backend = instance.renderBackendOverride)
                        }
                    }
                }

                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    StatItem("Last Played", instance.lastPlayedAt?.let { formatRelativeTime(it) } ?: "Never")
                    StatItem("Total Play Time", formatPlayTime(instance.totalPlayTimeSeconds))
                    StatItem("Created", instance.createdAt.toString().take(10))
                }

                if (showPlayButton) {
                    Button(onClick = onLaunch, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Text("Play", fontSize = 18.sp)
                    }
                }
            }
        }

        // Quick access cards
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tabs.drop(1)) { (title, icon) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { /* Navigate to tab */ }
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.padding(16.dp).height(56.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun ModsTab(instanceId: String) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Mods", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Mod manager coming soon…", fontSize = 16.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ResourcePacksTab(instanceId: String) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Resource Packs", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Resource pack manager coming soon…", fontSize = 16.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ShaderPacksTab(instanceId: String) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Shader Packs", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Shader pack manager coming soon…", fontSize = 16.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun WorldsTab(instanceId: String) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Worlds", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("World manager coming soon…", fontSize = 16.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun LogsTab(instanceId: String) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Logs", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Log viewer coming soon…", fontSize = 16.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ScreenshotsTab(instanceId: String) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Screenshots", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Screenshot gallery coming soon…", fontSize = 16.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun StatItem(label: String, value: String) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(label, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

private val tabs = listOf(
    "Overview" to Icons.Default.FolderOpen,
    "Mods" to Icons.Default.Layers,
    "Resource Packs" to Icons.Default.Folder,
    "Shader Packs" to Icons.Default.Layers,
    "Worlds" to Icons.Default.Folder,
    "Logs" to Icons.Default.Folder,
    "Screenshots" to Icons.Default.Folder
)

private fun formatRelativeTime(instant: kotlinx.datetime.Instant): String {
    val now = kotlinx.datetime.Clock.System.now()
    val diff = now - instant
    return when {
        diff.inWholeMinutes < 1 -> "Just now"
        diff.inWholeMinutes < 60 -> "${diff.inWholeMinutes}m ago"
        diff.inWholeHours < 24 -> "${diff.inWholeHours}h ago"
        diff.inWholeDays < 7 -> "${diff.inWholeDays}d ago"
        else -> instant.toString().take(10)
    }
}

private fun formatPlayTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}