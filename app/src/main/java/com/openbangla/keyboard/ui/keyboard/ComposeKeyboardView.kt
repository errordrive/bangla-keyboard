package com.openbangla.keyboard.ui.keyboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openbangla.keyboard.data.db.ClipEntity
import com.openbangla.keyboard.data.preferences.KeyboardTheme
import com.openbangla.keyboard.data.preferences.TypingLayout
import com.openbangla.keyboard.engine.EnglishLayout
import com.openbangla.keyboard.engine.FixedBanglaLayout
import com.openbangla.keyboard.engine.ProbhatBanglaLayout
import com.openbangla.keyboard.ui.theme.KeyboardPalette
import com.openbangla.keyboard.ui.theme.getKeyboardPalette

@Composable
fun ComposeKeyboardView(
    state: KeyboardLayoutState,
    clips: List<ClipEntity> = emptyList(),
    onKeyClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit,
    onSpace: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    onLayoutSwitch: () -> Unit,
    onSelectTheme: (KeyboardTheme) -> Unit,
    onOpenTranslate: () -> Unit,
    onOpenFactCheck: () -> Unit,
    onOpenStore: () -> Unit,
    onOpenSettings: () -> Unit,
    onMoveCursorLeft: (Boolean) -> Unit = {},
    onMoveCursorRight: (Boolean) -> Unit = {},
    onMoveCursorUp: (Boolean) -> Unit = {},
    onMoveCursorDown: (Boolean) -> Unit = {},
    onMoveHome: (Boolean) -> Unit = {},
    onMoveEnd: (Boolean) -> Unit = {},
    onSelectAll: () -> Unit = {},
    onCopy: () -> Unit = {},
    onCut: () -> Unit = {},
    onPaste: () -> Unit = {},
    onDeleteClip: (ClipEntity) -> Unit = {},
    onClearAllClips: () -> Unit = {},
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {}
) {
    val palette = getKeyboardPalette(state.theme)
    val keyHeight = (46 * state.heightRatio).dp
    val totalBodyHeight = keyHeight * 4 + 24.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.background)
            .padding(bottom = 6.dp)
    ) {
        // Top Suggestion & Toolbar Strip
        SuggestionStrip(
            suggestions = state.suggestions,
            palette = palette,
            layoutMode = state.layoutMode,
            onSuggestionClick = onSuggestionClick,
            onMenuClick = { state.toggleMoreOptions() },
            onLayoutBadgeClick = onLayoutSwitch
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Animated Body: Smooth transitions between all keyboard modes
        val activeOverlay = when {
            state.isMoreOptionsOpen -> "options"
            state.isTextEditingOpen -> "text_editing"
            state.isEmojiKeyboardOpen -> "emoji"
            state.isClipboardOpen -> "clipboard"
            state.isNumberPadMode -> "numpad"
            else -> "keyboard"
        }

        AnimatedContent(
            targetState = activeOverlay,
            transitionSpec = {
                (slideInVertically { height -> height / 5 } + fadeIn()) togetherWith
                        (slideOutVertically { height -> -height / 5 } + fadeOut())
            },
            label = "KeyboardOverlayTransition"
        ) { overlayMode ->
            when (overlayMode) {
                "options" -> {
                    InKeyboardMoreOptionsView(
                        palette = palette,
                        totalHeight = totalBodyHeight,
                        currentTheme = state.theme,
                        onSelectTheme = onSelectTheme,
                        onOpenClipboard = { state.toggleClipboard() },
                        onOpenNumberPad = { state.toggleNumberPad() },
                        onOpenTextEditing = { state.toggleTextEditing() },
                        onOpenTranslate = onOpenTranslate,
                        onOpenFactCheck = onOpenFactCheck,
                        onOpenStore = onOpenStore,
                        onOpenSettings = onOpenSettings,
                        onClose = { state.closeOverlays() }
                    )
                }
                "text_editing" -> {
                    InKeyboardTextEditingView(
                        palette = palette,
                        totalHeight = totalBodyHeight,
                        onMoveLeft = onMoveCursorLeft,
                        onMoveRight = onMoveCursorRight,
                        onMoveUp = onMoveCursorUp,
                        onMoveDown = onMoveCursorDown,
                        onMoveHome = onMoveHome,
                        onMoveEnd = onMoveEnd,
                        onSelectAll = onSelectAll,
                        onCopy = onCopy,
                        onCut = onCut,
                        onPaste = onPaste,
                        onClose = { state.closeOverlays() }
                    )
                }
                "emoji" -> {
                    InKeyboardEmojiView(
                        palette = palette,
                        totalHeight = totalBodyHeight,
                        onEmojiClick = onKeyClick,
                        onBackspace = onBackspace,
                        onSpace = onSpace,
                        onClose = { state.closeOverlays() }
                    )
                }
                "clipboard" -> {
                    InKeyboardClipboardView(
                        clips = clips,
                        palette = palette,
                        totalHeight = totalBodyHeight,
                        onClipClick = { clipText ->
                            onKeyClick(clipText)
                            state.closeOverlays()
                        },
                        onDeleteClip = onDeleteClip,
                        onClearAll = onClearAllClips,
                        onClose = { state.closeOverlays() }
                    )
                }
                "numpad" -> {
                    DedicatedNumberPadView(
                        palette = palette,
                        keyHeight = keyHeight,
                        onKeyClick = onKeyClick,
                        onBackspace = onBackspace,
                        onEnter = onEnter,
                        onSpace = onSpace,
                        onClose = { state.closeOverlays() }
                    )
                }
                else -> {
                    StandardKeyboardMatrix(
                        state = state,
                        palette = palette,
                        keyHeight = keyHeight,
                        onKeyClick = onKeyClick,
                        onBackspace = onBackspace,
                        onEnter = onEnter,
                        onSpace = onSpace,
                        onSwipeLeft = onSwipeLeft,
                        onSwipeRight = onSwipeRight
                    )
                }
            }
        }
    }
}

@Composable
private fun StandardKeyboardMatrix(
    state: KeyboardLayoutState,
    palette: KeyboardPalette,
    keyHeight: Dp,
    onKeyClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit,
    onSpace: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    val isProbhat = state.layoutMode == TypingLayout.PROBHAT
    val isSohoj = state.layoutMode == TypingLayout.SOHOJ

    val matrix: List<List<String>> = when {
        state.isSymbolsMode -> {
            if (state.isMoreSymbolsMode) EnglishLayout.symbolRows else EnglishLayout.numberRows
        }
        isProbhat -> {
            if (state.isShifted) ProbhatBanglaLayout.shiftedRows else ProbhatBanglaLayout.normalRows
        }
        isSohoj -> {
            if (state.isShifted) FixedBanglaLayout.shiftedRows else FixedBanglaLayout.normalRows
        }
        else -> {
            // English / Avro
            if (state.isShifted) EnglishLayout.shiftedRows else EnglishLayout.normalRows
        }
    }

    fun getSecondaryHint(key: String): String? {
        if (state.isSymbolsMode) return null
        return when (state.layoutMode) {
            TypingLayout.PROBHAT -> ProbhatBanglaLayout.getSecondaryChar(key, state.isShifted)
            TypingLayout.SOHOJ -> {
                val sohojMap = mapOf(
                    "ঙ" to "ং", "য" to "্য", "ড" to "ঢ", "প" to "ফ", "ট" to "ঠ",
                    "চ" to "ছ", "জ" to "ঝ", "হ" to "ঞ", "গ" to "ঘ", "ড়" to "ঢ়",
                    "ৃ" to "ঃ", "ভ" to "ভ", "থ" to "ঠ", "দ" to "ধ", "শ" to "ষ",
                    "স" to "স", "ব" to "ব", "ন" to "ণ", "ম" to "ম",
                    "ো" to "ৌ", "ে" to "ৈ", "া" to "া", "ি" to "ী", "ু" to "ূ",
                    "ল" to "ল", "ত" to "থ"
                )
                sohojMap[key]
            }
            TypingLayout.ENGLISH -> {
                val numMap = mapOf(
                    "q" to "1", "w" to "2", "e" to "3", "r" to "4", "t" to "5",
                    "y" to "6", "u" to "7", "i" to "8", "o" to "9", "p" to "0",
                    "a" to "@", "s" to "#", "d" to "$", "f" to "_", "g" to "&",
                    "h" to "-", "j" to "+", "k" to "(", "l" to ")"
                )
                numMap[key]
            }
            else -> null
        }
    }

    fun getLongPressHint(key: String): String? {
        if (state.isSymbolsMode) return null
        return when (state.layoutMode) {
            TypingLayout.PROBHAT -> ProbhatBanglaLayout.getLongPressChar(key, state.isShifted)
            else -> null
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        // Row 1
        if (matrix.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
            ) {
                matrix[0].forEach { keyChar ->
                    val secondary = getSecondaryHint(keyChar)
                    val longPress = getLongPressHint(keyChar)
                    KeyButton(
                        modifier = Modifier.weight(1f),
                        label = keyChar,
                        secondaryLabel = secondary,
                        backgroundColor = palette.keyBackground,
                        textColor = palette.textPrimary,
                        height = keyHeight,
                        dragToTypeShiftEnabled = state.dragToTypeShiftEnabled,
                        onClick = { onKeyClick(keyChar) },
                        onDragAction = { shiftChar -> onKeyClick(shiftChar) },
                        onLongPress = { if (longPress != null) onKeyClick(longPress) }
                    )
                }
            }
        }

        // Row 2 (Centered with side spacing if needed)
        if (matrix.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
            ) {
                val needSpacer = matrix[1].size < matrix[0].size
                if (needSpacer) {
                    Spacer(modifier = Modifier.weight(0.5f))
                }
                matrix[1].forEach { keyChar ->
                    val secondary = getSecondaryHint(keyChar)
                    val longPress = getLongPressHint(keyChar)
                    KeyButton(
                        modifier = Modifier.weight(1f),
                        label = keyChar,
                        secondaryLabel = secondary,
                        backgroundColor = palette.keyBackground,
                        textColor = palette.textPrimary,
                        height = keyHeight,
                        dragToTypeShiftEnabled = state.dragToTypeShiftEnabled,
                        onClick = { onKeyClick(keyChar) },
                        onDragAction = { shiftChar -> onKeyClick(shiftChar) },
                        onLongPress = { if (longPress != null) onKeyClick(longPress) }
                    )
                }
                if (needSpacer) {
                    Spacer(modifier = Modifier.weight(0.5f))
                }
            }
        }

        // Row 3 (Shift, characters, Backspace)
        if (matrix.size > 2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
            ) {
                val isNineKeyRow = matrix[2].size >= 9
                val sideWeight = if (isNineKeyRow) 1.25f else 1.4f

                // Shift Key
                val shiftLabel = when {
                    state.isSymbolsMode -> if (state.isMoreSymbolsMode) "1/2" else "2/2"
                    state.shiftMode == ShiftMode.LOCKED -> "⇪"
                    else -> "⇧"
                }
                val shiftBgColor = when {
                    state.shiftMode == ShiftMode.LOCKED -> palette.accent
                    state.shiftMode == ShiftMode.ONCE -> palette.accent.copy(alpha = 0.35f)
                    else -> palette.keyActionBackground
                }
                val shiftTextColor = when {
                    state.shiftMode == ShiftMode.LOCKED -> palette.background
                    state.shiftMode == ShiftMode.ONCE -> palette.accent
                    else -> palette.textPrimary
                }

                KeyButton(
                    modifier = Modifier.weight(sideWeight),
                    label = shiftLabel,
                    backgroundColor = shiftBgColor,
                    textColor = shiftTextColor,
                    height = keyHeight,
                    dragToTypeShiftEnabled = false,
                    onClick = {
                        if (state.isSymbolsMode) {
                            state.toggleMoreSymbols()
                        } else {
                            state.toggleShift()
                        }
                    }
                )

                // Middle Character Keys
                matrix[2].forEach { keyChar ->
                    val secondary = getSecondaryHint(keyChar)
                    val longPress = getLongPressHint(keyChar)
                    KeyButton(
                        modifier = Modifier.weight(1f),
                        label = keyChar,
                        secondaryLabel = secondary,
                        backgroundColor = palette.keyBackground,
                        textColor = palette.textPrimary,
                        height = keyHeight,
                        dragToTypeShiftEnabled = state.dragToTypeShiftEnabled,
                        onClick = { onKeyClick(keyChar) },
                        onDragAction = { shiftChar -> onKeyClick(shiftChar) },
                        onLongPress = { if (longPress != null) onKeyClick(longPress) }
                    )
                }

                // Backspace Key
                KeyButton(
                    modifier = Modifier.weight(sideWeight),
                    label = "⌫",
                    backgroundColor = palette.keyActionBackground,
                    textColor = palette.textPrimary,
                    height = keyHeight,
                    dragToTypeShiftEnabled = false,
                    onClick = onBackspace
                )
            }
        }

        // Row 4: Bottom Action Bar (Symbols, Numpad, Emoji, Comma, Space, Period, Enter)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
        ) {
            // Symbols toggle
            KeyButton(
                modifier = Modifier.weight(1.1f),
                label = if (state.isSymbolsMode) "ABC" else "?123",
                backgroundColor = palette.keyActionBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { state.toggleSymbols() }
            )

            // Dedicated Number Pad button
            KeyButton(
                modifier = Modifier.weight(0.85f),
                label = "123",
                backgroundColor = palette.keyActionBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { state.toggleNumberPad() }
            )

            // Direct Emoji Button
            KeyButton(
                modifier = Modifier.weight(0.85f),
                label = "😊",
                backgroundColor = palette.keyActionBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { state.toggleEmojiKeyboard() }
            )

            // Comma
            KeyButton(
                modifier = Modifier.weight(0.75f),
                label = ",",
                backgroundColor = palette.keyActionBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { onKeyClick(",") }
            )

            // Spacebar showing active language with horizontal slide detection
            val spaceLabel = when (state.layoutMode) {
                TypingLayout.ENGLISH -> "English"
                TypingLayout.PROBHAT -> "প্রভাত"
                TypingLayout.SOHOJ -> "সহজ"
                TypingLayout.AVRO -> "Avro"
            }
            SpaceKeyButton(
                modifier = Modifier.weight(3.4f),
                label = spaceLabel,
                backgroundColor = palette.keyBackground,
                textColor = palette.textPrimary.copy(alpha = 0.85f),
                height = keyHeight,
                onClick = onSpace,
                onSwipeLeft = onSwipeLeft,
                onSwipeRight = onSwipeRight
            )

            // Period
            KeyButton(
                modifier = Modifier.weight(0.75f),
                label = ".",
                backgroundColor = palette.keyActionBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { onKeyClick(".") }
            )

            // Enter / Action Key
            KeyButton(
                modifier = Modifier.weight(1.2f),
                label = "⏎",
                backgroundColor = palette.accent,
                textColor = palette.background,
                height = keyHeight,
                onClick = onEnter
            )
        }
    }
}

@Composable
fun SpaceKeyButton(
    modifier: Modifier = Modifier,
    label: String,
    backgroundColor: Color,
    textColor: Color,
    height: Dp,
    onClick: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val displayBgColor = if (isPressed) {
        backgroundColor.copy(alpha = 0.7f)
    } else {
        backgroundColor
    }

    Box(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 3.dp)
            .height(height)
            .shadow(elevation = if (isPressed) 0.5.dp else 1.5.dp, shape = RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .background(displayBgColor)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    var accumulatedX = 0f
                    var swipeTriggered = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break

                        if (!change.pressed) {
                            isPressed = false
                            if (!swipeTriggered) {
                                onClick()
                            }
                            break
                        }

                        val dx = change.position.x - change.previousPosition.x
                        accumulatedX += dx

                        if (!swipeTriggered && accumulatedX > 50f) {
                            swipeTriggered = true
                            isPressed = false
                            onSwipeRight()
                        } else if (!swipeTriggered && accumulatedX < -50f) {
                            swipeTriggered = true
                            isPressed = false
                            onSwipeLeft()
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "‹ $label ›",
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
