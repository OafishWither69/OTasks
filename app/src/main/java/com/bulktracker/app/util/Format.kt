package com.bulktracker.app.util

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

object Format {
    private val inLocale = Locale.forLanguageTag("en-IN")
    private val integerFormatter = NumberFormat.getIntegerInstance(inLocale)

    fun formatKcal(kcal: Int?): String {
        if (kcal == null) return "—"
        return integerFormatter.format(kcal)
    }

    fun formatProtein(protein: Double?): String {
        if (protein == null) return "—"
        return if (protein % 1.0 == 0.0) {
            integerFormatter.format(protein.toLong())
        } else {
            val df = DecimalFormat("#.#")
            df.format(protein)
        }
    }
}
