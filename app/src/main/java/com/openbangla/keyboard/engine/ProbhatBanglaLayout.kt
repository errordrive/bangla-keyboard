package com.openbangla.keyboard.engine

/**
 * Probhat (প্রভাত) Bengali layout matching user specification and Ridmik Probhat specification.
 */
object ProbhatBanglaLayout {

    val normalRows = listOf(
        listOf("দ", "ঋ", "ঙ", "র", "ট", "এ", "ঋ", "ি", "ও", "প", "ে", "ৈ"),
        listOf("শ", "স", "ড", "ত", "গ", "হ", "জ", "ক", "ল"),
        listOf("য", "ষ", "চ", "আ", "ব", "ন", "ম")
    )

    val shiftedRows = listOf(
        listOf("ধ", "ঊ", "ঈ", "ড়", "ঠ", "ঐ", "ঊ", "ই", "ঔ", "ফ", "ৈ", "ৌ"),
        listOf("অ", "ঝ", "ট", "থ", "ঘ", "ঃ", "ঝ", "খ", "ং"),
        listOf("য", "ট", "ছ", "খ", "ভ", "ণ", "ঊ")
    )
}
