package com.example.util

import java.text.DecimalFormat

object CurrencyUtils {
    private val formatter = DecimalFormat("#,###")

    fun toPersianDigits(text: String): String {
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val sb = StringBuilder()
        for (ch in text) {
            if (ch in '0'..'9') {
                sb.append(persianDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Formats amount in Rials (ریال), e.g. "۱,۵۰۰,۰۰۰ ریال"
     */
    fun formatRials(amount: Long, includeUnit: Boolean = true): String {
        val formatted = formatter.format(amount)
        val persian = toPersianDigits(formatted)
        return if (includeUnit) "$persian ریال" else persian
    }

    /**
     * Formats equivalent in Tomans for convenience: e.g. "۱۵۰,۰۰۰ تومان"
     */
    fun formatTomans(rials: Long): String {
        val tomans = rials / 10
        val formatted = formatter.format(tomans)
        return "${toPersianDigits(formatted)} تومان"
    }

    fun parseRials(input: String): Long {
        val cleaned = input.replace("[^0-9]".toRegex(), "")
        return cleaned.toLongOrNull() ?: 0L
    }
}
