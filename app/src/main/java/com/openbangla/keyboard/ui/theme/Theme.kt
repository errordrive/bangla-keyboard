package com.openbangla.keyboard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.openbangla.keyboard.data.preferences.KeyboardTheme

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = EmeraldOnContainer,
    background = DarkBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = EmeraldOnContainer,
    background = LightBackground,
    surface = LightSurface,
    onSurface = LightOnSurface
)

private val AmoledColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    background = AmoledBackground,
    surface = AmoledBackground,
    onSurface = AmoledOnSurface
)

data class KeyboardPalette(
    val background: Color,
    val keyBackground: Color,
    val keyActionBackground: Color,
    val textPrimary: Color,
    val accent: Color
)

fun getKeyboardPalette(theme: KeyboardTheme): KeyboardPalette {
    return when (theme) {
        KeyboardTheme.LIGHT -> KeyboardPalette(
            background = Color(0xFFECEFF1),
            keyBackground = Color(0xFFFFFFFF),
            keyActionBackground = Color(0xFFCFD8DC),
            textPrimary = Color(0xFF263238),
            accent = EmeraldPrimary
        )
        KeyboardTheme.DARK -> KeyboardPalette(
            background = Color(0xFF1E2124),
            keyBackground = Color(0xFF2F3136),
            keyActionBackground = Color(0xFF36393F),
            textPrimary = Color(0xFFFFFFFF),
            accent = EmeraldContainer
        )
        KeyboardTheme.AMOLED -> KeyboardPalette(
            background = Color(0xFF000000),
            keyBackground = Color(0xFF141414),
            keyActionBackground = Color(0xFF242424),
            textPrimary = Color(0xFFFFFFFF),
            accent = EmeraldContainer
        )
        KeyboardTheme.EMERALD -> KeyboardPalette(
            background = Color(0xFF004D36),
            keyBackground = Color(0xFF006C4C),
            keyActionBackground = Color(0xFF00583E),
            textPrimary = Color(0xFFFFFFFF),
            accent = EmeraldContainer
        )
        KeyboardTheme.SLATE -> KeyboardPalette(
            background = Color(0xFF1E293B),
            keyBackground = Color(0xFF334155),
            keyActionBackground = Color(0xFF475569),
            textPrimary = Color(0xFFF8FAFC),
            accent = Color(0xFF38BDF8)
        )
    }
}

@Composable
fun BanglaKeyboardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
