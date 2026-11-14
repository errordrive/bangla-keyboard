package com.openbangla.keyboard.service

import android.content.Context
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.ui.platform.ComposeView
import com.openbangla.keyboard.R
import com.openbangla.keyboard.data.preferences.KeyboardTheme
import com.openbangla.keyboard.data.preferences.TypingLayout
import com.openbangla.keyboard.data.preferences.UserPreferences
import com.openbangla.keyboard.engine.AvroPhoneticParser
import com.openbangla.keyboard.engine.NativeAutoCorrectFixer
import com.openbangla.keyboard.engine.NativeLanguageEngine
import com.openbangla.keyboard.ui.clipshare.ClipShareActivity
import com.openbangla.keyboard.ui.factcheck.FactCheckActivity
import com.openbangla.keyboard.ui.keyboard.ComposeKeyboardView
import com.openbangla.keyboard.ui.keyboard.KeyboardLayoutState
import com.openbangla.keyboard.ui.settings.SettingsActivity
import com.openbangla.keyboard.ui.store.StickerStoreActivity
import com.openbangla.keyboard.ui.theme.BanglaKeyboardTheme
import com.openbangla.keyboard.ui.translation.DownloadOfflineTranslationActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BanglaInputMethodService : InputMethodService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val layoutState = KeyboardLayoutState()
    private val nativeEngine = NativeLanguageEngine()
    private val autoCorrectFixer = NativeAutoCorrectFixer()
    private lateinit var preferences: UserPreferences

    private var vibrator: Vibrator? = null
    private var vibrationEnabled = true
    private var isComposing = false
    private val recentWords = mutableListOf<String>()

    override fun onCreate() {
        super.onCreate()
        preferences = UserPreferences(this)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        // Load dictionary databases
        serviceScope.launch(Dispatchers.IO) {
            nativeEngine.loadDictionaryFromResource(this@BanglaInputMethodService, R.raw.main_bn)
            nativeEngine.loadDictionaryFromResource(this@BanglaInputMethodService, R.raw.main_en)
        }

        // Collect preferences
        serviceScope.launch {
            preferences.themeFlow.collect { theme ->
                layoutState.theme = theme
            }
        }
        serviceScope.launch {
            preferences.keyboardHeightFlow.collect { ratio ->
                layoutState.heightRatio = ratio
            }
        }
        serviceScope.launch {
            preferences.vibrationFlow.collect { enabled ->
                vibrationEnabled = enabled
            }
        }
        serviceScope.launch {
            preferences.autoCorrectFlow.collect { enabled ->
                layoutState.autoCorrectEnabled = enabled
            }
        }
        serviceScope.launch {
            preferences.activeLayoutFlow.collect { layout ->
                layoutState.layoutMode = layout
            }
        }
    }

    override fun onCreateInputView(): View {
        return ComposeView(this).apply {
            setContent {
                BanglaKeyboardTheme {
                    ComposeKeyboardView(
                        state = layoutState,
                        onKeyClick = { key -> handleKeyInput(key) },
                        onBackspace = { handleBackspace() },
                        onEnter = { handleEnter() },
                        onSpace = { handleSpace() },
                        onSuggestionClick = { suggestion -> commitSuggestion(suggestion) },
                        onActionClick = { action -> handleActionClick(action) }
                    )
                }
            }
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        layoutState.composingText = ""
        layoutState.suggestions = emptyList()
        isComposing = false
    }

    override fun onFinishInput() {
        super.onFinishInput()
        layoutState.composingText = ""
        layoutState.suggestions = emptyList()
        isComposing = false
    }

    private fun handleKeyInput(key: String) {
        performHapticFeedback()
        val ic = currentInputConnection ?: return

        when (layoutState.layoutMode) {
            TypingLayout.AVRO -> {
                // Buffer roman characters for Avro phonetic parsing
                if (key.length == 1 && (key[0].isLetter() || key[0] == '`' || key[0] == '^' || key[0] == ':')) {
                    layoutState.composingText += key
                    val transliterated = AvroPhoneticParser.parse(layoutState.composingText)
                    ic.setComposingText(transliterated, 1)
                    isComposing = true
                    updateSuggestions(transliterated)
                } else {
                    commitComposingText()
                    ic.commitText(key, 1)
                }
            }
            TypingLayout.FIXED, TypingLayout.ENGLISH -> {
                if (layoutState.layoutMode == TypingLayout.ENGLISH && key.length == 1 && key[0].isLetter()) {
                    layoutState.composingText += key
                    ic.setComposingText(layoutState.composingText, 1)
                    isComposing = true
                    updateSuggestions(layoutState.composingText)
                } else {
                    commitComposingText()
                    ic.commitText(key, 1)
                }
            }
        }
    }

    private fun handleBackspace() {
        performHapticFeedback()
        val ic = currentInputConnection ?: return

        if (isComposing && layoutState.composingText.isNotEmpty()) {
            layoutState.composingText = layoutState.composingText.dropLast(1)
            if (layoutState.composingText.isEmpty()) {
                ic.commitText("", 1)
                isComposing = false
                layoutState.suggestions = emptyList()
            } else {
                val transliterated = if (layoutState.layoutMode == TypingLayout.AVRO) {
                    AvroPhoneticParser.parse(layoutState.composingText)
                } else {
                    layoutState.composingText
                }
                ic.setComposingText(transliterated, 1)
                updateSuggestions(transliterated)
            }
        } else {
            ic.deleteSurroundingText(1, 0)
        }
    }

    private fun handleSpace() {
        performHapticFeedback()
        val ic = currentInputConnection ?: return

        if (isComposing) {
            var finalWord = if (layoutState.layoutMode == TypingLayout.AVRO) {
                AvroPhoneticParser.parse(layoutState.composingText)
            } else {
                layoutState.composingText
            }

            // Check auto-correction
            if (layoutState.autoCorrectEnabled) {
                val corrected = autoCorrectFixer.getAutoCorrection(layoutState.composingText)
                if (!corrected.isNullOrBlank()) {
                    finalWord = corrected
                }
            }

            ic.commitText(finalWord, 1)
            recentWords.add(finalWord)
            if (recentWords.size > 5) recentWords.removeAt(0)
            nativeEngine.updateContext(recentWords)

            layoutState.composingText = ""
            layoutState.suggestions = emptyList()
            isComposing = false
        }

        ic.commitText(" ", 1)
    }

    private fun handleEnter() {
        performHapticFeedback()
        val ic = currentInputConnection ?: return
        commitComposingText()

        val editorInfo = currentInputEditorInfo
        val imeAction = editorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
        if (imeAction != null && imeAction != EditorInfo.IME_ACTION_NONE && imeAction != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(imeAction)
        } else {
            ic.commitText("\n", 1)
        }
    }

    private fun commitSuggestion(suggestion: String) {
        val ic = currentInputConnection ?: return
        ic.commitText(suggestion + " ", 1)
        layoutState.composingText = ""
        layoutState.suggestions = emptyList()
        isComposing = false
    }

    private fun commitComposingText() {
        if (isComposing) {
            val ic = currentInputConnection ?: return
            val text = if (layoutState.layoutMode == TypingLayout.AVRO) {
                AvroPhoneticParser.parse(layoutState.composingText)
            } else {
                layoutState.composingText
            }
            ic.commitText(text, 1)
            layoutState.composingText = ""
            layoutState.suggestions = emptyList()
            isComposing = false
        }
    }

    private fun updateSuggestions(query: String) {
        serviceScope.launch(Dispatchers.Default) {
            val list = mutableListOf<String>()

            // 1. Check autocorrect fixer
            val fix = autoCorrectFixer.getAutoCorrection(layoutState.composingText)
            if (!fix.isNullOrBlank() && fix != query) {
                list.add(fix)
            }

            // 2. Add current parsed representation
            if (!list.contains(query)) {
                list.add(query)
            }

            // 3. Query native Trie and N-gram suggestions
            val trieSuggestions = nativeEngine.getSuggestions(query, maxCount = 4)
            for (s in trieSuggestions) {
                if (!list.contains(s)) {
                    list.add(s)
                }
            }

            layoutState.suggestions = list.take(5)
        }
    }

    private fun handleActionClick(action: String) {
        when (action) {
            "theme" -> cycleTheme()
            "settings" -> launchActivity(SettingsActivity::class.java)
            "stickers" -> launchActivity(StickerStoreActivity::class.java)
            "translate" -> launchActivity(DownloadOfflineTranslationActivity::class.java)
            "factcheck" -> launchActivity(FactCheckActivity::class.java)
            "clipboard" -> launchActivity(ClipShareActivity::class.java)
        }
    }

    private fun cycleTheme() {
        serviceScope.launch {
            val currentTheme = preferences.themeFlow.first()
            val allThemes = KeyboardTheme.entries
            val nextIndex = (allThemes.indexOf(currentTheme) + 1) % allThemes.size
            preferences.setTheme(allThemes[nextIndex])
        }
    }

    private fun launchActivity(clazz: Class<*>) {
        val intent = Intent(this, clazz).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    private fun performHapticFeedback() {
        if (!vibrationEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(15)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
