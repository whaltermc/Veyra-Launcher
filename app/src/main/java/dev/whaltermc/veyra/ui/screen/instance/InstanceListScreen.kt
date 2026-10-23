package dev.whaltermc.veyra.ui.screen.instance

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.input.pointer.detectTapGestures
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntSize
import dev.whaltermc.veyra.VeyraApplication
import dev.whaltermc.veyra.data.model.InstanceEntity
import dev.whaltermc.veyra.data.model.ModloaderType
import dev.whaltermc.veyra.data.model.RenderBackend
import dev.whaltermc.veyra.ui.component.Avatar
import dev.whaltermc.veyra.ui.component.ConfirmDialog
import dev.whaltermc.veyra.ui.component.ModloaderBadge
import dev.whaltermc.veyra.ui.component.BackendIndicator
import dev.whaltermc.veyra.ui.component.DropdownMenuButton
import dev.whaltermc.veyra.ui.component.SortBy
import dev.whaltermc.veyra.ui.theme.Theme.Veyra
import dev.whaltermc.veyra.viewmodel.InstanceListViewModel
import kotlinx.coroutines.launch
import dev.whaltermc.veyra.R
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.vector.icons.Icons
import androidx.compose.ui.graphics.vector.icons.filled.Add
import androidx.compose.ui.graphics.vector.icons.filled.Delete
import androidx.compose.ui.graphics.vector.icons.filled.Edit
import androidx.compose.ui.graphics.vector.icons.filled.FilterList
import androidx.compose.ui.graphics.vector.icons.filled.GridView
import androidx.compose.ui.graphics.vector.icons.filled.Info
import androidx.compose.ui.graphics.vector.icons.filled.Launch
import androidx.compose.ui.graphics.vector.icons.filled.List
import androidx.compose.ui.graphics.vector.icons.filled.MoreVert
import androidx.compose.ui.graphics.vector.icons.filled.Person
import androidx.compose.ui.graphics.vector.icons.filled.PlayArrow
import androidx.compose.ui.graphics.vector.icons.filled.Search
import androidx.compose.ui.graphics.vector.icons.filled.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import dev.whaltermc.veyra.auth.AuthManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstanceListScreen(
    onNewInstance: () -> Unit,
    onInstanceClick: (String) -> Unit,
    onInstanceLongClick: (String) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as VeyraApplication
    val viewModel: InstanceListViewModel = viewModel(factory = InstanceListViewModel.Factory(app))
    val scope = rememberCoroutineScope()

    var showDeleteConfirm by remember { mutableStateOf<InstanceEntity?>(null) }
    var showExportConfirm by remember { mutableStateOf<InstanceEntity?>(null) }
    var expandedMenuId by remember { mutableStateOf<String?>(null) }

    val instances = viewModel.filteredInstances.collectAsStateWithLifecycle().value ?: emptyList()
    val isGridView = viewModel.isGridView.collectAsStateWithLifecycle().value ?: true
    val searchText by viewModel.searchText
    val sortBy = viewModel.sortBy.collectAsStateWithLifecycle().value ?: SortBy.LAST_PLAYED

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        TopAppBar(
            title = {
                if (searchText.isNotBlank()) {
                    Text(searchText, fontSize = 18.sp)
                } else {
                    Text(stringResource(R.string.app_name))
                }
            },
            navigationIcon = {
                IconButton(onClick = { /* Open drawer or account switcher */ }) {
                    Icon(Icons.Default.Person, contentDescription = "Account")
                }
            },
            actions = {
                // Search
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { viewModel.searchText.value = it },
                    label = { Text(stringResource(R.string.search)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier.width(200.dp),
                    singleLine = true,
                    colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
                    )
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))

                // View toggle
                IconButton(onClick = { viewModel.toggleView() }) {
                    Icon(if (isGridView) Icons.Default.List else Icons.Default.GridView, contentDescription = "Toggle view")
                }

                // Sort
                DropdownMenuButton(
                    selected = sortBy,
                    onSelect = { viewModel.setSortBy(it) }
                )

                // Settings
                IconButton(onClick = { /* Navigate to settings */ }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
            )
        )

        if (instances.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.mipmap.ic_launcher),
                    contentDescription = "",
                    modifier = Modifier.size(128.dp)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))
                Text(stringResource(R.string.no_instances), fontSize = 24.sp, fontWeight = FontWeight.Medium)
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(8.dp))
                Text(stringResource(R.string.create_first_instance), fontSize = 16.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))
                Button(onClick = onNewInstance) {
                    Text(stringResource(R.string.create_instance))
                }
            }
        } else {
            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 180.dp),
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(instances) { instance ->
                        InstanceGridCard(
                            instance = instance,
                            onClick = { onInstanceClick(instance.id) },
                            onLongClick = { onInstanceLongClick(instance.id) },
                            onMenuClick = { expandedMenuId = if (expandedMenuId == instance.id) null else instance.id }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(instances) { instance ->
                        InstanceListCard(
                            instance = instance,
                            onClick = { onInstanceClick(instance.id) },
                            onLongClick = { onInstanceLongClick(instance.id) },
                            onMenuClick = { expandedMenuId = if (expandedMenuId == instance.id) null else instance.id }
                        )
                    }
                }
            }
        }

        // Floating action button for new instance
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            androidx.compose.material3.FloatingActionButton(
                onClick = onNewInstance,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Instance")
            }
        }
    }

    // Delete confirmation
    showDeleteConfirm?.let { instance ->
        ConfirmDialog(
            title = stringResource(R.string.confirm_delete),
            message = "Delete \"${instance.name}\"?",
            onConfirm = {
                scope.launch { viewModel.deleteInstance(instance.id, wipeSaves = false) }
            },
            onDismiss = { showDeleteConfirm = null },
            isDestructive = true
        )
    }

    showExportConfirm?.let { instance ->
        ConfirmDialog(
            title = stringResource(R.string.export),
            message = "Export \"${instance.name}\" as a shareable file?",
            onConfirm = {
                scope.launch { viewModel.exportInstance(instance.id) }
            },
            onDismiss = { showExportConfirm = null }
        )
    }
}

@Composable
fun InstanceGridCard(
    instance: InstanceEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .fillMaxSize(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background image/icon
            Box(modifier = Modifier.fillMaxSize()) {
                if (instance.iconPath != null) {
                    // Load custom icon
                    AsyncImage(
                        model = instance.iconPath!!,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Auto-generated from modloader
                    ModloaderBackground(modloader = instance.modloaderType)
                }
            }

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .clip(RoundedCornerShape(12.dp))
            )

            // Content
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.Start
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(instance.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = androidx.compose.ui.text.TextOverflow.Ellipsis)
                        Text(instance.gameVersion, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                    ModloaderBadge(modloader = instance.modloaderType, version = instance.modloaderVersion)
                }

                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    androidx.compose.foundation.layout.Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        BackendIndicator(backend = instance.renderBackendOverride)
                        if (instance.isPinned) {
                            Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(
                        instance.lastPlayedAt?.let { formatRelativeTime(it) } ?: "Never played",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Menu button
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun InstanceListCard(
    instance: InstanceEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(8.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .height(72.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(
                skinUrl = instance.iconPath,
                size = 48.dp
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(instance.name, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = androidx.compose.ui.text.TextOverflow.Ellipsis)
                    ModloaderBadge(modloader = instance.modloaderType, version = instance.modloaderVersion)
                    BackendIndicator(backend = instance.renderBackendOverride)
                }
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(instance.gameVersion, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(instance.lastPlayedAt?.let { formatRelativeTime(it) } ?: "Never played", fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    if (instance.isPinned) {
                        androidx.compose.foundation.layout.Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Text("Pinned", fontSize = 10.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }
        }
    }
}