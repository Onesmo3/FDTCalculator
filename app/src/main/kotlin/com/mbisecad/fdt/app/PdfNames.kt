package com.mbisecad.fdt.app

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Safe, readable PDF name: FDT_<MODE>_<LOCATION>_<LAYER>_<suffix>_<date>.pdf */
internal fun fdtPdfName(mode: String, location: String, layer: String, suffix: String): String {
    fun part(value: String, fallback: String): String {
        val cleaned = value.trim()
            .replace(Regex("[^A-Za-z0-9]+"), "_")
            .trim('_')
            .uppercase(Locale.US)
        return (if (cleaned.isEmpty()) fallback else cleaned).take(18)
    }
    val date = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
    return "FDT_${part(mode, "FDT")}_${part(location, "NoLocation")}_" +
        "${part(layer, "NoLayer")}_${part(suffix, "PDF")}_$date.pdf"
}
