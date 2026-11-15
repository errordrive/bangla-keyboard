package com.openbangla.keyboard.ui.keyboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openbangla.keyboard.ui.theme.KeyboardPalette

private val EMOJI_CATEGORIES = listOf(
    "Smileys" to listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
        "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚",
        "😋", "😛", "😜", "🤪", "😝", "🤑", "🤗", "🤭", "🤫", "🤔",
        "🤐", "🤨", "😐", "😑", "😶", "😏", "😒", "🙄", "😬", "🤥",
        "😌", "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕", "🤢", "🤮",
        "🤧", "🥵", "🥶", "🥴", "😵", "🤯", "🤠", "🥳", "😎", "🤓",
        "🧐", "😕", "😟", "🙁", "☹️", "😮", "😯", "😲", "😳", "🥺",
        "😦", "😧", "😨", "😰", "😥", "😢", "😭", "😱", "😖", "😣",
        "😞", "😓", "😩", "😫", "🥱", "😤", "😡", "😠", "🤬", "😈",
        "💀", "☠️", "💩", "🤡", "👹", "👺", "👻", "👽", "👾", "🤖"
    ),
    "Gestures" to listOf(
        "👍", "👎", "👊", "✊", "🤛", "🤜", "🤞", "✌️", "🤟", "🤘",
        "👌", "🤏", "👈", "👉", "👆", "👇", "☝️", "✋", "🤚", "🖐️",
        "🖖", "👋", "🤙", "💪", "🦾", "🖕", "✍️", "🙏", "🦶", "🦵",
        "👂", "🦻", "👃", "🧠", "👀", "👁️", "👅", "👄", "💋", "❤️"
    ),
    "Animals & Nature" to listOf(
        "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯",
        "🦁", "🐮", "🐷", "🐸", "🐵", "🐔", "🐧", "🐦", "🐤", "🦆",
        "🦅", "🦉", "🦇", "🐺", "🐗", "🐴", "🦄", "🐝", "🐛", "🦋",
        "🐌", "🐞", "🐜", "🦟", "🦗", "🕷️", "🦂", "🐢", "🐍", "🦎",
        "🐙", "🦑", "🦐", "🦞", "🦀", "🐡", "🐠", "🐟", "🐬", "🐳",
        "🦈", "🐊", "🐅", "🐆", "🦓", "🦍", "🐘", "🦛", "🦏", "🐪",
        "🌲", "🌳", "🌴", "🌵", "🌾", "🌿", "☘️", "🍀", "🍁", "🍂",
        "🌸", "🌺", "🌻", "🌹", "🌷"
    ),
    "Food" to listOf(
        "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐",
        "🍈", "🍒", "🍑", "🥭", "🍍", "🥥", "🥝", "🍅", "🍆", "🥑",
        "🥦", "🥬", "🥒", "🌶️", "🌽", "🥕", "🥔", "🥐", "🍞", "🥖",
        "🧀", "🥚", "🍳", "🥞", "🧇", "🥓", "🍗", "🍖", "🌭", "🍔",
        "🍟", "🍕", "🥪", "🥙", "🌮", "🌯", "🥗", "🥘", "🍝", "🍜",
        "🍲", "🍛", "🍣", "🍱", "🥟", "🍤", "🍦", "🍧", "🍨", "🍩",
        "🍪", "🎂", "🍰", "🧁", "🍫", "🍬", "🍭", "☕", "🧃", "🥤"
    ),
    "Activities" to listOf(
        "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱",
        "🪀", "🏓", "🏸", "🏒", "🏏", "⛳", "🏹", "🎣", "🥊", "🥋",
        "🛹", "🛼", "🎿", "🏂", "🏋️", "🤼", "🤸", "⛹️", "🤺", "🏇",
        "🏆", "🥇", "🥈", "🥉", "🏅", "🎖️", "🎫", "🎟️", "🎪", "🎨",
        "🎬", "🎤", "🎧", "🎼", "🎹", "🥁", "🎷", "🎺", "🎸", "🎻"
    ),
    "Objects & Symbols" to listOf(
        "💡", "🔦", "🏮", "📱", "💻", "⌨️", "🖥️", "📷", "📸", "📹",
        "📺", "📻", "⏰", "⏱️", "⌛", "💰", "💵", "💸", "💳", "💎",
        "✉️", "📦", "✏️", "✒️", "📝", "📁", "📅", "🔒", "🔓", "🔑",
        "🔨", "🪓", "🔧", "🪛", "⚙️", "🔗", "❤️", "🧡", "💛", "💚",
        "💙", "💜", "🖤", "🤍", "🤎", "💔", "❣️", "💕", "💞", "💓",
        "💗", "💖", "💘", "💝", "☮️", "✝️", "☪️", "🕉️", "✡️", "☸️",
        "☯️", "☦️", "💯", "💢", "💥", "💫", "💦", "💨", "✨", "⭐"
    ),
    "Flags" to listOf(
        "🇧🇩", "🇮🇳", "🇵🇰", "🇺🇸", "🇬🇧", "🇨🇦", "🇦🇺", "🇸🇦", "🇦🇪", "🇲🇾",
        "🇸🇬", "🇯🇵", "🇰🇷", "🇨🇳", "🇩🇪", "🇫🇷", "🇮🇹", "🇪🇸", "🇧🇷", "🇦🇷",
        "🇹🇷", "🇪🇬", "🇿🇦", "🇳🇿", "🇮🇩", "🇲🇽", "🇷🇺", "🇳🇱", "🇨🇭", "🇸🇪"
    )
)

@Composable
fun InKeyboardEmojiView(
    palette: KeyboardPalette,
    totalHeight: Dp,
    onEmojiClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onSpace: () -> Unit,
    onClose: () -> Unit
) {
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val currentEmojis = EMOJI_CATEGORIES[selectedCategoryIndex].second

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
            .background(palette.background)
    ) {
        // Category Tabs Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.keyActionBackground.copy(alpha = 0.5f))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            EMOJI_CATEGORIES.forEachIndexed { index, (categoryName, emojis) ->
                val isSelected = index == selectedCategoryIndex
                val previewEmoji = emojis.firstOrNull() ?: "😊"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) palette.accent else palette.keyBackground)
                        .clickable { selectedCategoryIndex = index }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$previewEmoji $categoryName",
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) palette.background else palette.textPrimary
                    )
                }
            }
        }

        // Emoji Grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(currentEmojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onEmojiClick(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 22.sp
                        )
                    }
                }
            }
        }

        // Bottom Bar (ABC, Space, Backspace)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ABC switch back to letters
            Box(
                modifier = Modifier
                    .weight(1.5f)
                    .height(42.dp)
                    .shadow(1.5.dp, RoundedCornerShape(6.dp))
                    .clip(RoundedCornerShape(6.dp))
                    .background(palette.keyActionBackground)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ABC",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
            }

            // Spacebar
            Box(
                modifier = Modifier
                    .weight(4f)
                    .height(42.dp)
                    .shadow(1.5.dp, RoundedCornerShape(6.dp))
                    .clip(RoundedCornerShape(6.dp))
                    .background(palette.keyBackground)
                    .clickable(onClick = onSpace),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Space",
                    fontSize = 12.sp,
                    color = palette.textPrimary.copy(alpha = 0.7f)
                )
            }

            // Backspace
            Box(
                modifier = Modifier
                    .weight(1.5f)
                    .height(42.dp)
                    .shadow(1.5.dp, RoundedCornerShape(6.dp))
                    .clip(RoundedCornerShape(6.dp))
                    .background(palette.keyActionBackground)
                    .clickable(onClick = onBackspace),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⌫",
                    fontSize = 16.sp,
                    color = palette.textPrimary
                )
            }
        }
    }
}
