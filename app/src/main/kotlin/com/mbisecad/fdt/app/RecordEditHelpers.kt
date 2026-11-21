package com.mbisecad.fdt.app

import android.widget.EditText
import com.mbisecad.fdt.FdtHoleData
import com.mbisecad.fdt.FdtRecord
import com.mbisecad.fdt.HoleInput
import com.mbisecad.fdt.Tolerance
import com.mbisecad.fdt.calcHole
import com.mbisecad.fdt.defaultLayers
import com.mbisecad.fdt.summarize

internal data class HoleEdit(
    val A: EditText, val B: EditText, val C: EditText, val D: EditText,
    val J: EditText, val K: EditText, val L: EditText,
    val chainage: EditText, val offset: EditText, val depth: EditText,
    val cylinder: EditText, val cylinderWeight: EditText, val containerNo: EditText,
)

private fun num(field: EditText, label: String, hole: Int): Double? {
    val value = field.text.toString().trim().toDoubleOrNull()
    if (value == null) field.fdtError("Hole ${hole + 1}: $label") else field.fdtError(null)
    return value
}

private fun optionalNum(field: EditText): Double? {
    val text = field.text.toString().trim()
    if (text.isEmpty()) {
        field.fdtError(null)
        return null
    }
    val value = text.toDoubleOrNull()
    if (value == null) field.fdtError("Andika namba sahihi.") else field.fdtError(null)
    return value
}

internal fun recomputeWorksheet(
    base: FdtRecord,
    edits: List<HoleEdit>,
    sandDensity: Double,
    mdd: Double,
    omc: Double,
    layerText: String,
): Pair<FdtRecord?, String?> {
    val comps = mutableListOf<Double>()
    val holes = mutableListOf<FdtHoleData>()

    edits.forEachIndexed { i, e ->
        val a = num(e.A, "A soil", i) ?: return null to "Hole ${i + 1}: A si sahihi.".loc()
        val b = num(e.B, "B initial sand", i) ?: return null to "Hole ${i + 1}: B si sahihi.".loc()
        val c = num(e.C, "C final sand", i) ?: return null to "Hole ${i + 1}: C si sahihi.".loc()
        val d = num(e.D, "D cone", i) ?: return null to "Hole ${i + 1}: D si sahihi.".loc()
        val j = num(e.J, "J container+wet", i) ?: return null to "Hole ${i + 1}: J si sahihi.".loc()
        val k = num(e.K, "K container+dry", i) ?: return null to "Hole ${i + 1}: K si sahihi.".loc()
        val l = num(e.L, "L container", i) ?: return null to "Hole ${i + 1}: L si sahihi.".loc()

        val result = calcHole(
            HoleInput(A = a, B = b, C = c, J = j, K = k, L = l),
            D = d,
            sandDensity = sandDensity,
            mdd = mdd,
            omc = omc,
        )
        comps += result.S
        val tolerance = when (result.U) {
            Tolerance.OK -> "OK"
            Tolerance.NOT_OK -> "NOT OK"
            Tolerance.NONE -> "-"
        }
        holes += FdtHoleData(
            A = a, B = b, C = c, D = d, J = j, K = k, L = l,
            sandInHole = result.E,
            sandDensityOfHole = sandDensity,
            volume = result.G,
            wetDensity = result.H,
            dryDensity = result.P,
            moisture = result.O,
            compaction = result.S,
            tolerance = tolerance,
            chainage = e.chainage.text.toString().trim(),
            offset = e.offset.text.toString().trim(),
            depth = e.depth.text.toString().trim(),
            cylinder = e.cylinder.text.toString().trim(),
            cylinderWeight = optionalNum(e.cylinderWeight) ?: 0.0,
            containerNo = e.containerNo.text.toString().trim(),
        )
    }

    if (comps.isEmpty()) return null to "Hakuna hole iliyokamilika.".loc()

    val layerName = layerText.substringBefore("(").trim()
    val layers = defaultLayers()
    val layer = layers.firstOrNull { it.name.equals(layerName, true) } ?: layers.first()
    val summary = summarize(comps, edits.size, layer)

    return base.copy(
        holes = holes,
        status = summary.status,
        xn = summary.Xn,
        xo = summary.Xo,
        xs = summary.Xs,
        xmin = summary.Xmin,
    ) to null
}
