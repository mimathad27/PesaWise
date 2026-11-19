package com.pesawise.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.pesawise.app.R

val PesaNavy = Color(0xFF102A43)
val PesaGreen = Color(0xFF0D8F72)
val PesaCanvas = Color(0xFFF7F5F0)
val PesaSlate = Color(0xFF707781)
val PesaInterFontFamily = FontFamily(
    Font(R.font.inter_variable, weight = FontWeight.Normal),
    Font(R.font.inter_variable, weight = FontWeight.Bold)
)
private val PesaDmSansFontFamily = FontFamily(
    Font(R.font.dm_sans_variable, weight = FontWeight.Normal),
    Font(R.font.dm_sans_variable, weight = FontWeight.Bold)
)

private val Colors = lightColorScheme(
    primary = PesaGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEEE7),
    onPrimaryContainer = PesaNavy,
    secondary = PesaNavy,
    onSecondary = Color.White,
    background = PesaCanvas,
    onBackground = PesaNavy,
    surface = Color.White,
    onSurface = PesaNavy,
    surfaceVariant = Color(0xFFE9E6E0),
    onSurfaceVariant = PesaSlate
)

@Composable
fun PesaWiseTheme(content: @Composable () -> Unit) {
    val defaults = Typography()
    val typography = defaults.copy(
        displayLarge = defaults.displayLarge.copy(fontFamily = PesaInterFontFamily),
        displayMedium = defaults.displayMedium.copy(fontFamily = PesaInterFontFamily),
        displaySmall = defaults.displaySmall.copy(fontFamily = PesaInterFontFamily),
        headlineLarge = defaults.headlineLarge.copy(fontFamily = PesaInterFontFamily),
        headlineMedium = defaults.headlineMedium.copy(fontFamily = PesaInterFontFamily),
        headlineSmall = defaults.headlineSmall.copy(fontFamily = PesaInterFontFamily),
        titleLarge = defaults.titleLarge.copy(fontFamily = PesaInterFontFamily),
        titleMedium = defaults.titleMedium.copy(fontFamily = PesaInterFontFamily),
        titleSmall = defaults.titleSmall.copy(fontFamily = PesaInterFontFamily),
        bodyLarge = defaults.bodyLarge.copy(fontFamily = PesaDmSansFontFamily),
        bodyMedium = defaults.bodyMedium.copy(fontFamily = PesaDmSansFontFamily),
        bodySmall = defaults.bodySmall.copy(fontFamily = PesaDmSansFontFamily),
        labelLarge = defaults.labelLarge.copy(fontFamily = PesaDmSansFontFamily),
        labelMedium = defaults.labelMedium.copy(fontFamily = PesaDmSansFontFamily),
        labelSmall = defaults.labelSmall.copy(fontFamily = PesaDmSansFontFamily)
    )
    MaterialTheme(colorScheme = Colors, typography = typography, content = content)
}
