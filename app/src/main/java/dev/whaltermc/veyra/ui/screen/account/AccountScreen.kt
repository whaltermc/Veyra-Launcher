package dev.whaltermc.veyra.ui.screen.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.whaltermc.veyra.VeyraApplication
import dev.whaltermc.veyra.data.model.AccountEntity
import dev.whaltermc.veyra.data.model.AuthType
import dev.whaltermc.veyra.ui.theme.Theme.Veyra
import dev.whaltermc.veyra.viewmodel.AccountViewModel
import kotlinx.coroutines.launch
import dev.whaltermc.veyra.R
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.vector.icons.Icons
import androidx.compose.ui.graphics.vector.icons.filled.Add
import androidx.compose.ui.graphics.vector.icons.filled.ArrowForward
import androidx.compose.ui.graphics.vector.icons.filled.Check
import androidx.compose.ui.graphics.vector.icons.filled.Close
import androidx.compose.ui.graphics.vector.icons.filled.ContentCopy
import androidx.compose.ui.graphics.vector.icons.filled.Delete
import androidx.compose.ui.graphics.vector.icons.filled.Login
import androidx.compose.ui.graphics.vector.icons.filled.Person
import androidx.compose.ui.graphics.vector.icons.filled.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntSize
import dev.whaltermc.veyra.auth.AuthManager
import dev.whaltermc.veyra.ui.component.Avatar
import dev.whaltermc.veyra.ui.component.LoadingOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as VeyraApplication
    val viewModel: AccountViewModel = viewModel(factory = AccountViewModel.Factory(app))
    val scope = rememberCoroutineScope()

    var showAddAccount by remember { mutableStateOf(false) }
    var authFlowState by remember { mutableStateOf<AuthFlowState>(AuthFlowState.Idle) }

    val accounts = viewModel.accounts.collectAsStateWithLifecycle().value ?: emptyList()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.accounts)) },
            actions = {
                IconButton(onClick = { showAddAccount = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add account")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
            )
        )

        if (accounts.isEmpty() && !showAddAccount) {
            // Empty state - prompt to add first account
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No accounts yet", fontSize = 24.sp, fontWeight = FontWeight.Medium)
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))
                Button(onClick = { showAddAccount = true }) {
                    Text(stringResource(R.string.login_microsoft))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(accounts) { account ->
                    AccountCard(
                        account = account,
                        isActive = app.authManager.activeAccountId == account.id,
                        onClick = {
                            scope.launch { app.authManager.setActiveAccount(account.id) }
                        },
                        onRemove = {
                            scope.launch { app.authManager.removeAccount(account.id) }
                        },
                        onRefresh = {
                            scope.launch { app.authManager.refreshAccount(account.id) }
                        }
                    )
                }
                if (showAddAccount) {
                    item {
                        AddAccountSheet(
                            onClose = { showAddAccount = false },
                            onSuccess = {
                                showAddAccount = false
                                onLoginSuccess()
                            }
                        )
                    }
                }
            }
        }

        LoadingOverlay(visible = authFlowState != AuthFlowState.Idle)
    }
}

@Composable
fun AccountCard(
    account: AccountEntity,
    isActive: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (isActive)
                androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
            else
                androidx.compose.material3.MaterialTheme.colorScheme.surface
        )
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(
                skinUrl = account.skinUrl,
                size = 56.dp
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(account.displayName, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Text(account.uuid.take(8), fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    AuthTypeBadge(authType = account.authType)
                    if (isActive) {
                        Icon(Icons.Default.Check, contentDescription = "Active", tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                    }
                }
                Text("Last used: ${formatRelativeTime(account.lastUsed)}", fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }

            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = androidx.compose.material3.MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AuthTypeBadge(authType: AuthType) {
    val (text, color) = when (authType) {
        AuthType.MICROSOFT -> "Microsoft" to androidx.compose.material3.MaterialTheme.colorScheme.secondary
        AuthType.ELY_BY -> "Ely.by" to androidx.compose.material3.MaterialTheme.colorScheme.tertiary
        AuthType.OFFLINE -> "Offline" to androidx.compose.material3.MaterialTheme.colorScheme.outline
    }
    androidx.compose.material3.Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = color,
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .background(color.copy(alpha = 0.2f), shape = androidx.compose.material3.MaterialTheme.shapes.small)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountSheet(
    onClose: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as VeyraApplication
    val viewModel: AccountViewModel = viewModel(factory = AccountViewModel.Factory(app))

    var selectedFlow by remember { mutableStateOf<AuthFlow>(AuthFlow.Microsoft) }
    var offlineUsername by remember { mutableStateOf("") }
    var elybyUsername by remember { mutableStateOf("") }
    var elybyPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.material3.MaterialTheme.shapes.largeTop,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Add Account", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Auth method tabs
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AuthFlow.values().forEach { flow ->
                    androidx.compose.material3.Chip(
                        onClick = { selectedFlow = flow },
                        selected = selectedFlow == flow,
                        onClick = { selectedFlow = flow },
                        label = { Text(flow.label) }
                    )
                }
            }

            when (selectedFlow) {
                AuthFlow.Microsoft -> MicrosoftAuthFlow(
                    onSuccess = { onSuccess() },
                    onError = { errorMessage = it },
                    isLoading = isLoading
                )
                AuthFlow.ElyBy -> ElyByAuthFlow(
                    username = elybyUsername,
                    onUsernameChange = { elybyUsername = it },
                    password = elybyPassword,
                    onPasswordChange = { elybyPassword = it },
                    onLogin = {
                        isLoading = true
                        viewModel.loginElyBy(elybyUsername, elybyPassword)
                            .onSuccess { onSuccess() }
                            .onFailure { errorMessage = it.message ?: "Login failed" }
                            .onCompletion { isLoading = false }
                    },
                    isLoading = isLoading
                )
                AuthFlow.Offline -> OfflineAuthFlow(
                    username = offlineUsername,
                    onUsernameChange = { offlineUsername = it },
                    onLogin = {
                        viewModel.loginOffline(offlineUsername)
                            .onSuccess { onSuccess() }
                            .onFailure { errorMessage = it.message ?: "Login failed" }
                    },
                    isLoading = isLoading
                )
            }

            errorMessage?.let {
                Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun MicrosoftAuthFlow(
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    isLoading: Boolean
) {
    val context = LocalContext.current
    val app = context.applicationContext as VeyraApplication

    var deviceCode by remember { mutableStateOf<String?>(null) }
    var userCode by remember { mutableStateOf<String?>(null) }
    var verificationUrl by remember { mutableStateOf<String?>(null) }
    var interval by remember { mutableStateOf(5) }
    var expiresIn by remember { mutableStateOf(900) }
    var pollJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (deviceCode == null) {
            Button(
                onClick = {
                    app.authManager.startMicrosoftAuth()
                        .onSuccess { result ->
                            deviceCode = result.deviceCode
                            userCode = result.userCode
                            verificationUrl = result.verificationUri
                            interval = result.interval
                            expiresIn = result.expiresIn
                            startPolling()
                        }
                        .onFailure { onError(it.message ?: "Failed to start auth") }
                },
                enabled = !isLoading
            ) {
                Text(stringResource(R.string.login_microsoft))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Sign in at:", fontSize = 16.sp)
                Text(
                    text = AnnotatedString(verificationUrl ?: ""),
                    fontSize = 16.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    style = SpanStyle(textDecoration = TextDecoration.Underline)
                )
                Text("Enter code:", fontSize = 16.sp)
                Text(
                    text = userCode ?: "",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 8.sp
                )
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = {
                        userCode?.let { text ->
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Microsoft Auth Code", text))
                        }
                    }) {
                        Text("Copy Code")
                    }
                    Button(onClick = {
                        verificationUrl?.let { url ->
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                            context.startActivity(intent)
                        }
                    }) {
                        Text("Open Browser")
                    }
                }
                CircularProgressIndicator()
                Text("Waiting for sign-in… (expires in ${formatDuration(expiresIn)})", fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }

            androidx.compose.runtime.DisposableEffect(Unit) {
                onDispose { pollJob?.cancel() }
            }
        }
    }

    fun startPolling() {
        pollJob = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            var elapsed = 0
            while (elapsed < expiresIn) {
                kotlinx.coroutines.delay(interval * 1000L)
                elapsed += interval
                app.authManager.pollMicrosoftToken(deviceCode!!)
                    .onSuccess { tokens ->
                        app.authManager.completeMicrosoftAuth(tokens)
                            .onSuccess { onSuccess() }
                            .onFailure { onError(it.message ?: "Auth completion failed") }
                        return@launch
                    }
                    .onFailure {
                        if (it.message?.contains("authorization_pending") != true) {
                            onError(it.message ?: "Auth failed")
                            return@launch
                        }
                    }
            }
            onError("Authentication timed out")
        }
    }
}

@Composable
fun ElyByAuthFlow(
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    isLoading: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Username/Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = androidx.compose.foundation.text.PasswordVisualTransformation()
        )
        Button(onClick = onLogin, enabled = !isLoading && username.isNotBlank() && password.isNotBlank()) {
            if (isLoading) CircularProgressIndicator(color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp)) else Text(stringResource(R.string.login_elyby))
        }
    }
}

@Composable
fun OfflineAuthFlow(
    username: String,
    onUsernameChange: (String) -> Unit,
    onLogin: () -> Unit,
    isLoading: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Text(
            "Offline mode — multiplayer on most servers will not work.",
            fontSize = 12.sp,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onLogin, enabled = !isLoading && username.isNotBlank()) {
            if (isLoading) CircularProgressIndicator(color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp)) else Text(stringResource(R.string.login_offline))
        }
    }
}

enum class AuthFlow(val label: String) {
    Microsoft("Microsoft"),
    ElyBy("Ely.by"),
    Offline("Offline")
}

sealed class AuthFlowState {
    object Idle : AuthFlowState()
    data class InProgress(val message: String) : AuthFlowState()
    data class Success(val account: AccountEntity) : AuthFlowState()
    data class Error(val message: String) : AuthFlowState()
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

private fun formatDuration(seconds: Int): String {
    return when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
        else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
    }
}