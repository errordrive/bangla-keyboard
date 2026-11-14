package com.openbangla.keyboard.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

enum class KeyboardTheme(val id: String, val displayName: String) {
    LIGHT("light", "Classic Light"),
    DARK("dark", "Modern Dark"),
    AMOLED("amoled", "OLED Pure Black"),
    EMERALD("emerald", "Bangla Emerald Green"),
    SLATE("slate", "Deep Slate")
}

enum class TypingLayout(val id: String, val displayName: String) {
    ENGLISH("english", "English"),
    PROBHAT("probhat", "প্রভাত"),
    SOHOJ("sohoj", "সহজ"),
    AVRO("avro", "অভ্র")
}

class UserPreferences(private val context: Context) {

    companion object {
        val KEY_THEME = stringPreferencesKey("pref_theme")
        val KEY_KEYBOARD_HEIGHT = floatPreferencesKey("pref_keyboard_height")
        val KEY_VIBRATION = booleanPreferencesKey("pref_vibration")
        val KEY_SOUND = booleanPreferencesKey("pref_sound")
        val KEY_AUTOCORRECT = booleanPreferencesKey("pref_autocorrect")
        val KEY_SUGGESTION_STRIP = booleanPreferencesKey("pref_suggestion_strip")
        val KEY_ACTIVE_LAYOUT = stringPreferencesKey("pref_active_layout")
        val KEY_DRAG_TO_TYPE_SHIFT = booleanPreferencesKey("pref_drag_to_type_shift")
    }

    val themeFlow: Flow<KeyboardTheme> = context.dataStore.data.map { preferences ->
        val themeId = preferences[KEY_THEME] ?: KeyboardTheme.LIGHT.id
        KeyboardTheme.entries.find { it.id == themeId } ?: KeyboardTheme.LIGHT
    }

    val keyboardHeightFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[KEY_KEYBOARD_HEIGHT] ?: 1.30f
    }

    val vibrationFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_VIBRATION] ?: true
    }

    val soundFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_SOUND] ?: false
    }

    val autoCorrectFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_AUTOCORRECT] ?: true
    }

    val suggestionStripFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_SUGGESTION_STRIP] ?: true
    }

    val activeLayoutFlow: Flow<TypingLayout> = context.dataStore.data.map { preferences ->
        val layoutId = preferences[KEY_ACTIVE_LAYOUT] ?: TypingLayout.ENGLISH.id
        TypingLayout.entries.find { it.id == layoutId } ?: TypingLayout.ENGLISH
    }

    val dragToTypeShiftFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DRAG_TO_TYPE_SHIFT] ?: true
    }

    suspend fun setTheme(theme: KeyboardTheme) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME] = theme.id
        }
    }

    suspend fun setKeyboardHeight(heightRatio: Float) {
        context.dataStore.edit { preferences ->
            preferences[KEY_KEYBOARD_HEIGHT] = heightRatio
        }
    }

    suspend fun setVibration(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_VIBRATION] = enabled
        }
    }

    suspend fun setSound(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SOUND] = enabled
        }
    }

    suspend fun setAutoCorrect(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTOCORRECT] = enabled
        }
    }

    suspend fun setSuggestionStrip(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SUGGESTION_STRIP] = enabled
        }
    }

    suspend fun setActiveLayout(layout: TypingLayout) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ACTIVE_LAYOUT] = layout.id
        }
    }

    suspend fun setDragToTypeShift(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DRAG_TO_TYPE_SHIFT] = enabled
        }
    }
}
