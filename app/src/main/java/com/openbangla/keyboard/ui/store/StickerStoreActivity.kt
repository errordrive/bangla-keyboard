package com.openbangla.keyboard.ui.store

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openbangla.keyboard.media.GifDecoder
import com.openbangla.keyboard.media.GifFrame
import com.openbangla.keyboard.monetization.BillingManager
import com.openbangla.keyboard.ui.theme.BanglaKeyboardTheme
import kotlinx.coroutines.delay

data class StickerPack(
    val id: String,
    val title: String,
    val category: String,
    val isPremium: Boolean,
    val previewEmoji: String
)

class StickerStoreActivity : ComponentActivity() {

    private lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        billingManager = BillingManager(this) { orderId ->
            Toast.makeText(this, "Purchase successful! Pack unlocked.", Toast.LENGTH_LONG).show()
        }
        billingManager.startConnection()

        setContent {
            BanglaKeyboardTheme {
                StickerStoreScreen(
                    onBack = { finish() },
                    onBuyPremium = { billingManager.launchBillingFlow(this) }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingManager.destroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickerStoreScreen(
    onBack: () -> Unit,
    onBuyPremium: () -> Unit
) {
    val categories = listOf("All", "Memes & Fun", "Bengali Idioms", "Festivals", "Daily Expressions")
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

    val allPacks = remember {
        listOf(
            StickerPack("p1", "Funny Cat Memes", "Memes & Fun", false, "🐱"),
            StickerPack("p2", "Classic Bengali Adda", "Bengali Idioms", false, "☕"),
            StickerPack("p3", "Boishakhi Celebration", "Festivals", false, "🎭"),
            StickerPack("p4", "Office Life Bengali", "Daily Expressions", false, "💼"),
            StickerPack("p5", "Animated Eid Specials", "Festivals", true, "🌙"),
            StickerPack("p6", "Pro Reaction Memes", "Memes & Fun", true, "🔥")
        )
    }

    val filteredPacks = if (selectedCategoryIndex == 0) {
        allPacks
    } else {
        allPacks.filter { it.category == categories[selectedCategoryIndex] }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sticker & Meme Store", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedCategoryIndex,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                categories.forEachIndexed { index, title ->
                    Tab(
                        selected = (selectedCategoryIndex == index),
                        onClick = { selectedCategoryIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Medium) }
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPacks) { pack ->
                    StickerPackCard(pack = pack, onBuyPremium = onBuyPremium)
                }
            }
        }
    }
}

@Composable
fun StickerPackCard(pack: StickerPack, onBuyPremium: () -> Unit) {
    var downloaded by remember { mutableStateOf(!pack.isPremium) }

    // Simulated Animated GIF preview using GifDecoder
    val gifFrames = remember {
        val decoder = GifDecoder()
        decoder.decode(byteArrayOf(0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x80.toByte(), 0x00, 0x80.toByte(), 0x00))
        decoder.frames
    }

    var currentFrameIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(gifFrames) {
        if (gifFrames.isNotEmpty()) {
            while (true) {
                delay(gifFrames[currentFrameIndex].delayMs.toLong())
                currentFrameIndex = (currentFrameIndex + 1) % gifFrames.size
            }
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                if (gifFrames.isNotEmpty()) {
                    Image(
                        bitmap = gifFrames[currentFrameIndex].bitmap.asImageBitmap(),
                        contentDescription = "Animated Sticker Preview",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(text = pack.previewEmoji, fontSize = 48.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = pack.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = pack.category, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(10.dp))

            if (pack.isPremium) {
                Button(
                    onClick = onBuyPremium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(" Unlock Pro", fontSize = 12.sp)
                }
            } else {
                OutlinedButton(
                    onClick = { downloaded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(if (downloaded) " Installed" else " Download", fontSize = 12.sp)
                }
            }
        }
    }
}
