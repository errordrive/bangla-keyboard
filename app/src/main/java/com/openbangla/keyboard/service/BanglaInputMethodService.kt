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
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
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

class BanglaInputMethodService : InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = store

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
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        preferences = UserPreferences(this)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        // Load dictionary databases safely in background
        serviceScope.launch(Dispatchers.IO) {
            try {
                nativeEngine.loadDictionaryFromResource(this@BanglaInputMethodService, R.raw.main_bn)
                nativeEngine.loadDictionaryFromResource(this@BanglaInputMethodService, R.raw.main_en)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        // Collect preferences
        serviceScope.launch {
            try {
                preferences.themeFlow.collect { theme -> layoutState.theme = theme }
            } catch (e: Throwable) { e.printStackTrace() }
        }
        serviceScope.launch {
            try {
                preferences.keyboardHeightFlow.collect { ratio -> layoutState.heightRatio = ratio }
            } catch (e: Throwable) { e.printStackTrace() }
        }
        serviceScope.launch {
            try {
                preferences.vibrationFlow.collect { enabled -> vibrationEnabled = enabled }
            } catch (e: Throwable) { e.printStackTrace() }
        }
        serviceScope.launch {
            try {
                preferences.autoCorrectFlow.collect { enabled -> layoutState.autoCorrectEnabled = enabled }
            } catch (e: Throwable) { e.printStackTrace() }
        }
        serviceScope.launch {
            try {
                preferences.activeLayoutFlow.collect { layout -> layoutState.layoutMode = layout }
            } catch (e: Throwable) { e.printStackTrace() }
        }
    }

    override fun onCreateInputView(): View {
        // Set lifecycle and saved state owners on decor view so Compose window recomposer works
        window?.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeViewModelStoreOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        }

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
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

        // Also explicitly attach ViewTree owners to ComposeView itself
        composeView.setViewTreeLifecycleOwner(this)
        composeView.setViewTreeViewModelStoreOwner(this)
        composeView.setViewTreeSavedStateRegistryOwner(this)

        return composeView
    }

    override fun onWindowShown() {
        super.onWindowShown()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        serviceScope.cancel()
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
            TypingLayout.FIXED -> {
                commitComposingText()
                ic.commitText(key, 1)
            }
            TypingLayout.ENGLISH -> {
                layoutState.composingText += key
                ic.setComposingText(layoutState.composingText, 1)
                isComposing = true
                updateSuggestions(layoutState.composingText)
            }
        }
    }

    private fun handleBackspace() {
        performHapticFeedback()
        val ic = currentInputConnection ?: return

        if (layoutState.composingText.isNotEmpty()) {
            layoutState.composingText = layoutState.composingText.dropLast(1)
            if (layoutState.composingText.isEmpty()) {
                ic.commitText("", 1)
                isComposing = false
                layoutState.suggestions = emptyList()
            } else {
                val parsed = if (layoutState.layoutMode == TypingLayout.AVRO) {
                    AvroPhoneticParser.parse(layoutState.composingText)
                } else {
                    layoutState.composingText
                }
                ic.setComposingText(parsed, 1)
                updateSuggestions(parsed)
            }
        } else {
            ic.deleteSurroundingText(1, 0)
        }
    }

    private fun handleSpace() {
        performHapticFeedback()
        val ic = currentInputConnection ?: return

        if (isComposing && layoutState.composingText.isNotEmpty()) {
            val candidateToCommit = if (layoutState.autoCorrectEnabled) {
                val autocorrected = autoCorrectFixer.getAutoCorrection(layoutState.composingText)
                autocorrected ?: if (layoutState.layoutMode == TypingLayout.AVRO) {
                    AvroPhoneticParser.parse(layoutState.composingText)
                } else {
                    layoutState.composingText
                }
            } else {
                if (layoutState.layoutMode == TypingLayout.AVRO) {
                    AvroPhoneticParser.parse(layoutState.composingText)
                } else {
                    layoutState.composingText
                }
            }
            ic.commitText("$candidateToCommit ", 1)
            recentWords.add(candidateToCommit)
            layoutState.composingText = ""
            layoutState.suggestions = emptyList()
            isComposing = false
        } else {
            ic.commitText(" ", 1)
        }
    }

    private fun handleEnter() {
        performHapticFeedback()
        commitComposingText()
        val ic = currentInputConnection ?: return
        val editorInfo = currentInputEditorInfo
        val imeAction = (editorInfo?.imeOptions ?: 0) and EditorInfo.IME_MASK_ACTION

        if (imeAction != EditorInfo.IME_ACTION_NONE && imeAction != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(imeAction)
        } else {
            ic.commitText("\n", 1)
        }
    }

    private fun commitSuggestion(suggestion: String) {
        performHapticFeedback()
        val ic = currentInputConnection ?: return
        ic.commitText("$suggestion ", 1)
        recentWords.add(suggestion)
        layoutState.composingText = ""
        layoutState.suggestions = emptyList()
        isComposing = false
    }

    private fun commitComposingText() {
        if (!isComposing || layoutState.composingText.isEmpty()) return
        val ic = currentInputConnection ?: return
        val text = if (layoutState.layoutMode == TypingLayout.AVRO) {
            AvroPhoneticParser.parse(layoutState.composingText)
        } else {
            layoutState.composingText
        }
        ic.commitText(text, 1)
        recentWords.add(text)
        layoutState.composingText = ""
        layoutState.suggestions = emptyList()
        isComposing = false
    }

    private fun updateSuggestions(query: String) {
        if (query.isBlank()) {
            layoutState.suggestions = emptyList()
            return
        }

        serviceScope.launch(Dispatchers.Default) {
            try {
                val predictions = nativeEngine.getSuggestions(query, maxCount = 4)
                val autocorrect = if (layoutState.autoCorrectEnabled) {
                    autoCorrectFixer.getAutoCorrection(layoutState.composingText)
                } else null

                val combined = mutableListOf<String>()
                if (autocorrect != null && !combined.contains(autocorrect)) {
                    combined.add(autocorrect)
                }
                combined.add(query)
                for (p in predictions) {
                    if (!combined.contains(p)) {
                        combined.add(p)
                    }
                }
                launch(Dispatchers.Main) {
                    layoutState.suggestions = combined.take(5)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private fun handleActionClick(action: String) {
        when (action) {
            "store" -> launchActivity(StickerStoreActivity::class.java)
            "translate" -> launchActivity(DownloadOfflineTranslationActivity::class.java)
            "factcheck" -> launchActivity(FactCheckActivity::class.java)
            "clipboard" -> launchActivity(ClipShareActivity::class.java)
            "settings" -> launchActivity(SettingsActivity::class.java)
            "theme" -> {
                // Cycle themes quickly from keyboard
                val nextTheme = when (layoutState.theme) {
                    KeyboardTheme.EMERALD -> KeyboardTheme.DARK
                    KeyboardTheme.DARK -> KeyboardTheme.AMOLED
                    KeyboardTheme.AMOLED -> KeyboardTheme.SLATE
                    KeyboardTheme.SLATE -> KeyboardTheme.LIGHT
                    KeyboardTheme.LIGHT -> KeyboardTheme.EMERALD
                }
                serviceScope.launch {
                    preferences.setTheme(nextTheme)
                }
            }
            "layout_switch" -> {
                val nextLayout = when (layoutState.layoutMode) {
                    TypingLayout.AVRO -> TypingLayout.ENGLISH
                    TypingLayout.ENGLISH -> TypingLayout.FIXED
                    TypingLayout.FIXED -> TypingLayout.AVRO
                }
                serviceScope.launch {
                    preferences.setActiveLayout(nextLayout)
                }
            }
        }
    }

    private fun launchActivity(clazz: Class<*>) {
        val intent = Intent(this, clazz).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun performHapticFeedback() {
        if (!vibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
