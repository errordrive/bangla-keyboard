package com.openbangla.keyboard.ui.keyboard

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    onActionClick: (String) -> Unit
) {
    var showToolbarMenu by remember { mutableStateOf(false) }

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
            onClick = { showToolbarMenu = !showToolbarMenu },
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = "Keyboard Tools",
                tint = if (showToolbarMenu) palette.accent else palette.textPrimary.copy(alpha = 0.75f),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(2.dp))

        // Center Content: Either expanded Toolbar or Candidates
        if (showToolbarMenu || suggestions.isEmpty()) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolbarIcon(Icons.Default.ColorLens, "Theme", palette) {
                    showToolbarMenu = false
                    onActionClick("theme")
                }
                ToolbarIcon(Icons.Default.Dialpad, "Number Pad", palette) {
                    showToolbarMenu = false
                    onActionClick("numpad")
                }
                ToolbarIcon(Icons.Default.Mood, "Stickers", palette) {
                    showToolbarMenu = false
                    onActionClick("store")
                }
                ToolbarIcon(Icons.Default.Assignment, "Clipboard", palette) {
                    showToolbarMenu = false
                    onActionClick("clipboard")
                }
                ToolbarIcon(Icons.Default.Translate, "Translate", palette) {
                    showToolbarMenu = false
                    onActionClick("translate")
                }
                ToolbarIcon(Icons.Default.FactCheck, "Fact Check", palette) {
                    showToolbarMenu = false
                    onActionClick("factcheck")
                }
                ToolbarIcon(Icons.Default.Settings, "Settings", palette) {
                    showToolbarMenu = false
                    onActionClick("settings")
                }
            }
        } else {
            // Suggestion Chips (First candidate is primary)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                suggestions.forEachIndexed { index, suggestion ->
                    val isFirst = index == 0
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isFirst) palette.accent.copy(alpha = 0.2f) else palette.keyBackground)
                            .clickable { onSuggestionClick(suggestion) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
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

        // Right side Quick Layout Switcher button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(palette.keyActionBackground.copy(alpha = 0.6f))
                .clickable { onActionClick("layout_switch") }
                .padding(horizontal = 8.dp, vertical = 5.dp),
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
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = palette.accent
            )
        }
    }
}

@Composable
private fun ToolbarIcon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    palette: KeyboardPalette,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp)
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = palette.textPrimary.copy(alpha = 0.8f),
            modifier = Modifier.size(19.dp)
        )
    }
}
