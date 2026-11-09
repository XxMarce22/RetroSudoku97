package com.retro.sudoku97.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Windows 95/97 palette and type scale.
 */
object Win95 {
    val Teal = Color(0xFF008080)
    val TealDark = Color(0xFF006C6C)
    val Gray = Color(0xFFC0C0C0)
    val LightGray = Color(0xFFDFDFDF)
    val White = Color(0xFFFFFFFF)
    val MidGray = Color(0xFF808080)
    val DarkGray = Color(0xFF404040)
    val Black = Color(0xFF000000)
    val Navy = Color(0xFF000080)
    val Blue = Color(0xFF0000A0)
    val Red = Color(0xFFC00000)
    val Green = Color(0xFF006000)
    val Yellow = Color(0xFFFFFF80)
    val SelectedCell = Color(0xFFFFFFC0)
    val PeerCell = Color(0xFFD8E8F8)
    val GivenCell = Color(0xFFC0C0C0)
    val EditableCell = Color(0xFFFFFFFF)
    val ConflictCell = Color(0xFFFFC0C0)
    val HintCell = Color(0xFFFFFF80)
}

val RetroTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        letterSpacing = 1.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 19.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 15.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 0.2.sp
    )
)

private val RetroColorScheme = lightColorScheme(
    primary = Win95.Navy,
    onPrimary = Win95.White,
    primaryContainer = Win95.Navy,
    onPrimaryContainer = Win95.White,
    secondary = Win95.MidGray,
    onSecondary = Win95.Black,
    secondaryContainer = Win95.LightGray,
    onSecondaryContainer = Win95.Black,
    tertiary = Win95.Teal,
    onTertiary = Win95.White,
    background = Win95.Teal,
    onBackground = Win95.Black,
    surface = Win95.Gray,
    onSurface = Win95.Black,
    surfaceVariant = Win95.LightGray,
    onSurfaceVariant = Win95.DarkGray,
    error = Win95.Red,
    onError = Win95.White,
    outline = Win95.MidGray,
    outlineVariant = Win95.DarkGray
)

@Composable
fun Sudoku97Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RetroColorScheme,
        typography = RetroTypography,
        content = content
    )
}

@Composable
fun RetroDesktop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Win95.Teal)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val step = 18.dp.toPx()
            val dotRadius = 0.65.dp.toPx()
            var y = step / 2f
            while (y < size.height) {
                var x = step / 2f
                while (x < size.width) {
                    drawCircle(
                        color = Win95.TealDark.copy(alpha = 0.55f),
                        radius = dotRadius,
                        center = Offset(x, y)
                    )
                    x += step
                }
                y += step
            }
        }
        content()
    }
}
