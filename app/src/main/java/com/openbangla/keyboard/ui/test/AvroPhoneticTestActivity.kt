package com.openbangla.keyboard.ui.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openbangla.keyboard.engine.AvroPhoneticParser
import com.openbangla.keyboard.ui.theme.BanglaKeyboardTheme

data class AvroTestCase(
    val romanInput: String,
    val expectedBengali: String,
    val actualBengali: String,
    val passed: Boolean
)

class AvroPhoneticTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BanglaKeyboardTheme {
                AvroTestScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvroTestScreen(onBack: () -> Unit) {
    var testInput by remember { mutableStateOf("ami banglay gan gai") }

    val testSuite = remember {
        val testCases = listOf(
            "ami" to "আমি",
            "amar" to "আমার",
            "amader" to "আমাদের",
            "tumi" to "তুমি",
            "apni" to "আপনি",
            "kemon" to "কেমন",
            "achho" to "আছো",
            "bhalo" to "ভালো",
            "bhalobashi" to "ভালোবাসি",
            "dhaka" to "ঢাকা",
            "bangla" to "বাংলা",
            "shonar" to "সোনার",
            "desh" to "দেশ",
            "kintu" to "কিন্তু",
            "dhonnobad" to "ধন্যবাদ",
            "kothay" to "কোথায়",
            "ebong" to "এবং",
            "shundor" to "সুন্দর",
            "shokal" to "সকাল",
            "shondha" to "সন্ধ্যা"
        )

        testCases.map { (input, expected) ->
            val actual = AvroPhoneticParser.parse(input)
            AvroTestCase(
                romanInput = input,
                expectedBengali = expected,
                actualBengali = actual,
                passed = (actual == expected)
            )
        }
    }

    val passCount = testSuite.count { it.passed }
    val totalCount = testSuite.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Avro Phonetic Debugger", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Interactive Sandbox
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Live Interactive Transliteration Test",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = testInput,
                        onValueChange = { testInput = it },
                        label = { Text("Type English Romanized text") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Parsed: ${AvroPhoneticParser.parse(testInput)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Test Suite Status Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (passCount == totalCount) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Automated Test Suite Status",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "$passCount / $totalCount Passed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (passCount == totalCount) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }

            // Results List
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(testSuite) { testCase ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "${testCase.romanInput} ➔ ${testCase.actualBengali}", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                            if (!testCase.passed) {
                                Text(text = "Expected: ${testCase.expectedBengali}", fontSize = 11.sp, color = Color.Red)
                            }
                        }

                        Icon(
                            imageVector = if (testCase.passed) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (testCase.passed) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }
            }
        }
    }
}
