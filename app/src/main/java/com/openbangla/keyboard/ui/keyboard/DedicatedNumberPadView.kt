package com.openbangla.keyboard.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DedicatedNumberPadView(
    modifier: Modifier = Modifier,
    palette: KeyboardPalette,
    keyHeight: Dp,
    onKeyClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit,
    onSpace: () -> Unit,
    onClose: () -> Unit
) {
    var isBanglaDigits by remember { mutableStateOf(false) }

    val digits = if (isBanglaDigits) {
        listOf("১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯", "০")
    } else {
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.background)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Top Toolbar: Digit Language Switcher & Exit
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Toggle English / Bengali digits
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.keyActionBackground)
                    .clickable { isBanglaDigits = !isBanglaDigits }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBanglaDigits) "বাংলা (১২৩)" else "English (123)",
                    color = palette.accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Exit back to letter keyboard
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.keyActionBackground)
                    .clickable { onClose() }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ABC",
                        color = palette.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit Number Pad",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Row 1: [ 1, 2, 3, + ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KeyButton(
                modifier = Modifier.weight(1f),
                label = digits[0],
                backgroundColor = palette.keyBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { onKeyClick(digits[0]) }
            )
            KeyButton(
                modifier = Modifier.weight(1f),
                label = digits[1],
                backgroundColor = palette.keyBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { onKeyClick(digits[1]) }
            )
            KeyButton(
                modifier = Modifier.weight(1f),
                label = digits[2],
                backgroundColor = palette.keyBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { onKeyClick(digits[2]) }
            )
            KeyButton(
                modifier = Modifier.weight(1f),
                label = "+",
                backgroundColor = palette.keyActionBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { onKeyClick("+") }
            )
        }

        // Row 2: [ 4, 5, 6, - ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KeyButton(
                modifier = Modifier.weight(1f),
                label = digits[3],
                backgroundColor = palette.keyBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { onKeyClick(digits[3]) }
            )
            KeyButton(
                modifier = Modifier.weight(1f),
                label = digits[4],
                backgroundColor = palette.keyBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { onKeyClick(digits[4]) }
            )
            KeyButton(
                modifier = Modifier.weight(1f),
                label = digits[5],
                backgroundColor = palette.keyBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { onKeyClick(digits[5]) }
            )
            KeyButton(
                modifier = Modifier.weight(1f),
                label = "-",
                backgroundColor = palette.keyActionBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { onKeyClick("-") }
            )
        }

        // Row 3: [ 7, 8, 9, * ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KeyButton(
                modifier = Modifier.weight(1f),
                label = digits[6],
                backgroundColor = palette.keyBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { onKeyClick(digits[6]) }
            )
            KeyButton(
                modifier = Modifier.weight(1f),
                label = digits[7],
                backgroundColor = palette.keyBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { onKeyClick(digits[7]) }
            )
            KeyButton(
                modifier = Modifier.weight(1f),
                label = digits[8],
                backgroundColor = palette.keyBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { onKeyClick(digits[8]) }
            )
            KeyButton(
                modifier = Modifier.weight(1f),
                label = "*",
                backgroundColor = palette.keyActionBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { onKeyClick("*") }
            )
        }

        // Row 4: [ ., 0, ⌫, ⏎ ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KeyButton(
                modifier = Modifier.weight(1f),
                label = if (isBanglaDigits) "।" else ".",
                backgroundColor = palette.keyActionBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { onKeyClick(if (isBanglaDigits) "।" else ".") }
            )
            KeyButton(
                modifier = Modifier.weight(1f),
                label = digits[9],
                backgroundColor = palette.keyBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = { onKeyClick(digits[9]) }
            )
            KeyButton(
                modifier = Modifier.weight(1f),
                label = "⌫",
                backgroundColor = palette.keyActionBackground,
                textColor = palette.textPrimary,
                height = keyHeight,
                onClick = onBackspace
            )
            KeyButton(
                modifier = Modifier.weight(1f),
                label = "⏎",
                backgroundColor = palette.accent,
                textColor = palette.background,
                height = keyHeight,
                onClick = onEnter
            )
        }
    }
}
