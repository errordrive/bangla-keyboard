package com.openbangla.keyboard.engine

object FixedBanglaLayout {

    val normalRows = listOf(
        listOf("১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯", "০"),
        listOf("ক", "খ", "গ", "ঘ", "ঙ", "চ", "ছ", "জ", "ঝ", "ঞ"),
        listOf("ট", "ঠ", "ড", "ঢ", "ণ", "ত", "থ", "দ", "ধ", "ন"),
        listOf("প", "ফ", "ব", "ভ", "ম", "য", "র", "ল", "শ", "স")
    )

    val shiftedRows = listOf(
        listOf("!", "@", "#", "$", "%", "^", "&", "*", "(", ")"),
        listOf("া", "ি", "ী", "ু", "ূ", "ৃ", "ে", "ৈ", "ো", "ৌ"),
        listOf("অ", "আ", "ই", "ঈ", "উ", "ঊ", "ঋ", "এ", "ঐ", "ও"),
        listOf("হ", "ড়", "ঢ়", "য়", "ৎ", "ং", "ঃ", "ঁ", "্", "।")
    )

    val bottomSymbols = listOf("্", "।", "ঁ", "ং", "ঃ")
}
