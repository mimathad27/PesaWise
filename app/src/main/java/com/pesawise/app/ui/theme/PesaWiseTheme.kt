package com.pesawise.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Colors = lightColorScheme(
    primary = Color(0xFF0B8F70),
    onPrimary = Color.White,
    background = Color(0xFFF5F8FA),
    surface = Color.White
)

@Composable
fun PesaWiseTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
