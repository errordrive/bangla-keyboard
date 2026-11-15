package com.openbangla.keyboard.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openbangla.keyboard.data.preferences.KeyboardTheme
import com.openbangla.keyboard.ui.theme.KeyboardPalette

@Composable
fun InKeyboardMoreOptionsView(
    palette: KeyboardPalette,
    totalHeight: Dp,
    currentTheme: KeyboardTheme,
    onSelectTheme: (KeyboardTheme) -> Unit,
    onOpenClipboard: () -> Unit,
    onOpenNumberPad: () -> Unit,
    onOpenTextEditing: () -> Unit,
    onOpenTranslate: () -> Unit,
    onOpenFactCheck: () -> Unit,
    onOpenStore: () -> Unit,
    onOpenSettings: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
            .background(palette.background)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header with Title and Close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Options & Tools",
                color = palette.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = palette.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Theme Selection Chips (Direct preview switcher)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeyboardTheme.entries.forEach { theme ->
                val isSelected = theme == currentTheme
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) palette.accent else palette.keyBackground)
                        .clickable { onSelectTheme(theme) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = theme.displayName,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) palette.background else palette.textPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Grid of Ridmik Action Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OptionTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Edit,
                title = "Text Editing",
                palette = palette,
                onClick = onOpenTextEditing
            )
            OptionTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.ContentPaste,
                title = "Clipboard",
                palette = palette,
                onClick = onOpenClipboard
            )
            OptionTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Dialpad,
                title = "Number Pad",
                palette = palette,
                onClick = onOpenNumberPad
            )
            OptionTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Translate,
                title = "Translate",
                palette = palette,
                onClick = onOpenTranslate
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OptionTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Mood,
                title = "Stickers",
                palette = palette,
                onClick = onOpenStore
            )
            OptionTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.FactCheck,
                title = "Fact Check",
                palette = palette,
                onClick = onOpenFactCheck
            )
            OptionTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.ColorLens,
                title = "Themes",
                palette = palette,
                onClick = {
                    val nextTheme = when (currentTheme) {
                        KeyboardTheme.RIDMIK_GREEN -> KeyboardTheme.LIGHT
                        KeyboardTheme.LIGHT -> KeyboardTheme.DARK
                        KeyboardTheme.DARK -> KeyboardTheme.AMOLED
                        KeyboardTheme.AMOLED -> KeyboardTheme.DARK_SILVER
                        KeyboardTheme.DARK_SILVER -> KeyboardTheme.MIDNIGHT_BLACK
                        KeyboardTheme.MIDNIGHT_BLACK -> KeyboardTheme.RIDMIK_GREEN
                    }
                    onSelectTheme(nextTheme)
                }
            )
            OptionTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Settings,
                title = "Settings",
                palette = palette,
                onClick = onOpenSettings
            )
        }
    }
}

@Composable
private fun OptionTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    palette: KeyboardPalette,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(palette.keyBackground)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(palette.keyActionBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = palette.accent,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = palette.textPrimary,
            maxLines = 1
        )
    }
}
