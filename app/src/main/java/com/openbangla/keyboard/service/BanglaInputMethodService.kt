package com.openbangla.keyboard.service

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.openbangla.keyboard.BanglaApplication
import com.openbangla.keyboard.R
import com.openbangla.keyboard.data.db.AppDatabase
import com.openbangla.keyboard.data.db.ClipEntity
import com.openbangla.keyboard.data.preferences.KeyboardTheme
import com.openbangla.keyboard.data.preferences.TypingLayout
import com.openbangla.keyboard.data.preferences.UserPreferences
import com.openbangla.keyboard.engine.AvroPhoneticParser
import com.openbangla.keyboard.engine.EnglishLayout
import com.openbangla.keyboard.engine.FixedBanglaLayout
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
    private val clipDao by lazy {
        (applicationContext as? BanglaApplication)?.database?.clipDao()
            ?: AppDatabase.getDatabase(applicationContext).clipDao()
    }

    private var vibrator: Vibrator? = null
    private var vibrationEnabled = true
    private var isComposing = false
    private val recentWords = mutableListOf<String>()

    override fun onCreate() {
        super.onCreate()
        try {
            savedStateRegistryController.performAttach()
            savedStateRegistryController.performRestore(null)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        try {
            preferences = UserPreferences(this)
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Throwable) {
            e.printStackTrace()
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

        // Collect preferences safely
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

    override fun onConfigureWindow(win: Window, isFullscreen: Boolean, isCandidatesOnly: Boolean) {
        super.onConfigureWindow(win, isFullscreen, isCandidatesOnly)
        try {
            win.decorView.let { decor ->
                decor.setViewTreeLifecycleOwner(this)
                decor.setViewTreeViewModelStoreOwner(this)
                decor.setViewTreeSavedStateRegistryOwner(this)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onCreateInputView(): View {
        return try {
            window?.window?.decorView?.let { decorView ->
                decorView.setViewTreeLifecycleOwner(this)
                decorView.setViewTreeViewModelStoreOwner(this)
                decorView.setViewTreeSavedStateRegistryOwner(this)
            }

            if (lifecycleRegistry.currentState != Lifecycle.State.RESUMED) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }

            val rootLayout = FrameLayout(this).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setViewTreeLifecycleOwner(this@BanglaInputMethodService)
                setViewTreeViewModelStoreOwner(this@BanglaInputMethodService)
                setViewTreeSavedStateRegistryOwner(this@BanglaInputMethodService)
            }

            val composeView = ComposeView(this).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
                setViewTreeLifecycleOwner(this@BanglaInputMethodService)
                setViewTreeViewModelStoreOwner(this@BanglaInputMethodService)
                setViewTreeSavedStateRegistryOwner(this@BanglaInputMethodService)

                setContent {
                    BanglaKeyboardTheme {
                        val clips by clipDao.getAllClips().collectAsState(initial = emptyList())
                        ComposeKeyboardView(
                            state = layoutState,
                            clips = clips,
                            onKeyClick = { key -> handleKeyInput(key) },
                            onBackspace = { handleBackspace() },
                            onEnter = { handleEnter() },
                            onSpace = { handleSpace() },
                            onSuggestionClick = { suggestion -> commitSuggestion(suggestion) },
                            onActionClick = { action -> handleActionClick(action) },
                            onDeleteClip = { clip -> serviceScope.launch(Dispatchers.IO) { clipDao.deleteClip(clip) } },
                            onClearAllClips = { serviceScope.launch(Dispatchers.IO) { clipDao.clearAllUnpinned() } },
                            onSwipeLeft = { cycleLayout(true) },
                            onSwipeRight = { cycleLayout(false) }
                        )
                    }
                }
            }

            rootLayout.addView(composeView)
            rootLayout
        } catch (t: Throwable) {
            t.printStackTrace()
            createFallbackKeyboardView()
        }
    }

    override fun onWindowShown() {
        super.onWindowShown()
        try {
            window?.window?.decorView?.let { decorView ->
                decorView.setViewTreeLifecycleOwner(this)
                decorView.setViewTreeViewModelStoreOwner(this)
                decorView.setViewTreeSavedStateRegistryOwner(this)
            }
            if (lifecycleRegistry.currentState != Lifecycle.State.RESUMED) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        try {
            if (lifecycleRegistry.currentState == Lifecycle.State.RESUMED) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            store.clear()
            serviceScope.cancel()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        layoutState.composingText = ""
        layoutState.suggestions = emptyList()
        isComposing = false
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        syncSystemClipboard()
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
            TypingLayout.ENGLISH -> {
                val charToInsert = if (layoutState.isShifted && key.length == 1) key.uppercase() else key
                layoutState.composingText += charToInsert
                ic.setComposingText(layoutState.composingText, 1)
                isComposing = true
                updateSuggestions(layoutState.composingText)
                layoutState.onCharacterTyped()
            }
            TypingLayout.PROBHAT, TypingLayout.SOHOJ -> {
                commitComposingText()
                ic.commitText(key, 1)
                layoutState.onCharacterTyped()
            }
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
                layoutState.onCharacterTyped()
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
            "clipboard" -> {
                syncSystemClipboard()
                layoutState.toggleClipboard()
            }
            "numpad" -> {
                layoutState.toggleNumberPad()
            }
            "settings" -> launchActivity(SettingsActivity::class.java)
            "theme" -> {
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
                cycleLayout(true)
            }
        }
    }

    private fun cycleLayout(forward: Boolean) {
        performHapticFeedback()
        commitComposingText()
        layoutState.switchLayout(forward)
        serviceScope.launch {
            preferences.setActiveLayout(layoutState.layoutMode)
        }
    }

    private fun syncSystemClipboard() {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager ?: return
            val primaryClip = clipboard.primaryClip
            if (primaryClip != null && primaryClip.itemCount > 0) {
                val itemText = primaryClip.getItemAt(0)?.coerceToText(this)?.toString()
                if (!itemText.isNullOrBlank()) {
                    serviceScope.launch(Dispatchers.IO) {
                        clipDao.insertClip(ClipEntity(text = itemText, timestamp = System.currentTimeMillis()))
                    }
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun launchActivity(clazz: Class<*>) {
        try {
            val intent = Intent(this, clazz).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
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

    /**
     * Native Android View keyboard fallback to guarantee zero IME crashes
     * even if Compose encounters an unexpected platform error.
     */
    private fun createFallbackKeyboardView(): View {
        val density = resources.displayMetrics.density
        fun dp(value: Int): Int = (value * density).toInt()

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AndroidColor.parseColor("#004D36"))
            setPadding(dp(4), dp(4), dp(4), dp(8))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Top Suggestion Strip
        val strip = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(42)
            )
        }

        val toolBtn = TextView(this).apply {
            text = "▦"
            textSize = 18f
            setTextColor(AndroidColor.WHITE)
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(4), dp(8), dp(4))
            setOnClickListener { handleActionClick("settings") }
        }
        strip.addView(toolBtn)

        val suggestionScroll = HorizontalScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            isHorizontalScrollBarEnabled = false
        }
        val suggestionHolder = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        suggestionScroll.addView(suggestionHolder)
        strip.addView(suggestionScroll)

        val modeBadge = TextView(this).apply {
            text = when (layoutState.layoutMode) {
                TypingLayout.ENGLISH -> "EN"
                TypingLayout.PROBHAT -> "প্রভাত"
                TypingLayout.SOHOJ -> "সহজ"
                TypingLayout.AVRO -> "Avro"
            }
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            setTextColor(AndroidColor.parseColor("#A7F3D0"))
            setPadding(dp(8), dp(4), dp(8), dp(4))
            setOnClickListener {
                handleActionClick("layout_switch")
                text = when (layoutState.layoutMode) {
                    TypingLayout.ENGLISH -> "EN"
                    TypingLayout.PROBHAT -> "প্রভাত"
                    TypingLayout.SOHOJ -> "সহজ"
                    TypingLayout.AVRO -> "Avro"
                }
            }
        }
        strip.addView(modeBadge)
        mainLayout.addView(strip)

        // Key rows
        fun createKey(label: String, weight: Float, action: () -> Unit): TextView {
            return TextView(this).apply {
                text = label
                textSize = if (label.length > 2) 11f else 16f
                setTextColor(AndroidColor.WHITE)
                gravity = Gravity.CENTER
                typeface = Typeface.DEFAULT_BOLD

                val bg = GradientDrawable().apply {
                    setColor(AndroidColor.parseColor("#006C4C"))
                    cornerRadius = dp(6).toFloat()
                }
                background = bg

                val lp = LinearLayout.LayoutParams(0, dp(46), weight).apply {
                    setMargins(dp(2), dp(3), dp(2), dp(3))
                }
                layoutParams = lp
                setOnClickListener { action() }
            }
        }

        val rows = listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            listOf("z", "x", "c", "v", "b", "n", "m")
        )

        // Row 1
        val r1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        rows[0].forEach { ch -> r1.addView(createKey(ch, 1f) { handleKeyInput(ch) }) }
        mainLayout.addView(r1)

        // Row 2
        val r2 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        r2.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(0, 0, 0.5f) })
        rows[1].forEach { ch -> r2.addView(createKey(ch, 1f) { handleKeyInput(ch) }) }
        r2.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(0, 0, 0.5f) })
        mainLayout.addView(r2)

        // Row 3
        val r3 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        r3.addView(createKey("⇧", 1.4f) { layoutState.toggleShift() })
        rows[2].forEach { ch -> r3.addView(createKey(ch, 1f) { handleKeyInput(ch) }) }
        r3.addView(createKey("⌫", 1.4f) { handleBackspace() })
        mainLayout.addView(r3)

        // Row 4 (Bottom Action Row)
        val r4 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        r4.addView(createKey("?123", 1.35f) { layoutState.toggleSymbols() })
        r4.addView(createKey(",", 0.95f) { handleKeyInput(",") })
        val spaceText = when (layoutState.layoutMode) {
            TypingLayout.ENGLISH -> "English"
            TypingLayout.PROBHAT -> "প্রভাত"
            TypingLayout.SOHOJ -> "সহজ"
            TypingLayout.AVRO -> "Avro"
        }
        r4.addView(createKey(spaceText, 4.4f) { handleSpace() })
        r4.addView(createKey(".", 0.95f) { handleKeyInput(".") })
        r4.addView(createKey("⏎", 1.35f) { handleEnter() })
        mainLayout.addView(r4)

        return mainLayout
    }
}
