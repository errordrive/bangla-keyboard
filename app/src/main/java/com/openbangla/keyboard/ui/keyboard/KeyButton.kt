package com.openbangla.keyboard.ui.keyboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
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
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

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
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!secondaryLabel.isNullOrEmpty()) {
            Text(
                text = secondaryLabel,
                color = textColor.copy(alpha = 0.45f),
                fontSize = 9.sp,
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
