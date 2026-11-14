package com.openbangla.keyboard.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openbangla.keyboard.ui.clipshare.ClipShareActivity
import com.openbangla.keyboard.ui.factcheck.FactCheckActivity
import com.openbangla.keyboard.ui.settings.SettingsActivity
import com.openbangla.keyboard.ui.store.StickerStoreActivity
import com.openbangla.keyboard.ui.test.AvroPhoneticTestActivity
import com.openbangla.keyboard.ui.theme.BanglaKeyboardTheme
import com.openbangla.keyboard.ui.translation.DownloadOfflineTranslationActivity

class MainActivity : ComponentActivity() {

    private val isEnabledState = mutableStateOf(false)
    private val isDefaultState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            refreshStatus()
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        try {
            setContent {
                BanglaKeyboardTheme {
                    MainAppScreen(
                        isImeEnabledInit = isEnabledState.value,
                        isImeDefaultInit = isDefaultState.value,
                        onEnableKeyboard = {
                            try {
                                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                            } catch (e: Throwable) {
                                e.printStackTrace()
                            }
                        },
                        onSelectDefaultKeyboard = {
                            try {
                                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                imm?.showInputMethodPicker()
                            } catch (e: Throwable) {
                                e.printStackTrace()
                            }
                        },
                        onOpenSettings = { launchSafely(SettingsActivity::class.java) },
                        onOpenStore = { launchSafely(StickerStoreActivity::class.java) },
                        onOpenTranslate = { launchSafely(DownloadOfflineTranslationActivity::class.java) },
                        onOpenFactCheck = { launchSafely(FactCheckActivity::class.java) },
                        onOpenClipShare = { launchSafely(ClipShareActivity::class.java) },
                        onOpenTutorial = { launchSafely(AvroPhoneticTestActivity::class.java) }
                    )
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            refreshStatus()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun refreshStatus() {
        isEnabledState.value = checkImeEnabled()
        isDefaultState.value = checkImeDefault()
    }

    private fun checkImeEnabled(): Boolean {
        return try {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.enabledInputMethodList?.any { it?.packageName == packageName } == true
        } catch (e: Throwable) {
            false
        }
    }

    private fun checkImeDefault(): Boolean {
        return try {
            val currentId = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
            currentId != null && currentId.contains(packageName)
        } catch (e: Throwable) {
            false
        }
    }

    private fun launchSafely(clazz: Class<*>) {
        try {
            startActivity(Intent(this, clazz))
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainAppScreen(
        isImeEnabledInit: Boolean,
        isImeDefaultInit: Boolean,
        onEnableKeyboard: () -> Unit,
        onSelectDefaultKeyboard: () -> Unit,
        onOpenSettings: () -> Unit,
        onOpenStore: () -> Unit,
        onOpenTranslate: () -> Unit,
        onOpenFactCheck: () -> Unit,
        onOpenClipShare: () -> Unit,
        onOpenTutorial: () -> Unit
    ) {
        var isEnabled by remember { mutableStateOf(isImeEnabledInit) }
        var isDefault by remember { mutableStateOf(isImeDefaultInit) }
        var selectedTab by remember { mutableIntStateOf(0) }

        LaunchedEffect(Unit) {
            try {
                isEnabled = checkImeEnabled()
                isDefault = checkImeDefault()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        val primaryGreen = Color(0xFF006C4C)
        val darkGreen = Color(0xFF004D36)

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("ক", color = primaryGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Bangla Keyboard", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                                Text("বাংলা ও ইংরেজি কীবোর্ড", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = primaryGreen
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("হোম") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryGreen,
                            selectedTextColor = primaryGreen,
                            indicatorColor = primaryGreen.copy(alpha = 0.15f)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            onOpenTutorial()
                        },
                        icon = { Icon(Icons.Default.School, contentDescription = "Tutorial") },
                        label = { Text("টিউটোরিয়াল") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryGreen,
                            selectedTextColor = primaryGreen,
                            indicatorColor = primaryGreen.copy(alpha = 0.15f)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                            onOpenSettings()
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("সেটিংস") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryGreen,
                            selectedTextColor = primaryGreen,
                            indicatorColor = primaryGreen.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFF6F8FA))
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Status Header / Setup Wizard
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(primaryGreen, darkGreen))
                        )
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    if (!isDefault) {
                        // Setup Wizard Card (like Ridmik's SetupWizardActivity)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "কীবোর্ড সেটআপ করুন",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E2124)
                                )
                                Text(
                                    text = "টাইপিং শুরু করতে নিচের ধাপগুলো সম্পন্ন করুন",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                                )

                                SetupStepItem(
                                    stepNum = "১",
                                    title = "কীবোর্ড সক্রিয় করুন",
                                    isDone = isEnabled,
                                    actionLabel = "চালু করুন",
                                    primaryColor = primaryGreen,
                                    onAction = onEnableKeyboard
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                SetupStepItem(
                                    stepNum = "২",
                                    title = "ডিফল্ট কীবোর্ড নির্বাচন করুন",
                                    isDone = isDefault,
                                    actionLabel = "বেছে নিন",
                                    primaryColor = primaryGreen,
                                    onAction = onSelectDefaultKeyboard
                                )
                            }
                        }
                    } else {
                        // Keyboard is fully active status banner
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE8F5E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = primaryGreen,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "কীবোর্ড সক্রিয় রয়েছে",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF1E2124)
                                    )
                                    Text(
                                        text = "যেকোনো অ্যাপে সরাসরি বাংলা টাইপ করতে পারেন",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                // Features Grid (Ridmik Keyboard feature items)
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ফিচার ও সেটিংস",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF263238),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Row 1: Themes & Layouts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RidmikFeatureCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.ColorLens,
                            iconBg = Color(0xFFE8F5E9),
                            iconTint = primaryGreen,
                            title = "কীবোর্ড থিম",
                            subtitle = "রঙ ও কাস্টমাইজেশন",
                            onClick = onOpenSettings
                        )
                        RidmikFeatureCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Keyboard,
                            iconBg = Color(0xFFE3F2FD),
                            iconTint = Color(0xFF1976D2),
                            title = "লেআউট",
                            subtitle = "অভ্র, সহজ, ইংরেজি",
                            onClick = onOpenSettings
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 2: Stickers & Offline Translate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RidmikFeatureCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Mood,
                            iconBg = Color(0xFFFFF3E0),
                            iconTint = Color(0xFFF57C00),
                            title = "স্টিকার ও মিম",
                            subtitle = "বাংলা স্টিকার স্টোর",
                            onClick = onOpenStore
                        )
                        RidmikFeatureCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Translate,
                            iconBg = Color(0xFFEDE7F6),
                            iconTint = Color(0xFF7E57C2),
                            title = "অফলাইন অনুবাদ",
                            subtitle = "ইংরেজি ⇄ বাংলা",
                            onClick = onOpenTranslate
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 3: Clipboard & Fact Check
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RidmikFeatureCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Assignment,
                            iconBg = Color(0xFFE0F7FA),
                            iconTint = Color(0xFF0097A7),
                            title = "ক্লিপবোর্ড",
                            subtitle = "সংরক্ষিত লেখা ও ইতিহাস",
                            onClick = onOpenClipShare
                        )
                        RidmikFeatureCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.FactCheck,
                            iconBg = Color(0xFFFFEBEE),
                            iconTint = Color(0xFFD32F2F),
                            title = "ফ্যাক্ট চেক",
                            subtitle = "গুজব যাচাইকরণ",
                            onClick = onOpenFactCheck
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 4: Tutorial & Settings
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RidmikFeatureCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.School,
                            iconBg = Color(0xFFF1F8E9),
                            iconTint = Color(0xFF689F38),
                            title = "টাইপিং টিউটোরিয়াল",
                            subtitle = "যুক্তাক্ষর প্র্যাকটিস",
                            onClick = onOpenTutorial
                        )
                        RidmikFeatureCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Settings,
                            iconBg = Color(0xFFECEFF1),
                            iconTint = Color(0xFF455A64),
                            title = "সেটিংস",
                            subtitle = "শব্দ, ভাইব্রেশন, সাইজ",
                            onClick = onOpenSettings
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun SetupStepItem(
        stepNum: String,
        title: String,
        isDone: Boolean,
        actionLabel: String,
        primaryColor: Color,
        onAction: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (isDone) Color(0xFFF1F8F5) else Color(0xFFF8F9FA))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isDone) primaryColor else Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text(stepNum, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color(0xFF263238)
                )
            }
            if (!isDone) {
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(actionLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text("✓ সম্পন্ন", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    @Composable
    private fun RidmikFeatureCard(
        modifier: Modifier = Modifier,
        icon: ImageVector,
        iconBg: Color,
        iconTint: Color,
        title: String,
        subtitle: String,
        onClick: () -> Unit
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            onClick = onClick
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E2124)
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
