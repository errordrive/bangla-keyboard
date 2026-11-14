package com.openbangla.keyboard.engine

object AvroPhoneticParser {

    private val vowels = setOf('a', 'e', 'i', 'o', 'u', 'A', 'E', 'I', 'O', 'U')

    // Independent Vowels
    private val independentVowels = mapOf(
        "a" to "আ",
        "aa" to "আ",
        "i" to "ই",
        "I" to "ঈ",
        "ee" to "ঈ",
        "u" to "উ",
        "U" to "ঊ",
        "oo" to "ঊ",
        "e" to "এ",
        "oi" to "ঐ",
        "o" to "অ",
        "O" to "ও",
        "ou" to "ঔ",
        "ow" to "ঔ",
        "r" to "ঋ",
        "ri" to "ঋ"
    )

    // Dependent Vowels (Kar forms)
    private val vowelKars = mapOf(
        "a" to "া",
        "aa" to "া",
        "i" to "ি",
        "I" to "ী",
        "ee" to "ী",
        "u" to "ু",
        "U" to "ূ",
        "oo" to "ূ",
        "e" to "ে",
        "oi" to "ৈ",
        "O" to "ো",
        "ou" to "ৌ",
        "ow" to "ৌ",
        "r" to "ৃ",
        "ri" to "ৃ"
    )

    // Consonants
    private val consonants = mapOf(
        "k" to "ক",
        "kh" to "খ",
        "g" to "গ",
        "gh" to "ঘ",
        "Ng" to "ঙ",
        "ng" to "ং",
        "c" to "চ",
        "ch" to "চ",
        "Ch" to "ছ",
        "chh" to "ছ",
        "j" to "জ",
        "jh" to "ঝ",
        "T" to "ট",
        "Th" to "ঠ",
        "D" to "ড",
        "Dh" to "ঢ",
        "R" to "ড়",
        "Rh" to "ঢ়",
        "t" to "ত",
        "th" to "থ",
        "d" to "দ",
        "dh" to "ধ",
        "n" to "ন",
        "N" to "ণ",
        "p" to "প",
        "ph" to "ফ",
        "f" to "ফ",
        "b" to "ব",
        "bh" to "ভ",
        "v" to "ভ",
        "m" to "ম",
        "z" to "য",
        "y" to "য়",
        "r" to "র",
        "l" to "ল",
        "sh" to "শ",
        "S" to "শ",
        "Sh" to "ষ",
        "s" to "স",
        "h" to "হ"
    )

    // Common Bengali Conjuncts (Juktakkhor)
    private val conjuncts = mapOf(
        "kk" to "ক্ক",
        "kt" to "ক্ত",
        "kkh" to "ক্ষ",
        "kS" to "ক্ষ",
        "ks" to "ক্স",
        "gdh" to "গ্ধ",
        "gn" to "গ্ন",
        "ngk" to "ঙ্ক",
        "ngkh" to "ঙ্খ",
        "ngg" to "ঙ্গ",
        "nggh" to "ঙ্ঘ",
        "chch" to "চ্চ",
        "chchh" to "চ্ছ",
        "Chch" to "চ্ছ",
        "jj" to "জ্জ",
        "jjh" to "জ্ঝ",
        "jJ" to "জ্ঞ",
        "TT" to "ট্ট",
        "DD" to "ড্ড",
        "NT" to "ণ্ট",
        "NTh" to "ণ্ঠ",
        "ND" to "ণ্ড",
        "NN" to "ণ্ণ",
        "tt" to "ত্ত",
        "tth" to "ত্থ",
        "tr" to "ত্র",
        "dd" to "দ্দ",
        "ddh" to "দ্ধ",
        "dhn" to "ধ্ন",
        "nt" to "ন্ত",
        "nth" to "ন্থ",
        "nd" to "ন্দ",
        "ndh" to "ন্ধ",
        "nn" to "ন্ন",
        "pt" to "প্ত",
        "pn" to "প্ন",
        "pl" to "প্ল",
        "bd" to "ব্দ",
        "bdh" to "ব্ধ",
        "bb" to "ব্ব",
        "bl" to "ব্ল",
        "mn" to "ম্ন",
        "mp" to "ম্প",
        "mph" to "ম্ফ",
        "mb" to "ম্ব",
        "mbh" to "ম্ভ",
        "mm" to "ম্ম",
        "ml" to "ম্ল",
        "lk" to "ল্ক",
        "lg" to "ল্গ",
        "lt" to "ল্ত",
        "ld" to "ল্দ",
        "lp" to "ল্প",
        "lb" to "ল্ব",
        "lm" to "ল্ম",
        "ll" to "ল্ল",
        "shc" to "শ্চ",
        "shch" to "শ্চ",
        "shn" to "শ্ন",
        "shl" to "শ্ল",
        "Shk" to "ষ্ক",
        "Sht" to "ষ্ট",
        "ShTh" to "ষ্ঠ",
        "ShN" to "ষ্ণ",
        "Shp" to "ষ্প",
        "Shph" to "ষ্ফ",
        "Shm" to "ষ্ম",
        "sk" to "স্ক",
        "skh" to "স্খ",
        "st" to "স্ত",
        "sth" to "স্থ",
        "sn" to "স্ন",
        "sp" to "স্প",
        "sph" to "স্ফ",
        "sb" to "স্ব",
        "sm" to "স্ম",
        "sl" to "স্ল",
        "hn" to "হ্ন",
        "hN" to "হ্ণ",
        "hm" to "হ্ম",
        "hl" to "হ্ল"
    )

    fun parse(input: String): String {
        if (input.isEmpty()) return ""

        val result = StringBuilder()
        var i = 0
        val len = input.length
        var prevWasConsonant = false

        while (i < len) {
            val ch = input[i]

            // Numbers
            if (ch in '0'..'9') {
                result.append(bengaliDigit(ch))
                prevWasConsonant = false
                i++
                continue
            }

            // Punctuation
            if (ch == '.') {
                result.append("।")
                prevWasConsonant = false
                i++
                continue
            }
            if (ch == '`') { // hasanta escape
                result.append("্")
                prevWasConsonant = false
                i++
                continue
            }
            if (ch == '^') {
                result.append("ঁ")
                prevWasConsonant = false
                i++
                continue
            }
            if (ch == ':') {
                result.append("ঃ")
                prevWasConsonant = false
                i++
                continue
            }

            // Check 4-character conjunct
            if (i + 4 <= len) {
                val sub4 = input.substring(i, i + 4)
                if (conjuncts.containsKey(sub4)) {
                    result.append(conjuncts[sub4])
                    prevWasConsonant = true
                    i += 4
                    continue
                }
            }

            // Check 3-character conjunct
            if (i + 3 <= len) {
                val sub3 = input.substring(i, i + 3)
                if (conjuncts.containsKey(sub3)) {
                    result.append(conjuncts[sub3])
                    prevWasConsonant = true
                    i += 3
                    continue
                }
            }

            // Check 2-character conjunct or consonant/vowel
            if (i + 2 <= len) {
                val sub2 = input.substring(i, i + 2)

                // Juktakkhor match
                if (conjuncts.containsKey(sub2)) {
                    result.append(conjuncts[sub2])
                    prevWasConsonant = true
                    i += 2
                    continue
                }

                // Vowel combination (aa, ee, oo, oi, ou, ow, ri)
                if (sub2 in vowelKars && prevWasConsonant) {
                    result.append(vowelKars[sub2])
                    prevWasConsonant = false
                    i += 2
                    continue
                } else if (sub2 in independentVowels && !prevWasConsonant) {
                    result.append(independentVowels[sub2])
                    prevWasConsonant = false
                    i += 2
                    continue
                }

                // Consonant pair (kh, gh, ch, Ch, jh, Th, Dh, Rh, th, dh, ph, bh, sh, Sh, ng)
                if (sub2 in consonants) {
                    result.append(consonants[sub2])
                    prevWasConsonant = true
                    i += 2
                    continue
                }
            }

            // Single Character Match
            val sub1 = input.substring(i, i + 1)

            // Vowels
            if (sub1 in vowelKars && prevWasConsonant) {
                // If 'o', default inherent vowel doesn't add kar
                if (sub1 == "o") {
                    // Inherent vowel
                } else {
                    result.append(vowelKars[sub1])
                }
                prevWasConsonant = false
                i++
                continue
            } else if (sub1 in independentVowels && !prevWasConsonant) {
                result.append(independentVowels[sub1])
                prevWasConsonant = false
                i++
                continue
            }

            // Consonants
            if (sub1 in consonants) {
                result.append(consonants[sub1])
                prevWasConsonant = true
                i++
                continue
            }

            // Passthrough for spaces, symbols, unknown characters
            result.append(ch)
            prevWasConsonant = false
            i++
        }

        return result.toString()
    }

    private fun bengaliDigit(ch: Char): Char {
        return when (ch) {
            '0' -> '০'
            '1' -> '১'
            '2' -> '২'
            '3' -> '৩'
            '4' -> '৪'
            '5' -> '৫'
            '6' -> '৬'
            '7' -> '৭'
            '8' -> '৮'
            '9' -> '৯'
            else -> ch
        }
    }
}
