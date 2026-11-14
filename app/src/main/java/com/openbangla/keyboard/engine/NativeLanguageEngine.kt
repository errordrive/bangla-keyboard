package com.openbangla.keyboard.engine

import android.content.Context
import java.io.InputStream

class NativeLanguageEngine {

    private var isNativeLoaded = false

    init {
        try {
            System.loadLibrary("c++_shared")
        } catch (e: Throwable) {}

        try {
            System.loadLibrary("openbangla_jni")
            initNativeEngine()
            isNativeLoaded = true
        } catch (e: Throwable) {
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
        } catch (e: Throwable) {
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
            try {
                val nativeResults = getSuggestionsNative(input, touchXs, touchYs, maxCount)
                return nativeResults.toList()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        // Safe Fallback
        return listOf(input)
    }

    fun insertCustomWord(word: String, frequency: Int) {
        if (isNativeLoaded) {
            try {
                addWord(word, frequency)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun updateContext(previousWords: List<String>) {
        if (isNativeLoaded) {
            try {
                setPreviousWords(previousWords.toTypedArray())
            } catch (e: Throwable) {
                e.printStackTrace()
            }
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
