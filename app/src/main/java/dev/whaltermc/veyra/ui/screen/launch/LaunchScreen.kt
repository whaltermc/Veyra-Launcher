package dev.whaltermc.veyra.ui.screen.launch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Column
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
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
import dev.whaltermc.veyra.launch.LaunchManager
import dev.whaltermc.veyra.launch.LaunchManager.LaunchStage
import dev.whaltermc.veyra.ui.theme.Theme.Veyra
import kotlinx.coroutines.launch
import dev.whaltermc.veyra.R
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.vector.icons.Icons
import androidx.compose.ui.graphics.vector.icons.filled.CheckCircle
import androidx.compose.ui.graphics.vector.icons.filled.Close
import androidx.compose.ui.graphics.vector.icons.filled.Error
import androidx.compose.ui.graphics.vector.icons.filled.PlayArrow
import androidx.compose.ui.platform.LocalContext
import dev.whaltermc.veyra.ui.component.LoadingOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaunchScreen(
    instanceId: String,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as VeyraApplication
    val viewModel: LaunchViewModel = viewModel(factory = LaunchViewModel.Factory(app, instanceId))

    val currentStage by viewModel.currentStage
    val progress by viewModel.progress
    val error by viewModel.error
    val isComplete by viewModel.isComplete

    LaunchedEffect(Unit) {
        viewModel.startLaunch()
    }

    LaunchedEffect(isComplete) {
        if (isComplete) {
            // Launch successful - game process started
            // The Compose UI should unmount now (handled by activity)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .width(400.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App icon/name
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = androidx.compose.ui.res.R.mipmap.ic_launcher),
                        contentDescription = "Veyra",
                        modifier = Modifier.size(80.dp)
                    )
                    Text("Veyra Launcher", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }

                // Stage progress
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LaunchStage.values().forEachIndexed { index, stage ->
                        StageRow(
                            stage = stage,
                            isCurrent = currentStage == stage,
                            isComplete = index < LaunchStage.values().indexOf(currentStage) || (isComplete && index < LaunchStage.values().size),
                            progress = if (currentStage == stage) progress else (if (index < LaunchStage.values().indexOf(currentStage)) 1f else 0f)
                        )
                    }
                }

                // Overall progress bar
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    trackColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
                )

                // Status text
                Text(
                    text = currentStage.label,
                    fontSize = 14.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // Error state
                error?.let { msg ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Error, contentDescription = "Error", tint = androidx.compose.material3.MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
                            Text("Launch Failed", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                            Text(msg, fontSize = 14.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Button(onClick = onExit) {
                                Text("Back to Launcher")
                            }
                        }
                    }
                }

                // Complete state
                if (isComplete && error == null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Text("Launching Minecraft…", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("The game will open shortly", fontSize = 14.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun StageRow(
    stage: LaunchStage,
    isCurrent: Boolean,
    isComplete: Boolean,
    progress: Float
) {
    val (icon, color) = when {
        isComplete -> Icons.Default.CheckCircle to androidx.compose.material3.MaterialTheme.colorScheme.primary
        isCurrent -> Icons.Default.PlayArrow to androidx.compose.material3.MaterialTheme.colorScheme.secondary
        else -> Icons.Default.RadioButtonUnchecked to androidx.compose.material3.MaterialTheme.colorScheme.outline
    }

    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(stage.label, fontSize = 14.sp, color = if (isCurrent || isComplete) Color.Unspecified else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = color,
                trackColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}