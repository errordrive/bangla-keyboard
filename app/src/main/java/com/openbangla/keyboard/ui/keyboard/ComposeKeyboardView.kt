package com.openbangla.keyboard.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openbangla.keyboard.data.preferences.TypingLayout
import com.openbangla.keyboard.engine.EnglishLayout
import com.openbangla.keyboard.engine.FixedBanglaLayout
import com.openbangla.keyboard.ui.theme.getKeyboardPalette

@Composable
fun ComposeKeyboardView(
    state: KeyboardLayoutState,
    onKeyClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit,
    onSpace: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    onActionClick: (String) -> Unit
) {
    val palette = getKeyboardPalette(state.theme)
    val keyHeight = (46 * state.heightRatio).dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.background)
    ) {
        // Suggestion strip
        SuggestionStrip(
            suggestions = state.suggestions,
            palette = palette,
            onSuggestionClick = onSuggestionClick,
            onActionClick = onActionClick
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Key rows based on active mode
        if (state.isSymbolsMode) {
            val symbolMatrix = if (state.isMoreSymbolsMode) EnglishLayout.symbolRows else EnglishLayout.numberRows
            RenderKeyMatrix(
                matrix = symbolMatrix,
                palette = palette,
                keyHeight = keyHeight,
                onKeyClick = onKeyClick
            )
        } else {
            when (state.layoutMode) {
                TypingLayout.AVRO, TypingLayout.ENGLISH -> {
                    val rows = if (state.isShifted) EnglishLayout.shiftedRows else EnglishLayout.normalRows
                    RenderKeyMatrix(
                        matrix = rows,
                        palette = palette,
                        keyHeight = keyHeight,
                        onKeyClick = onKeyClick
                    )
                }
                TypingLayout.FIXED -> {
                    val rows = if (state.isShifted) FixedBanglaLayout.shiftedRows else FixedBanglaLayout.normalRows
                    RenderKeyMatrix(
                        matrix = rows,
                        palette = palette,
                        keyHeight = keyHeight,
                        onKeyClick = onKeyClick
                    )
                }
            }
        }

        // Action row: Shift / Symbols / Backspace (integrated)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
        ) {
            // Shift / Caps key
            KeyButton(
                modifier = Modifier.weight(1.3f),
                label = if (state.isShifted) "⇪" else "⇧",
                backgroundColor = palette.keyActionBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { state.toggleShift() }
            )

            // Backspace key
            KeyButton(
                modifier = Modifier.weight(1.3f),
                label = "⌫",
                backgroundColor = palette.keyActionBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = onBackspace
            )
        }

        // Bottom row: ?123, Layout switch, Space, Period, Enter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
        ) {
            KeyButton(
                modifier = Modifier.weight(1.2f),
                label = if (state.isSymbolsMode) "ABC" else "?123",
                backgroundColor = palette.keyActionBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { state.toggleSymbols() }
            )

            KeyButton(
                modifier = Modifier.weight(1.0f),
                label = "🌐",
                backgroundColor = palette.keyActionBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { state.switchLayout() }
            )

            KeyButton(
                modifier = Modifier.weight(3.8f),
                label = when (state.layoutMode) {
                    TypingLayout.AVRO -> "বাংলা (Avro)"
                    TypingLayout.FIXED -> "বাংলা (Fixed)"
                    TypingLayout.ENGLISH -> "English"
                },
                backgroundColor = palette.keyBackground,
                textColor = palette.textPrimary.copy(alpha = 0.8f),
                height = keyHeight,
                onClick = onSpace
            )

            KeyButton(
                modifier = Modifier.weight(1.0f),
                label = if (state.layoutMode == TypingLayout.ENGLISH) "." else "।",
                backgroundColor = palette.keyBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { onKeyClick(if (state.layoutMode == TypingLayout.ENGLISH) "." else "।") }
            )

            KeyButton(
                modifier = Modifier.weight(1.3f),
                label = "↵",
                backgroundColor = palette.accent,
                textColor = palette.background,
                height = keyHeight,
                onClick = onEnter
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun RenderKeyMatrix(
    matrix: List<List<String>>,
    palette: com.openbangla.keyboard.ui.theme.KeyboardPalette,
    keyHeight: androidx.compose.ui.unit.Dp,
    onKeyClick: (String) -> Unit
) {
    matrix.forEach { row ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 1.dp)
        ) {
            row.forEach { key ->
                KeyButton(
                    modifier = Modifier.weight(1f),
                    label = key,
                    backgroundColor = palette.keyBackground,
                    textColor = palette.textPrimary,
                    height = keyHeight,
                    onClick = { onKeyClick(key) }
                )
            }
        }
    }
}
