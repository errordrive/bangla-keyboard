package com.openbangla.keyboard.ui.keyboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.openbangla.keyboard.data.preferences.KeyboardTheme
import com.openbangla.keyboard.data.preferences.TypingLayout

enum class ShiftMode {
    OFF,
    ONCE,
    LOCKED
}

class KeyboardLayoutState {
    var layoutMode by mutableStateOf(TypingLayout.ENGLISH)
    var shiftMode by mutableStateOf(ShiftMode.OFF)
    val isShifted: Boolean
        get() = shiftMode != ShiftMode.OFF

    var isSymbolsMode by mutableStateOf(false)
    var isMoreSymbolsMode by mutableStateOf(false)
    var isNumberPadMode by mutableStateOf(false)
    var isClipboardOpen by mutableStateOf(false)
    var isMoreOptionsOpen by mutableStateOf(false)
    var isTextEditingOpen by mutableStateOf(false)
    var isEmojiKeyboardOpen by mutableStateOf(false)

    var composingText by mutableStateOf("")
    var suggestions by mutableStateOf<List<String>>(emptyList())
    var theme by mutableStateOf(KeyboardTheme.RIDMIK_GREEN)
    var heightRatio by mutableFloatStateOf(1.30f)
    var autoCorrectEnabled by mutableStateOf(true)
    var dragToTypeShiftEnabled by mutableStateOf(true)

    private var lastShiftTapTime = 0L

    fun toggleShift() {
        val now = System.currentTimeMillis()
        val isDoubleTap = (now - lastShiftTapTime) < 350L
        lastShiftTapTime = now

        shiftMode = when (shiftMode) {
            ShiftMode.OFF -> ShiftMode.ONCE
            ShiftMode.ONCE -> {
                if (isDoubleTap) ShiftMode.LOCKED else ShiftMode.OFF
            }
            ShiftMode.LOCKED -> ShiftMode.OFF
        }
    }

    fun onCharacterTyped() {
        if (shiftMode == ShiftMode.ONCE) {
            shiftMode = ShiftMode.OFF
        }
    }

    fun toggleSymbols() {
        isSymbolsMode = !isSymbolsMode
        isMoreSymbolsMode = false
        closeOverlays()
    }

    fun toggleMoreSymbols() {
        isMoreSymbolsMode = !isMoreSymbolsMode
    }

    fun toggleNumberPad() {
        val target = !isNumberPadMode
        closeOverlays()
        isNumberPadMode = target
    }

    fun toggleClipboard() {
        val target = !isClipboardOpen
        closeOverlays()
        isClipboardOpen = target
    }

    fun toggleMoreOptions() {
        val target = !isMoreOptionsOpen
        closeOverlays()
        isMoreOptionsOpen = target
    }

    fun toggleTextEditing() {
        val target = !isTextEditingOpen
        closeOverlays()
        isTextEditingOpen = target
    }

    fun toggleEmojiKeyboard() {
        val target = !isEmojiKeyboardOpen
        closeOverlays()
        isEmojiKeyboardOpen = target
    }

    fun closeOverlays() {
        isClipboardOpen = false
        isNumberPadMode = false
        isMoreOptionsOpen = false
        isTextEditingOpen = false
        isEmojiKeyboardOpen = false
    }

    fun switchLayout(forward: Boolean = true) {
        val cycle = listOf(TypingLayout.ENGLISH, TypingLayout.PROBHAT, TypingLayout.SOHOJ)
        val currentIndex = cycle.indexOf(layoutMode).let { if (it == -1) 0 else it }
        val nextIndex = if (forward) {
            (currentIndex + 1) % cycle.size
        } else {
            (currentIndex - 1 + cycle.size) % cycle.size
        }
        layoutMode = cycle[nextIndex]
        shiftMode = ShiftMode.OFF
        isSymbolsMode = false
        closeOverlays()
    }
}
