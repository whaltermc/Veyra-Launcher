package dev.whaltermc.veyra.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4CAF50),
    primaryVariant = Color(0xFF388E3C),
    secondary = Color(0xFF2196F3),
    secondaryVariant = Color(0xFF1976D2),
    background = Color(0xFF1E1E1E),
    surface = Color(0xFF2C2C2C),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFBBBBBB),
    outline = Color(0xFF555555),
    error = Color(0xFFCF6679),
    onError = Color.Black,
    surfaceVariant = Color(0xFF3A3A3A),
    inverseSurface = Color(0xFFE0E0E0),
    inverseOnSurface = Color(0xFF1E1E1E),
    inversePrimary = Color(0xFF388E3C)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF388E3C),
    primaryVariant = Color(0xFF2E7D32),
    secondary = Color(0xFF1976D2),
    secondaryVariant = Color(0xFF1565C0),
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onSurfaceVariant = Color(0xFF555555),
    outline = Color(0xFFBBBBBB),
    error = Color(0xFFB00020),
    onError = Color.White,
    surfaceVariant = Color(0xFFE0E0E0),
    inverseSurface = Color(0xFF2C2C2C),
    inverseOnSurface = Color.White,
    inversePrimary = Color(0xFF4CAF50)
)

@Composable
fun Veyra(
    darkTheme: Boolean = androidx.compose.material3.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

object Typography {
    val displayLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = -0.25.sp
    )
    val displayMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    )
    val displaySmall = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    )
    val headlineLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    )
    val headlineMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    )
    val headlineSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    )
    val titleLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    )
    val titleMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    )
    val titleSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
    val bodyLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    val bodyMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    )
    val bodySmall = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
    val labelLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
    val labelMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    val labelSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
}

object Shapes {
    val extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
    val small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    val medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    val large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    val extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
}

val FontFamily = androidx.compose.ui.text.font.FontFamily.Default