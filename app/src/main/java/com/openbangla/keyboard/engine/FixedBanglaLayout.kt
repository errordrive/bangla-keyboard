package com.openbangla.keyboard.engine

/**
 * Sohoj / National Bengali layout matching Ridmik Keyboard specification.
 */
object FixedBanglaLayout {

    // Row 1 (10 keys), Row 2 (9 keys), Row 3 (7 keys flanked by Shift & Backspace)
    val normalRows = listOf(
        listOf("ঙ", "য", "ড", "প", "ট", "চ", "জ", "হ", "গ", "ড়"),
        listOf("ৃ", "ভ", "থ", "দ", "শ", "স", "ব", "ন", "ম"),
        listOf("ো", "ে", "া", "ি", "ু", "ল", "ত")
    )

    val shiftedRows = listOf(
        listOf("ং", "্য", "ঢ", "ফ", "ঠ", "ছ", "ঝ", "ঞ", "ঘ", "ঢ়"),
        listOf("ঃ", "ভ", "ঠ", "ধ", "ষ", "স", "ব", "ণ", "ম"),
        listOf("ৌ", "ৈ", "া", "ী", "ূ", "ল", "থ")
    )

    val numberRows = listOf(
        listOf("১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯", "০"),
        listOf("ৎ", "ং", "ঃ", "ঁ", "্", "।", ",", "?", "!"),
        listOf("@", "#", "$", "%", "&", "-", "+")
    )
}
