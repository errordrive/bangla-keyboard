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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.runtime.remember
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
import com.openbangla.keyboard.data.preferences.TypingLayout
import com.openbangla.keyboard.engine.EnglishLayout
import com.openbangla.keyboard.engine.FixedBanglaLayout
import com.openbangla.keyboard.engine.ProbhatBanglaLayout
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
    onActionClick: (String) -> Unit,
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
        // Top Suggestion & Tool Strip
        SuggestionStrip(
            suggestions = state.suggestions,
            palette = palette,
            layoutMode = state.layoutMode,
            onSuggestionClick = onSuggestionClick,
            onActionClick = onActionClick
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Animated Body: Switch between Keyboard Keys, Clipboard View, or Number Pad Mode
        AnimatedContent(
            targetState = Triple(state.isClipboardOpen, state.isNumberPadMode, state.layoutMode),
            transitionSpec = {
                (slideInVertically { height -> height / 4 } + fadeIn()) togetherWith
                        (slideOutVertically { height -> -height / 4 } + fadeOut())
            },
            label = "KeyboardBodyTransition"
        ) { (isClipboard, isNumPad, currentMode) ->
            when {
                isClipboard -> {
                    InKeyboardClipboardView(
                        clips = clips,
                        palette = palette,
                        totalHeight = totalBodyHeight,
                        onClipClick = { clipText ->
                            onKeyClick(clipText)
                            state.isClipboardOpen = false
                        },
                        onDeleteClip = onDeleteClip,
                        onClearAll = onClearAllClips,
                        onClose = { state.isClipboardOpen = false }
                    )
                }
                isNumPad -> {
                    DedicatedNumberPadView(
                        palette = palette,
                        keyHeight = keyHeight,
                        onKeyClick = onKeyClick,
                        onBackspace = onBackspace,
                        onEnter = onEnter,
                        onSpace = onSpace,
                        onClose = { state.isNumberPadMode = false }
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
    palette: com.openbangla.keyboard.ui.theme.KeyboardPalette,
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

        // Row 2 (Centered with subtle side spacers if needed)
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
                    KeyButton(
                        modifier = Modifier.weight(1f),
                        label = keyChar,
                        backgroundColor = palette.keyBackground,
                        textColor = palette.textPrimary,
                        height = keyHeight,
                        onClick = { onKeyClick(keyChar) }
                    )
                }
                if (needSpacer) {
                    Spacer(modifier = Modifier.weight(0.5f))
                }
            }
        }

        // Row 3 (Shift on left, characters in middle, Backspace on right)
        if (matrix.size > 2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
            ) {
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
                    modifier = Modifier.weight(1.4f),
                    label = shiftLabel,
                    backgroundColor = shiftBgColor,
                    textColor = shiftTextColor,
                    height = keyHeight,
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
                    KeyButton(
                        modifier = Modifier.weight(1f),
                        label = keyChar,
                        backgroundColor = palette.keyBackground,
                        textColor = palette.textPrimary,
                        height = keyHeight,
                        onClick = { onKeyClick(keyChar) }
                    )
                }

                // Backspace Key
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

        // Row 4: Bottom Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
        ) {
            // Symbols toggle
            KeyButton(
                modifier = Modifier.weight(1.2f),
                label = if (state.isSymbolsMode) "ABC" else "?123",
                backgroundColor = palette.keyActionBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { state.toggleSymbols() }
            )

            // Dedicated Number Pad button
            KeyButton(
                modifier = Modifier.weight(0.9f),
                label = "123",
                backgroundColor = palette.keyActionBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { state.toggleNumberPad() }
            )

            // Comma
            KeyButton(
                modifier = Modifier.weight(0.8f),
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
                modifier = Modifier.weight(3.8f),
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
                modifier = Modifier.weight(0.8f),
                label = ".",
                backgroundColor = palette.keyActionBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { onKeyClick(".") }
            )

            // Enter / Action Key
            KeyButton(
                modifier = Modifier.weight(1.3f),
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 3.dp)
            .height(height)
            .shadow(elevation = if (isPressed) 0.5.dp else 1.5.dp, shape = RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .background(if (isPressed) backgroundColor.copy(alpha = 0.7f) else backgroundColor)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var accumulatedX = 0f
                    var triggered = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break

                        if (change.changedToUp()) {
                            if (!triggered && kotlin.math.abs(accumulatedX) < 30f) {
                                onClick()
                            }
                            break
                        }

                        val dx = change.position.x - change.previousPosition.x
                        accumulatedX += dx

                        if (!triggered) {
                            if (accumulatedX < -45f) {
                                triggered = true
                                onSwipeLeft()
                            } else if (accumulatedX > 45f) {
                                triggered = true
                                onSwipeRight()
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "‹ ",
                color = textColor.copy(alpha = 0.4f),
                fontSize = 13.sp
            )
            Text(
                text = label,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = " ›",
                color = textColor.copy(alpha = 0.4f),
                fontSize = 13.sp
            )
        }
    }
}
