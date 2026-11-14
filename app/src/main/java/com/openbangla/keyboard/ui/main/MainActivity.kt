package com.openbangla.keyboard.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openbangla.keyboard.monetization.AdMobManager
import com.openbangla.keyboard.ui.clipshare.ClipShareActivity
import com.openbangla.keyboard.ui.factcheck.FactCheckActivity
import com.openbangla.keyboard.ui.settings.SettingsActivity
import com.openbangla.keyboard.ui.store.StickerStoreActivity
import com.openbangla.keyboard.ui.test.AvroPhoneticTestActivity
import com.openbangla.keyboard.ui.theme.BanglaKeyboardTheme
import com.openbangla.keyboard.ui.translation.DownloadOfflineTranslationActivity

class MainActivity : ComponentActivity() {

    private lateinit var adMobManager: AdMobManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adMobManager = AdMobManager(this)

        setContent {
            BanglaKeyboardTheme {
                MainScreen(
                    onEnableKeyboard = {
                        startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                    },
                    onSelectDefaultKeyboard = {
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showInputMethodPicker()
                    },
                    onOpenSettings = { startActivity(Intent(this, SettingsActivity::class.java)) },
                    onOpenStore = { startActivity(Intent(this, StickerStoreActivity::class.java)) },
                    onOpenTranslate = { startActivity(Intent(this, DownloadOfflineTranslationActivity::class.java)) },
                    onOpenFactCheck = { startActivity(Intent(this, FactCheckActivity::class.java)) },
                    onOpenClipShare = { startActivity(Intent(this, ClipShareActivity::class.java)) },
                    onOpenTest = { startActivity(Intent(this, AvroPhoneticTestActivity::class.java)) }
                )
            }
        }
    }

    private fun isImeEnabled(): Boolean {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledList = imm.enabledInputMethodList
        return enabledList.any { it.packageName == packageName }
    }

    private fun isImeDefault(): Boolean {
        val currentId = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        return currentId != null && currentId.contains(packageName)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen(
        onEnableKeyboard: () -> Unit,
        onSelectDefaultKeyboard: () -> Unit,
        onOpenSettings: () -> Unit,
        onOpenStore: () -> Unit,
        onOpenTranslate: () -> Unit,
        onOpenFactCheck: () -> Unit,
        onOpenClipShare: () -> Unit,
        onOpenTest: () -> Unit
    ) {
        var isEnabled by remember { mutableStateOf(false) }
        var isDefault by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            isEnabled = isImeEnabled()
            isDefault = isImeDefault()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Bangla Keyboard", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Setup Wizard Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Keyboard Activation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        SetupStep(
                            stepNumber = 1,
                            title = "Enable in System Settings",
                            isComplete = isEnabled,
                            onAction = onEnableKeyboard
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SetupStep(
                            stepNumber = 2,
                            title = "Select as Default Keyboard",
                            isComplete = isDefault,
                            onAction = onSelectDefaultKeyboard
                        )
                    }
                }

                Text(
                    text = "Features & Utilities",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Feature grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Settings,
                        title = "Settings",
                        subtitle = "Themes, height & haptic",
                        onClick = onOpenSettings
                    )
                    FeatureCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Mood,
                        title = "Stickers & GIFs",
                        subtitle = "Store & Memes",
                        onClick = onOpenStore
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Translate,
                        title = "Offline Translation",
                        subtitle = "ML Kit Bengali ⇄ English",
                        onClick = onOpenTranslate
                    )
                    FeatureCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.FactCheck,
                        title = "Fact Check",
                        subtitle = "Verify news & rumors",
                        onClick = onOpenFactCheck
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Assignment,
                        title = "Clipboard",
                        subtitle = "Saved clips & snippets",
                        onClick = onOpenClipShare
                    )
                    FeatureCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.BugReport,
                        title = "Avro Debugger",
                        subtitle = "Phonetic test suite",
                        onClick = onOpenTest
                    )
                }
            }
        }
    }

    @Composable
    private fun SetupStep(
        stepNumber: Int,
        title: String,
        isComplete: Boolean,
        onAction: () -> Unit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = if (isComplete) Icons.Default.CheckCircle else Icons.Default.Keyboard,
                    contentDescription = null,
                    tint = if (isComplete) Color(0xFF00897B) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "  $stepNumber. $title",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            if (!isComplete) {
                Button(onClick = onAction) {
                    Text("Activate")
                }
            } else {
                Text("✓ Active", color = Color(0xFF00897B), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    @Composable
    private fun FeatureCard(
        modifier: Modifier = Modifier,
        icon: ImageVector,
        title: String,
        subtitle: String,
        onClick: () -> Unit
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            onClick = onClick
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
