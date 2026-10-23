package dev.whaltermc.veyra.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PorterDuffColorFilter
import androidx.compose.ui.graphics.PorterDuff
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import dev.whaltermc.veyra.R
import dev.whaltermc.veyra.ui.theme.Theme.Veyra
import dev.whaltermc.veyra.data.model.ModloaderType
import dev.whaltermc.veyra.data.model.RenderBackend
import androidx.compose.ui.graphics.vector.icons.filled.Check
import androidx.compose.ui.graphics.vector.icons.filled.Close
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun Avatar(
    skinUrl: String?,
    size: androidx.compose.ui.unit.Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            modifier = Modifier.size(size),
            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
        ) {
            if (skinUrl != null && skinUrl.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(skinUrl)
                        .transformations(CircleCropTransformation())
                        .build(),
                    contentDescription = "Player skin",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(size),
                    placeholder = { painterResource(R.mipmap.ic_launcher).let { painter ->
                        Image(painter = painter, contentDescription = "", contentScale = ContentScale.Crop, modifier = Modifier.size(size))
                    } },
                    error = { painterResource(R.mipmap.ic_launcher).let { painter ->
                        Image(painter = painter, contentDescription = "", contentScale = ContentScale.Crop, modifier = Modifier.size(size))
                    } },
                    fallback = { painterResource(R.mipmap.ic_launcher).let { painter ->
                        Image(painter = painter, contentDescription = "", contentScale = ContentScale.Crop, modifier = Modifier.size(size))
                    } }
                )
            } else {
                // Default Steve/Alex head
                Image(
                    painter = painterResource(R.mipmap.ic_launcher),
                    contentDescription = "Default skin",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(size)
                )
            }
        }
    }
}

@Composable
fun LoadingOverlay(visible: Boolean) {
    if (visible) {
        androidx.compose.material3.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Card(
                modifier = Modifier.padding(32.dp),
                elevation = 8.dp
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp
                    )
                    Text("Loading…", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ErrorMessage(message: String, onDismiss: () -> Unit) {
    androidx.compose.material3.Snackbar(
        modifier = Modifier.fillMaxWidth(),
        action = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text("Dismiss") } },
        dismissAction = null,
        visuals = {
            androidx.compose.material3.Text(text = message, color = androidx.compose.material3.MaterialTheme.colorScheme.onErrorContainer)
        },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.errorContainer
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    isDestructive: Boolean = false
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium) },
        text = { Text(message) },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = { onConfirm(); onDismiss() },
                colors = if (isDestructive) androidx.compose.material3.ButtonDefaults.textButtonColors(
                    contentColor = androidx.compose.material3.MaterialTheme.colorScheme.error
                ) else null
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}

@Composable
fun ModloaderBadge(modloader: ModloaderType, version: String?) {
    val color = when (modloader) {
        ModloaderType.VANILLA -> androidx.compose.material3.MaterialTheme.colorScheme.primary
        ModloaderType.FABRIC -> Color(0xFFD32F2F)
        ModloaderType.QUILT -> Color(0xFF7B1FA2)
        ModloaderType.FORGE -> Color(0xFFF57C00)
        ModloaderType.NEOFORGE -> Color(0xFF009688)
    }
    val text = version?.let { "${modloader.name} $it" } ?: modloader.name
    androidx.compose.material3.Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = color,
        modifier = Modifier
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .background(color.copy(alpha = 0.15f), shape = androidx.compose.material3.MaterialTheme.shapes.small)
    )
}

@Composable
fun BackendIndicator(backend: RenderBackend) {
    val (label, color) = when (backend) {
        RenderBackend.AUTO -> "Auto" to androidx.compose.material3.MaterialTheme.colorScheme.outline
        RenderBackend.PANVK_NATIVE -> "PanVK" to Color(0xFF00BCD4)
        RenderBackend.ZINK_PANVK -> "Zink" to Color(0xFF9C27B0)
        RenderBackend.RUSTGL -> "RustGL" to Color(0xFFFF9800)
        RenderBackend.GL4ES -> "GL4ES" to Color(0xFF795548)
    }
    androidx.compose.material3.Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = color,
        modifier = Modifier
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .background(color.copy(alpha = 0.15f), shape = androidx.compose.material3.MaterialTheme.shapes.small)
    )
}

@Composable
fun ModloaderBackground(modloader: ModloaderType) {
    val color = when (modloader) {
        ModloaderType.VANILLA -> androidx.compose.material3.MaterialTheme.colorScheme.primary
        ModloaderType.FABRIC -> Color(0xFFD32F2F)
        ModloaderType.QUILT -> Color(0xFF7B1FA2)
        ModloaderType.FORGE -> Color(0xFFF57C00)
        ModloaderType.NEOFORGE -> Color(0xFF009688)
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = color.copy(alpha = 0.3f))
        }
        Text(modloader.name, fontSize = 48.sp, fontWeight = FontWeight.Bold, color = color.copy(alpha = 0.5f))
    }
}

enum class SortBy(val label: String) {
    NAME("Name"),
    LAST_PLAYED("Last Played"),
    PLAY_TIME("Play Time"),
    DATE_CREATED("Date Created")
}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuButton(
    selected: SortBy,
    onSelect: (SortBy) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = !expanded }) {
        Icon(Icons.Default.Sort, contentDescription = "Sort")
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        SortBy.values().forEach { sort ->
            DropdownMenuItem(
                text = { Text(sort.label) },
                onClick = {
                    onSelect(sort)
                    expanded = false
                }
            )
        }
    }
}