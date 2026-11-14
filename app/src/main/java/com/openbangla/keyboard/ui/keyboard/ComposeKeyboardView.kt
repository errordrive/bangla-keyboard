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
            .padding(bottom = 6.dp)
    ) {
        // Top Suggestion & Tool Strip
        SuggestionStrip(
            suggestions = state.suggestions,
            palette = palette,
            layoutMode = state.layoutMode,
            onSuggestionClick = onSuggestionClick,
            onActionClick = onActionClick
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Determine current key matrix (3 rows)
        val matrix: List<List<String>> = when {
            state.isSymbolsMode -> {
                if (state.isMoreSymbolsMode) EnglishLayout.symbolRows else EnglishLayout.numberRows
            }
            state.layoutMode == TypingLayout.FIXED -> {
                if (state.isShifted) FixedBanglaLayout.shiftedRows else FixedBanglaLayout.normalRows
            }
            else -> {
                // AVRO or ENGLISH
                if (state.isShifted) EnglishLayout.shiftedRows else EnglishLayout.normalRows
            }
        }

        // Row 1 (10 keys)
        if (matrix.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
            ) {
                matrix[0].forEach { keyChar ->
                    KeyButton(
                        modifier = Modifier.weight(1f),
                        label = keyChar,
                        backgroundColor = palette.keyBackground,
                        textColor = palette.textPrimary,
                        height = keyHeight,
                        onClick = { onKeyClick(keyChar) }
                    )
                }
            }
        }

        // Row 2 (9 keys, centered with side padding)
        if (matrix.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
            ) {
                Spacer(modifier = Modifier.weight(0.5f))
                matrix[1].forEach { keyChar ->
                    KeyButton(
                        modifier = Modifier.weight(1f),
                        label = keyChar,
                        backgroundColor = palette.keyBackground,
                        textColor = palette.textPrimary,
                        height = keyHeight,
                        onClick = { onKeyClick(keyChar) }
                    )
                }
                Spacer(modifier = Modifier.weight(0.5f))
            }
        }

        // Row 3 (7 keys flanked by Shift on the left and Backspace on the right)
        if (matrix.size > 2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
            ) {
                // Shift Key (Left)
                val shiftLabel = when {
                    state.isSymbolsMode -> if (state.isMoreSymbolsMode) "1/2" else "2/2"
                    state.isShifted -> "⇪"
                    else -> "⇧"
                }
                KeyButton(
                    modifier = Modifier.weight(1.4f),
                    label = shiftLabel,
                    backgroundColor = if (state.isShifted) palette.accent.copy(alpha = 0.25f) else palette.keyActionBackground,
                    textColor = if (state.isShifted) palette.accent else palette.textPrimary,
                    height = keyHeight,
                    onClick = {
                        if (state.isSymbolsMode) {
                            state.toggleMoreSymbols()
                        } else {
                            state.toggleShift()
                        }
                    }
                )

                // 7 Middle Character Keys
                matrix[2].forEach { keyChar ->
                    KeyButton(
                        modifier = Modifier.weight(1f),
                        label = keyChar,
                        backgroundColor = palette.keyBackground,
                        textColor = palette.textPrimary,
                        height = keyHeight,
                        onClick = { onKeyClick(keyChar) }
                    )
                }

                // Backspace Key (Right)
                KeyButton(
                    modifier = Modifier.weight(1.4f),
                    label = "⌫",
                    backgroundColor = palette.keyActionBackground,
                    textColor = palette.textPrimary,
                    height = keyHeight,
                    onClick = onBackspace
                )
            }
        }

        // Row 4: Bottom Action Bar (matching Ridmik Keyboard layout)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
        ) {
            // Symbols / ABC toggle key
            KeyButton(
                modifier = Modifier.weight(1.35f),
                label = if (state.isSymbolsMode) "ABC" else "?123",
                backgroundColor = palette.keyActionBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { state.toggleSymbols() }
            )

            // Comma key
            KeyButton(
                modifier = Modifier.weight(0.95f),
                label = ",",
                backgroundColor = palette.keyActionBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { onKeyClick(",") }
            )

            // Spacebar showing active language
            val spaceLabel = when (state.layoutMode) {
                TypingLayout.AVRO -> "বাংলা (Avro)"
                TypingLayout.ENGLISH -> "English"
                TypingLayout.FIXED -> "সহজ বাংলা"
            }
            KeyButton(
                modifier = Modifier.weight(4.4f),
                label = spaceLabel,
                backgroundColor = palette.keyBackground,
                textColor = palette.textPrimary.copy(alpha = 0.85f),
                height = keyHeight,
                onClick = onSpace
            )

            // Period key
            KeyButton(
                modifier = Modifier.weight(0.95f),
                label = ".",
                backgroundColor = palette.keyActionBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { onKeyClick(".") }
            )

            // Enter / Action key (Colored in primary accent)
            KeyButton(
                modifier = Modifier.weight(1.35f),
                label = "⏎",
                backgroundColor = palette.accent,
                textColor = palette.background,
                height = keyHeight,
                onClick = onEnter
            )
        }
    }
}
