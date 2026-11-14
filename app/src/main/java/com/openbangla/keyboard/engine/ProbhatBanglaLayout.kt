package com.openbangla.keyboard.engine

/**
 * Probhat (প্রভাত) Bengali layout matching Ridmik Keyboard and user exact specification.
 */
object ProbhatBanglaLayout {

    val normalRows = listOf(
        listOf("দ", "ূ", "ী", "র", "ট", "এ", "ু", "ি", "ও", "প", "ে", "ো"),
        listOf("া", "স", "ড", "ত", "গ", "হ", "জ", "ক", "ল"),
        listOf("য়", "শ", "চ", "আ", "ব", "ন", "ম", "ৃ", "্")
    )

    val shiftedRows = listOf(
        listOf("ধ", "ঊ", "ঈ", "ড়", "ঠ", "ঐ", "উ", "ই", "ঔ", "ফ", "ৈ", "ৌ"),
        listOf("অ", "ষ", "ঢ", "থ", "ঘ", "ঃ", "ঝ", "খ", "ং"),
        listOf("য", "ঢ়", "ছ", "ঋ", "ভ", "ণ", "ঙ", "ৃ", "ঁ")
    )

    // Normal to Shift (back page) mapping for click-and-drag / hint label
    val normalToShiftMap = mapOf(
        "দ" to "ধ",
        "ূ" to "ঊ",
        "ী" to "ঈ",
        "র" to "ড়",
        "ট" to "ঠ",
        "এ" to "ঐ",
        "ু" to "উ",
        "ি" to "ই",
        "ও" to "ঔ",
        "প" to "ফ",
        "ে" to "ৈ",
        "ো" to "ৌ",
        "া" to "অ",
        "স" to "ষ",
        "ড" to "ঢ",
        "ত" to "থ",
        "গ" to "ঘ",
        "হ" to "ঃ",
        "জ" to "ঝ",
        "ক" to "খ",
        "ল" to "ং",
        "য়" to "য",
        "শ" to "ঢ়",
        "চ" to "ছ",
        "আ" to "ঋ",
        "ব" to "ভ",
        "ন" to "ণ",
        "ম" to "ঙ",
        "ৃ" to "ৃ",
        "্" to "ঁ"
    )

    // Alternate mappings requested by user:
    // "ঐ also ঞ", "উ is ৎ", "য is also ্য and র্য"
    val shiftedAlternateMap = mapOf(
        "ঐ" to "ঞ",
        "উ" to "ৎ",
        "য" to "্য",
        "ড়" to "ঢ়",
        "ধ" to "দ"
    )

    val longPressAlternateMap = mapOf(
        "য" to "র্য",
        "ঐ" to "ঞ",
        "উ" to "ৎ",
        "র" to "্র",
        "এ" to "ঞ",
        "ু" to "ৎ",
        "য়" to "্য"
    )

    fun getSecondaryChar(key: String, isShifted: Boolean): String? {
        return if (isShifted) {
            shiftedAlternateMap[key]
        } else {
            normalToShiftMap[key]
        }
    }

    fun getLongPressChar(key: String, isShifted: Boolean): String? {
        return if (isShifted) {
            if (key == "য") "র্য" else shiftedAlternateMap[key]
        } else {
            longPressAlternateMap[key] ?: normalToShiftMap[key]
        }
    }
}
