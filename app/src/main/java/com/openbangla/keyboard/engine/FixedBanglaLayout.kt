package com.openbangla.keyboard.engine

object FixedBanglaLayout {

    // 3 Rows - matching mobile keyboard design:
    // Row 1 (10 keys), Row 2 (9 keys), Row 3 (7 keys flanked by Shift & Backspace)
    val normalRows = listOf(
        listOf("ক", "খ", "গ", "ঘ", "ঙ", "চ", "ছ", "জ", "ঝ", "ঞ"),
        listOf("ট", "ঠ", "ড", "ঢ", "ণ", "ত", "থ", "দ", "ধ"),
        listOf("ন", "প", "ফ", "ব", "ভ", "ম", "য")
    )

    val shiftedRows = listOf(
        listOf("া", "ি", "ী", "ু", "ূ", "ৃ", "ে", "ৈ", "ো", "ৌ"),
        listOf("অ", "আ", "ই", "ঈ", "উ", "ঊ", "ঋ", "এ", "ঐ"),
        listOf("র", "ল", "শ", "ষ", "স", "হ", "ড়")
    )

    val numberRows = listOf(
        listOf("১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯", "০"),
        listOf("ৎ", "ং", "ঃ", "ঁ", "্", "।", ",", "?", "!"),
        listOf("@", "#", "$", "%", "&", "-", "+")
    )
}
