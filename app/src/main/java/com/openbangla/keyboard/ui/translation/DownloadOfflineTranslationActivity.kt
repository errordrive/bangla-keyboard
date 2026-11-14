package com.openbangla.keyboard.ui.translation

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.openbangla.keyboard.ui.theme.BanglaKeyboardTheme

class DownloadOfflineTranslationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BanglaKeyboardTheme {
                TranslationScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationScreen(onBack: () -> Unit) {
    var sourceLanguage by remember { mutableStateOf(TranslateLanguage.ENGLISH) }
    var targetLanguage by remember { mutableStateOf(TranslateLanguage.BENGALI) }
    var inputText by remember { mutableStateOf("") }
    var translatedText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }
    var modelStatus by remember { mutableStateOf("Offline translation model ready") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offline Translation", fontWeight = FontWeight.Bold) },
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
            // Language selector row
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (sourceLanguage == TranslateLanguage.ENGLISH) "English" else "বাংলা (Bengali)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    IconButton(onClick = {
                        val temp = sourceLanguage
                        sourceLanguage = targetLanguage
                        targetLanguage = temp
                        inputText = ""
                        translatedText = ""
                    }) {
                        Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "Swap Languages")
                    }

                    Text(
                        text = if (targetLanguage == TranslateLanguage.ENGLISH) "English" else "বাংলা (Bengali)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // Input box
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Enter text to translate") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )

            // Translate button
            Button(
                onClick = {
                    if (inputText.isBlank()) return@Button
                    isTranslating = true
                    modelStatus = "Downloading ML Kit model / Translating..."

                    val options = TranslatorOptions.Builder()
                        .setSourceLanguage(sourceLanguage)
                        .setTargetLanguage(targetLanguage)
                        .build()
                    val translator = Translation.getClient(options)
                    val conditions = DownloadConditions.Builder().build()

                    translator.downloadModelIfNeeded(conditions)
                        .addOnSuccessListener {
                            translator.translate(inputText)
                                .addOnSuccessListener { result ->
                                    translatedText = result
                                    isTranslating = false
                                    modelStatus = "Translation complete (Offline)"
                                }
                                .addOnFailureListener {
                                    translatedText = "Translation failed: ${it.localizedMessage}"
                                    isTranslating = false
                                }
                        }
                        .addOnFailureListener {
                            translatedText = "Model download error: ${it.localizedMessage}"
                            isTranslating = false
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isTranslating) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.height(20.dp))
                } else {
                    Text("Translate (Offline Engine)")
                }
            }

            // Output box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Translation Result:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = translatedText.ifEmpty { "Translation will appear here..." },
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text(
                text = "Status: $modelStatus",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
