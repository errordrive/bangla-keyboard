package com.openbangla.keyboard.engine

class NativeAutoCorrectFixer {

    private var isNativeLoaded = false

    private val fallbackMap = mutableMapOf(
        "ami" to "আমি",
        "amar" to "আমার",
        "amader" to "আমাদের",
        "amake" to "আমাকে",
        "apni" to "আপনি",
        "apnar" to "আপনার",
        "tumi" to "তুমি",
        "tomar" to "তোমার",
        "kemon" to "কেমন",
        "achho" to "আছো",
        "achen" to "আছেন",
        "bhalo" to "ভালো",
        "bhalobashi" to "ভালোবাসি",
        "dhaka" to "ঢাকা",
        "bangla" to "বাংলা",
        "bangladesh" to "বাংলাদেশ",
        "shonar" to "সোনার",
        "desh" to "দেশ",
        "khub" to "খুব",
        "onek" to "অনেক",
        "shundor" to "সুন্দর",
        "shokal" to "সকাল",
        "dhonnobad" to "ধন্যবাদ"
    )

    init {
        try {
            System.loadLibrary("autocorrect_jni")
            initFixer()
            isNativeLoaded = true
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
            isNativeLoaded = false
        }
    }

    fun getAutoCorrection(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        if (isNativeLoaded) {
            val res = getCorrectionNative(trimmed.lowercase())
            if (!res.isNullOrBlank()) return res
        }

        return fallbackMap[trimmed.lowercase()]
    }

    fun registerCorrectionRule(key: String, replacement: String) {
        fallbackMap[key.lowercase()] = replacement
        if (isNativeLoaded) {
            addRuleNative(key.lowercase(), replacement)
        }
    }

    // Native declarations
    private external fun initFixer()
    private external fun getCorrectionNative(input: String): String?
    private external fun addRuleNative(key: String, value: String)
    private external fun releaseFixer()
}
