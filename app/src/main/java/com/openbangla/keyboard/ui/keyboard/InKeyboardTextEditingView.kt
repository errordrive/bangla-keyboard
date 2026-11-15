package com.openbangla.keyboard.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.LastPage
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openbangla.keyboard.ui.theme.KeyboardPalette

@Composable
fun InKeyboardTextEditingView(
    palette: KeyboardPalette,
    totalHeight: Dp,
    onMoveLeft: (selectMode: Boolean) -> Unit,
    onMoveRight: (selectMode: Boolean) -> Unit,
    onMoveUp: (selectMode: Boolean) -> Unit,
    onMoveDown: (selectMode: Boolean) -> Unit,
    onMoveHome: (selectMode: Boolean) -> Unit,
    onMoveEnd: (selectMode: Boolean) -> Unit,
    onSelectAll: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onPaste: () -> Unit,
    onClose: () -> Unit
) {
    var isSelecting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
            .background(palette.background)
            .padding(8.dp)
    ) {
        // Main Controller Area: Left Side navigation pad, Right Side clipboard actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Directional Pad & Home/End (Left Column - weight 1.4)
            Column(
                modifier = Modifier
                    .weight(1.4f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Top: Up Arrow
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.Center
                ) {
                    EditingIconKey(
                        icon = Icons.Default.ArrowUpward,
                        palette = palette,
                        modifier = Modifier.weight(1f),
                        onClick = { onMoveUp(isSelecting) }
                    )
                }

                // Middle: Left, Select Mode Toggle, Right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EditingIconKey(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        palette = palette,
                        modifier = Modifier.weight(1f),
                        onClick = { onMoveLeft(isSelecting) }
                    )

                    // Center SELECT Button
                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight()
                            .shadow(1.5.dp, RoundedCornerShape(6.dp))
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelecting) palette.accent else palette.keyActionBackground)
                            .clickable { isSelecting = !isSelecting },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isSelecting) "SELECTING" else "SELECT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelecting) palette.background else palette.textPrimary
                        )
                    }

                    EditingIconKey(
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        palette = palette,
                        modifier = Modifier.weight(1f),
                        onClick = { onMoveRight(isSelecting) }
                    )
                }

                // Bottom: Down Arrow
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.Center
                ) {
                    EditingIconKey(
                        icon = Icons.Default.ArrowDownward,
                        palette = palette,
                        modifier = Modifier.weight(1f),
                        onClick = { onMoveDown(isSelecting) }
                    )
                }
            }

            // Action Buttons (Right Column - weight 1f)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EditingTextKey("Select All", palette, Modifier.weight(1f), onSelectAll)
                    EditingTextKey("Cut", palette, Modifier.weight(1f), onCut)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EditingTextKey("Copy", palette, Modifier.weight(1f), onCopy)
                    EditingTextKey("Paste", palette, Modifier.weight(1f), onPaste)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EditingIconKey(Icons.Default.FirstPage, palette, Modifier.weight(1f)) {
                        onMoveHome(isSelecting)
                    }
                    EditingIconKey(Icons.Default.LastPage, palette, Modifier.weight(1f)) {
                        onMoveEnd(isSelecting)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Bottom Bar: Close button back to keyboard
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(38.dp)
                    .shadow(1.5.dp, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.keyActionBackground)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                        contentDescription = "Return",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Back to Keyboard",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = palette.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun EditingIconKey(
    icon: ImageVector,
    palette: KeyboardPalette,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .shadow(1.5.dp, RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .background(palette.keyBackground)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = palette.textPrimary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun EditingTextKey(
    text: String,
    palette: KeyboardPalette,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .shadow(1.5.dp, RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .background(palette.keyBackground)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = palette.textPrimary
        )
    }
}
