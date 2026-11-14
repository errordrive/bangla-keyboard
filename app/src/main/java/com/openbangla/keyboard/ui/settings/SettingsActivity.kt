package com.openbangla.keyboard.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openbangla.keyboard.data.preferences.KeyboardTheme
import com.openbangla.keyboard.data.preferences.TypingLayout
import com.openbangla.keyboard.data.preferences.UserPreferences
import com.openbangla.keyboard.ui.theme.BanglaKeyboardTheme
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {

    private lateinit var preferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = UserPreferences(this)

        setContent {
            BanglaKeyboardTheme {
                SettingsScreen(
                    preferences = preferences,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val theme by preferences.themeFlow.collectAsState(initial = KeyboardTheme.LIGHT)
    val heightRatio by preferences.keyboardHeightFlow.collectAsState(initial = 1.0f)
    val vibration by preferences.vibrationFlow.collectAsState(initial = true)
    val sound by preferences.soundFlow.collectAsState(initial = false)
    val autoCorrect by preferences.autoCorrectFlow.collectAsState(initial = true)
    val suggestionStrip by preferences.suggestionStripFlow.collectAsState(initial = true)
    val activeLayout by preferences.activeLayoutFlow.collectAsState(initial = TypingLayout.AVRO)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keyboard Settings", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: Themes
            SectionTitle("Theme Selection")
            KeyboardTheme.entries.forEach { t ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { coroutineScope.launch { preferences.setTheme(t) } }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (theme == t),
                        onClick = { coroutineScope.launch { preferences.setTheme(t) } }
                    )
                    Text(text = t.displayName, fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp))
                }
            }

            Divider()

            // Section: Keyboard Height
            SectionTitle("Keyboard Height (${(heightRatio * 100).toInt()}%)")
            Slider(
                value = heightRatio,
                onValueChange = { coroutineScope.launch { preferences.setKeyboardHeight(it) } },
                valueRange = 0.8f..1.3f,
                steps = 5
            )

            Divider()

            // Section: Feedback & Typing
            SectionTitle("Feedback & Typing Preferences")

            ToggleRow("Vibration Feedback", vibration) {
                coroutineScope.launch { preferences.setVibration(it) }
            }

            ToggleRow("Sound on Keypress", sound) {
                coroutineScope.launch { preferences.setSound(it) }
            }

            ToggleRow("Phonetic Auto-Correction", autoCorrect) {
                coroutineScope.launch { preferences.setAutoCorrect(it) }
            }

            ToggleRow("Show Suggestion Strip", suggestionStrip) {
                coroutineScope.launch { preferences.setSuggestionStrip(it) }
            }

            Divider()

            // Section: Default Layout
            SectionTitle("Default Typing Mode")
            listOf(
                TypingLayout.AVRO to "Avro Phonetic (Phonetic Roman to Bengali)",
                TypingLayout.FIXED to "Fixed Bengali (Visual Layout)",
                TypingLayout.ENGLISH to "English QWERTY"
            ).forEach { (layout, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { coroutineScope.launch { preferences.setActiveLayout(layout) } }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (activeLayout == layout),
                        onClick = { coroutineScope.launch { preferences.setActiveLayout(layout) } }
                    )
                    Text(text = label, fontSize = 15.sp, modifier = Modifier.padding(start = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 16.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
