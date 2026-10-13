package com.baburamtechnologies.expensetracker.core.util

import java.util.Locale

/**
 * Utility functions for exact currency arithmetic and formatting in Indian Rupees (INR).
 * Amounts are stored as integer paise (Long) to avoid floating-point inaccuracies.
 * Example: ₹125.50 = 12550 paise.
 */
object MoneyUtils {

    /**
     * Formats integer paise into a human-readable currency string.
     * Example: 12550L -> "₹125.50", -500L -> "-₹5.00"
     */
    fun formatPaiseToCurrency(paise: Long, includeSymbol: Boolean = true): String {
        val isNegative = paise < 0
        val absPaise = kotlin.math.abs(paise)
        val rupees = absPaise / 100
        val remainingPaise = absPaise % 100
        val symbol = if (includeSymbol) "₹" else ""
        val formatted = String.format(Locale.getDefault(), "%s%d.%02d", symbol, rupees, remainingPaise)
        return if (isNegative) "-$formatted" else formatted
    }

    /**
     * Parses user string input into integer paise (Long).
     * Rejects non-finite inputs (NaN, Infinity), negative inputs,
     * and inputs with more than 2 decimal places.
     * Returns null if input is invalid or <= 0.
     */
    fun parseAmountToPaise(input: String?): Long? {
        if (input.isNullOrBlank()) return null
        val trimmed = input.trim()

        // Explicitly reject non-finite and illegal inputs
        if (trimmed.equals("nan", ignoreCase = true) ||
            trimmed.contains("infinity", ignoreCase = true) ||
            trimmed.contains("f", ignoreCase = true) ||
            trimmed.contains("d", ignoreCase = true)
        ) {
            return null
        }

        // Strict regex matching positive digits with optional dot and max 2 decimal places
        if (!trimmed.matches(Regex("""^\d+(\.\d{1,2})?$"""))) return null

        val parts = trimmed.split(".")
        val rupees = parts[0].toLongOrNull() ?: return null
        val paise = when {
            parts.size == 1 -> 0L
            parts[1].length == 1 -> (parts[1].toLongOrNull() ?: return null) * 10
            parts[1].length == 2 -> (parts[1].toLongOrNull() ?: return null)
            else -> return null
        }

        val totalPaise = try {
            Math.addExact(Math.multiplyExact(rupees, 100L), paise)
        } catch (_: ArithmeticException) {
            return null
        }

        return if (totalPaise > 0) totalPaise else null
    }

    /**
     * Converts integer paise into user-editable input string.
     * Example: 12550L -> "125.50", 12500L -> "125.00"
     */
    fun paiseToInputString(paise: Long): String {
        val absPaise = kotlin.math.abs(paise)
        val rupees = absPaise / 100
        val remainingPaise = absPaise % 100
        return String.format(Locale.US, "%d.%02d", rupees, remainingPaise)
    }
}

fun Long.toFormattedCurrency(includeSymbol: Boolean = true): String =
    MoneyUtils.formatPaiseToCurrency(this, includeSymbol)
