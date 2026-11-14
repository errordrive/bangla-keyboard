package com.openbangla.keyboard.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
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

@Composable
fun KeyButton(
    modifier: Modifier = Modifier,
    label: String,
    secondaryLabel: String? = null,
    backgroundColor: Color,
    textColor: Color,
    height: Dp = 48.dp,
    dragToTypeShiftEnabled: Boolean = true,
    onClick: () -> Unit,
    onDragAction: ((String) -> Unit)? = null,
    onLongPress: (() -> Unit)? = null
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
            .pointerInput(label, secondaryLabel, dragToTypeShiftEnabled) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    var accumulatedX = 0f
                    var accumulatedY = 0f
                    var dragTriggered = false
                    val downTime = System.currentTimeMillis()
                    var longPressFired = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break

                        if (!change.pressed) {
                            // Touch released (UP)
                            isPressed = false
                            if (!dragTriggered && !longPressFired) {
                                onClick()
                            }
                            break
                        }

                        val dx = change.position.x - change.previousPosition.x
                        val dy = change.position.y - change.previousPosition.y
                        accumulatedX += dx
                        accumulatedY += dy

                        val distSq = accumulatedX * accumulatedX + accumulatedY * accumulatedY

                        // Check long press if not dragging
                        if (!dragTriggered && !longPressFired && onLongPress != null && (System.currentTimeMillis() - downTime > 380L) && distSq < 400f) {
                            longPressFired = true
                            isPressed = false
                            onLongPress()
                        }

                        // Check click & drag (swiping up or dragging outward)
                        if (dragToTypeShiftEnabled && !dragTriggered && !longPressFired && !secondaryLabel.isNullOrEmpty()) {
                            // Upward flick (dy < -18) or significant drag (distSq > 900 -> dist > 30)
                            if (accumulatedY < -18f || distSq > 900f) {
                                dragTriggered = true
                                isPressed = false
                                if (onDragAction != null) {
                                    onDragAction(secondaryLabel)
                                } else {
                                    onClick()
                                }
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (!secondaryLabel.isNullOrEmpty()) {
            Text(
                text = secondaryLabel,
                color = textColor.copy(alpha = 0.55f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 4.dp)
            )
        }

        Text(
            text = label,
            color = textColor,
            fontSize = if (label.length > 3) 12.sp else if (label.length > 1) 14.sp else 18.sp,
            fontWeight = if (label.length == 1) FontWeight.Normal else FontWeight.Medium
        )
    }
}
