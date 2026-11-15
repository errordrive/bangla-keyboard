package com.openbangla.keyboard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.openbangla.keyboard.data.preferences.KeyboardTheme

private val DarkColorScheme = darkColorScheme(
    primary = RidmikGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = RidmikGreenAccent,
    onPrimaryContainer = Color(0xFF002114),
    background = ClassicDarkBg,
    surface = ClassicDarkBg,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = RidmikGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = RidmikGreenAccent,
    onPrimaryContainer = Color(0xFF002114),
    background = ClassicLightBg,
    surface = ClassicLightBg,
    onSurface = ClassicLightText
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
        KeyboardTheme.RIDMIK_GREEN -> KeyboardPalette(
            background = RidmikGreenPrimary,
            keyBackground = RidmikGreenKeyBg,
            keyActionBackground = RidmikGreenKeyActionBg,
            textPrimary = Color.White,
            accent = RidmikGreenAccent
        )
        KeyboardTheme.LIGHT -> KeyboardPalette(
            background = ClassicLightBg,
            keyBackground = ClassicLightKeyBg,
            keyActionBackground = ClassicLightActionBg,
            textPrimary = ClassicLightText,
            accent = Color(0xFF26A69A)
        )
        KeyboardTheme.DARK -> KeyboardPalette(
            background = ClassicDarkBg,
            keyBackground = ClassicDarkKeyBg,
            keyActionBackground = ClassicDarkActionBg,
            textPrimary = Color.White,
            accent = RidmikGreenAccent
        )
        KeyboardTheme.AMOLED -> KeyboardPalette(
            background = PureDarkBg,
            keyBackground = PureDarkKeyBg,
            keyActionBackground = PureDarkActionBg,
            textPrimary = Color.White,
            accent = RidmikGreenAccent
        )
        KeyboardTheme.DARK_SILVER -> KeyboardPalette(
            background = DarkSilverBg,
            keyBackground = DarkSilverKeyBg,
            keyActionBackground = DarkSilverActionBg,
            textPrimary = DarkSilverText,
            accent = DarkSilverText
        )
        KeyboardTheme.MIDNIGHT_BLACK -> KeyboardPalette(
            background = MidnightBlackBg,
            keyBackground = MidnightBlackKeyBg,
            keyActionBackground = MidnightBlackActionBg,
            textPrimary = Color.White,
            accent = MidnightBlackAccent
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
