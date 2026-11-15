package com.openbangla.keyboard.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openbangla.keyboard.data.preferences.TypingLayout
import com.openbangla.keyboard.ui.theme.KeyboardPalette

@Composable
fun SuggestionStrip(
    suggestions: List<String>,
    palette: KeyboardPalette,
    layoutMode: TypingLayout,
    onSuggestionClick: (String) -> Unit,
    onMenuClick: () -> Unit,
    onLayoutBadgeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(palette.background)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side Menu / Toolbar toggle button (Ridmik 4-dot / Apps icon)
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = "Keyboard Options",
                tint = palette.textPrimary.copy(alpha = 0.85f),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(2.dp))

        // Center Content: Predictive suggestions
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (suggestions.isEmpty()) {
                // Empty suggestion strip placeholder
                Spacer(modifier = Modifier.weight(1f))
            } else {
                suggestions.forEachIndexed { index, suggestion ->
                    val isFirst = index == 0
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isFirst) palette.accent.copy(alpha = 0.25f) else palette.keyBackground)
                            .clickable { onSuggestionClick(suggestion) }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = suggestion,
                            color = if (isFirst) palette.accent else palette.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = if (isFirst) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(2.dp))

        // Right side Quick Layout Switcher button (badge)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(palette.keyActionBackground.copy(alpha = 0.7f))
                .clickable(onClick = onLayoutBadgeClick)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            val label = when (layoutMode) {
                TypingLayout.ENGLISH -> "EN"
                TypingLayout.PROBHAT -> "প্রভাত"
                TypingLayout.SOHOJ -> "সহজ"
                TypingLayout.AVRO -> "Avro"
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = palette.accent
            )
        }
    }
}
