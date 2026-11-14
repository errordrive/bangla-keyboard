package com.openbangla.keyboard.ui.keyboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.openbangla.keyboard.data.preferences.KeyboardTheme
import com.openbangla.keyboard.data.preferences.TypingLayout

class KeyboardLayoutState {
    var layoutMode by mutableStateOf(TypingLayout.AVRO)
    var isShifted by mutableStateOf(false)
    var isSymbolsMode by mutableStateOf(false)
    var isMoreSymbolsMode by mutableStateOf(false)
    var composingText by mutableStateOf("")
    var suggestions by mutableStateOf<List<String>>(emptyList())
    var theme by mutableStateOf(KeyboardTheme.LIGHT)
    var heightRatio by mutableFloatStateOf(1.0f)
    var autoCorrectEnabled by mutableStateOf(true)

    fun toggleShift() {
        isShifted = !isShifted
    }

    fun toggleSymbols() {
        isSymbolsMode = !isSymbolsMode
        isMoreSymbolsMode = false
    }

    fun toggleMoreSymbols() {
        isMoreSymbolsMode = !isMoreSymbolsMode
    }

    fun switchLayout() {
        layoutMode = when (layoutMode) {
            TypingLayout.AVRO -> TypingLayout.FIXED
            TypingLayout.FIXED -> TypingLayout.ENGLISH
            TypingLayout.ENGLISH -> TypingLayout.AVRO
        }
        isShifted = false
        isSymbolsMode = false
    }
}
