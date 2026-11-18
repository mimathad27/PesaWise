package com.pesawise.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PesaNavy = Color(0xFF102A43)
val PesaGreen = Color(0xFF0B8F70)
val PesaCanvas = Color(0xFFF5F8FA)
val PesaSlate = Color(0xFF627D98)

private val Colors = lightColorScheme(
    primary = PesaGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9F2EA),
    onPrimaryContainer = PesaNavy,
    secondary = PesaNavy,
    onSecondary = Color.White,
    background = PesaCanvas,
    onBackground = PesaNavy,
    surface = Color.White,
    onSurface = PesaNavy,
    surfaceVariant = Color(0xFFE4EBF1),
    onSurfaceVariant = PesaSlate
)

@Composable
fun PesaWiseTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}