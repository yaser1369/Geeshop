package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface

object BarcodeUtils {

    // Code 128 Symbol Patterns (width of 6 elements: bars and spaces)
    // Format: 11-module length patterns for values 0..106
    private val PATTERNS = arrayOf(
        "212222", "222122", "222221", "121223", "121322", "131222", "122213", "122312", "132212", "221213",
        "221312", "231212", "112232", "122132", "122231", "113222", "123122", "123221", "223211", "221132",
        "221231", "213212", "223112", "312131", "311222", "321122", "321221", "312212", "322112", "322211",
        "212123", "212321", "232121", "111323", "131123", "131321", "112313", "132113", "132311", "211313",
        "231113", "231311", "112133", "112331", "132131", "113123", "113321", "133121", "313121", "211331",
        "231131", "213113", "213311", "213131", "311123", "311321", "331121", "312113", "312311", "332111",
        "314111", "221411", "431111", "111224", "111422", "121124", "121421", "141122", "141221", "112214",
        "112412", "122114", "122411", "142112", "142211", "241211", "221114", "413111", "241112", "134111",
        "111242", "121142", "121241", "114212", "124112", "124211", "411212", "421112", "421211", "212141",
        "214121", "412121", "111143", "111341", "131141", "114113", "114311", "411113", "411311", "113141",
        "114131", "311141", "411131", "211412", "211214", "211232", "2331112" // 106 is STOP pattern (13 modules)
    )

    private const val START_CODE_B = 104
    private const val STOP_CODE = 106

    /**
     * Generates a 12-digit standard store barcode based on product code and timestamp
     */
    fun generateProductBarcode(productCode: String): String {
        val sanitized = productCode.filter { it.isDigit() }.padStart(4, '0').takeLast(4)
        val timePart = (System.currentTimeMillis() % 100000000).toString().padStart(8, '0')
        return "GH$sanitized$timePart" // e.g. "GH010183746192"
    }

    /**
     * Encodes a string into Code 128 module pattern (sequence of 1s for bar, 0s for space)
     */
    fun encodeToModules(content: String): String {
        val patternList = mutableListOf<String>()
        val values = mutableListOf<Int>()

        // Start B
        values.add(START_CODE_B)
        patternList.add(PATTERNS[START_CODE_B])

        // Chars
        for (ch in content) {
            val ascii = ch.code
            val value = ascii - 32
            if (value in 0..95) {
                values.add(value)
                patternList.add(PATTERNS[value])
            }
        }

        // Checksum
        var checksum = values[0]
        for (i in 1 until values.size) {
            checksum += i * values[i]
        }
        val checkValue = checksum % 103
        patternList.add(PATTERNS[checkValue])

        // Stop
        patternList.add(PATTERNS[STOP_CODE])

        // Expand patterns into binary string (1 = black bar, 0 = white space)
        val sb = StringBuilder()
        for (pattern in patternList) {
            var isBar = true
            for (digitChar in pattern) {
                val width = digitChar - '0'
                for (w in 0 until width) {
                    sb.append(if (isBar) '1' else '0')
                }
                isBar = !isBar
            }
        }
        return sb.toString()
    }

    /**
     * Renders barcode into high-resolution Bitmap with clear human-readable text below
     * Suitable for standard 80mm thermal printers or screen inspection.
     */
    fun createBarcodeBitmap(
        content: String,
        labelTitle: String = "ققنوس شاپ",
        width: Int = 540,
        height: Int = 200
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 22f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        val subTextPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 18f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.MONOSPACE
        }

        val barPaint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = false
            style = Paint.Style.FILL
        }

        // Top title
        canvas.drawText(labelTitle, width / 2f, 32f, textPaint)

        // Draw barcode modules
        val modules = try {
            encodeToModules(content)
        } catch (_: Exception) {
            "1".repeat(100)
        }

        val quietZone = 36f
        val availableWidth = width - (quietZone * 2)
        val moduleWidth = availableWidth / modules.length.toFloat()
        val barTop = 46f
        val barBottom = height - 42f

        for (i in modules.indices) {
            if (modules[i] == '1') {
                val left = quietZone + (i * moduleWidth)
                val right = left + moduleWidth
                canvas.drawRect(left, barTop, right, barBottom, barPaint)
            }
        }

        // Human-readable code below barcode
        canvas.drawText(content, width / 2f, height - 14f, subTextPaint)

        return bitmap
    }
}
