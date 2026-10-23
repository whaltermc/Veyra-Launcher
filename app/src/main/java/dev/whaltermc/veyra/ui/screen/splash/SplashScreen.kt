package dev.whaltermc.veyra.ui.screen.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.whaltermc.veyra.ui.theme.Theme.Veyra

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    var navigateTo by remember { mutableStateOf<String?>(null) }

    // In a real app, this would be driven by ViewModel state
    // For now, simulate a short delay then navigate
    androidx.compose.runtime.LaunchedEffect(Unit) {
        try {
            // Simulate initialization work
            Thread.sleep(1500)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        // Check if we have a valid account - this would be real logic
        navigateTo = "home" // or "login"
    }

    androidx.compose.runtime.LaunchedEffect(navigateTo) {
        navigateTo?.let { target ->
            if (target == "login") onNavigateToLogin() else onNavigateToHome()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = androidx.compose.ui.res.R.mipmap.ic_launcher),
            contentDescription = "Veyra Launcher",
            modifier = Modifier.size(128.dp)
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(24.dp))
        Text(
            text = "Veyra",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(24.dp))
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))
        Text(
            text = "Loading…",
            fontSize = 16.sp,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}