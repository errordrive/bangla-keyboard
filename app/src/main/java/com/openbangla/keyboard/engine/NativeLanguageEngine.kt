package com.openbangla.keyboard.engine

import android.content.Context
import java.io.InputStream

class NativeLanguageEngine {

    private var isNativeLoaded = false

    init {
        try {
            System.loadLibrary("openbangla_jni")
            initNativeEngine()
            isNativeLoaded = true
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
            isNativeLoaded = false
        }
    }

    fun loadDictionaryFromResource(context: Context, resId: Int) {
        try {
            val inputStream: InputStream = context.resources.openRawResource(resId)
            val bytes = inputStream.readBytes()
            inputStream.close()
            if (isNativeLoaded) {
                loadDictionary(bytes)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSuggestions(
        input: String,
        touchXs: FloatArray? = null,
        touchYs: FloatArray? = null,
        maxCount: Int = 5
    ): List<String> {
        if (input.isBlank()) return emptyList()

        if (isNativeLoaded) {
            val nativeResults = getSuggestionsNative(input, touchXs, touchYs, maxCount)
            return nativeResults.toList()
        }

        // JVM Fallback
        return listOf(input)
    }

    fun insertCustomWord(word: String, frequency: Int) {
        if (isNativeLoaded) {
            addWord(word, frequency)
        }
    }

    fun updateContext(previousWords: List<String>) {
        if (isNativeLoaded) {
            setPreviousWords(previousWords.toTypedArray())
        }
    }

    // Native declarations
    private external fun initNativeEngine()
    private external fun loadDictionary(data: ByteArray)
    private external fun addWord(word: String, freq: Int)
    private external fun setPreviousWords(words: Array<String>)
    private external fun getSuggestionsNative(
        input: String,
        touchXs: FloatArray?,
        touchYs: FloatArray?,
        maxCount: Int
    ): Array<String>
    private external fun releaseNativeEngine()
}
