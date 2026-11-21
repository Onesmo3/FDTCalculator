package com.mbisecad.fdt

import java.util.Locale
import kotlin.math.abs

/**
 * Pure calculation core for the Field Density Test (Sand Cone Method).
 *
 * Ported 1:1 from the platform-independent C++ core (src/fdtcore.h/.cpp)
 * of the AndroidIDE/Qt version of FieldDensityTestCalculator.
 * No Android, Qt or UI dependencies - usable from Kotlin/JVM, Android or KMP.
 *
 * Reference standards: AASHTO T 191 / ASTM D1556.
 * Always verify against the applicable project specification.
 */

/** n values at which the acceptance table changes: 3, 5, 6, 7, 8, 9+ */
val LAYER_BREAKPOINTS: List<Int> = listOf(3, 5, 6, 7, 8, 9)

data class Cylinder(val n: Int, val weight: Double, val sandInCone: Double)

data class Layer(val name: String, val xs: List<Double>, val xmin: List<Double>) {
    init {
        require(xs.size == LAYER_BREAKPOINTS.size) { "xs must contain 6 values" }
        require(xmin.size == LAYER_BREAKPOINTS.size) { "xmin must contain 6 values" }
    }
}

enum class Tolerance { NONE, OK, NOT_OK }

fun defaultCylinders(): List<Cylinder> = listOf(
    Cylinder(1, 1604.0, 640.0),
    Cylinder(2, 1617.0, 656.0),
    Cylinder(3, 2103.0, 726.0),
    Cylinder(4, 2085.0, 724.0),
)

/** Table 7205/1: Xs and Xmin for n = 3, 5, 6, 7, 8, 9+ */
fun defaultLayers(): List<Layer> = listOf(
    Layer("Roadbed", listOf(100.1, 100.3, 100.5, 100.6, 100.7, 100.8), listOf(97.1, 96.9, 96.8, 96.7, 96.6, 96.5)),
    Layer("G3", listOf(90.1, 90.4, 90.6, 90.7, 90.9, 91.0), listOf(86.4, 86.2, 86.0, 85.9, 85.8, 85.7)),
    Layer("G7", listOf(93.2, 93.6, 93.8, 94.0, 94.2, 94.3), listOf(87.9, 87.6, 87.4, 87.2, 87.0, 86.9)),
    Layer("Backfill", listOf(93.2, 93.6, 93.8, 94.0, 94.2, 94.3), listOf(87.9, 87.6, 87.4, 87.2, 87.0, 86.9)),
    Layer("G15", listOf(95.2, 95.6, 95.8, 96.0, 96.2, 96.3), listOf(91.4, 91.2, 91.0, 90.9, 90.8, 90.7)),
    Layer("GW", listOf(95.2, 95.6, 95.8, 96.0, 96.2, 96.3), listOf(91.4, 91.2, 91.0, 90.9, 90.8, 90.7)),
    Layer("G25", listOf(95.2, 95.6, 95.8, 96.0, 96.2, 96.3), listOf(91.4, 91.2, 91.0, 90.9, 90.8, 90.7)),
    Layer("G45", listOf(95.2, 95.6, 95.8, 96.0, 96.2, 96.3), listOf(91.4, 91.2, 91.0, 90.9, 90.8, 90.7)),
    Layer("CM", listOf(95.2, 95.6, 95.8, 96.0, 96.2, 96.3), listOf(91.4, 91.2, 91.0, 90.9, 90.8, 90.7)),
    Layer("C1-Subbase", listOf(95.2, 95.6, 95.8, 96.0, 96.2, 96.3), listOf(91.4, 91.2, 91.0, 90.9, 90.8, 90.7)),
    Layer("C1-Base", listOf(97.1, 97.4, 97.6, 97.7, 97.9, 98.0), listOf(93.4, 93.2, 93.0, 92.9, 92.8, 92.7)),
    Layer("C2", listOf(97.1, 97.4, 97.6, 97.7, 97.9, 98.0), listOf(93.4, 93.2, 93.0, 92.9, 92.8, 92.7)),
    Layer("G60", listOf(98.1, 98.4, 98.6, 98.7, 98.9, 99.0), listOf(94.4, 94.2, 94.0, 93.9, 93.8, 93.7)),
    Layer("G80", listOf(98.1, 98.4, 98.6, 98.7, 98.9, 99.0), listOf(94.4, 94.2, 94.0, 93.9, 93.8, 93.7)),
    Layer("CRS", listOf(100.1, 100.3, 100.5, 100.6, 100.7, 100.8), listOf(97.1, 96.9, 96.8, 96.7, 96.6, 96.5)),
    Layer("CRR-88 (Aggregate Apparent Density)", listOf(88.4, 88.6, 88.7, 88.8, 88.9, 89.0), listOf(85.6, 85.5, 85.3, 85.2, 85.1, 85.0)),
    Layer("CRR-104 (BS-Heavy alt)", listOf(104.1, 104.4, 104.6, 104.7, 104.9, 105.0), listOf(100.4, 100.2, 100.0, 99.9, 99.8, 99.7)),
    Layer("AC20", listOf(96.1, 96.3, 96.5, 96.6, 96.7, 96.8), listOf(93.0, 92.9, 92.8, 92.7, 92.6, 92.5)),
    Layer("AC14", listOf(96.1, 96.3, 96.5, 96.6, 96.7, 96.8), listOf(93.0, 92.9, 92.8, 92.7, 92.6, 92.5)),
    Layer("AC10", listOf(96.1, 96.3, 96.5, 96.6, 96.7, 96.8), listOf(93.0, 92.9, 92.8, 92.7, 92.6, 92.5)),
    Layer("BEMIX", listOf(96.1, 96.3, 96.5, 96.6, 96.7, 96.8), listOf(93.0, 92.9, 92.8, 92.7, 92.6, 92.5)),
    Layer("FBMIX", listOf(96.1, 96.3, 96.5, 96.6, 96.7, 96.8), listOf(93.0, 92.9, 92.8, 92.7, 92.6, 92.5)),
)

/** Same lookup() as the JS/C++ versions: last breakpoint <= x. 0 means "no value". */
fun lookup(x: Int, ys: List<Double>): Double? {
    require(ys.size == LAYER_BREAKPOINTS.size) { "ys must contain 6 values" }
    var result: Double? = null
    for (i in LAYER_BREAKPOINTS.indices) {
        if (x >= LAYER_BREAKPOINTS[i]) result = ys[i]
    }
    return result?.takeIf { it != 0.0 }
}

fun findLayer(layers: List<Layer>, name: String): Layer? {
    val key = normalize(name)
    if (key.isEmpty()) return null
    return layers.firstOrNull { normalize(it.name) == key }
}

fun findCylinder(cylinders: List<Cylinder>, n: Int): Cylinder? = cylinders.firstOrNull { it.n == n }

private fun normalize(value: String): String = value.trim().lowercase(Locale.ROOT)

private fun format3(value: Double): String = String.format(Locale.US, "%.3f", value)

data class HoleInput(
    val A: Double = 0.0, // Weight of soil from hole (g)
    val B: Double = 0.0, // Initial sand in cylinder (g)
    val C: Double = 0.0, // Final sand in cylinder (g)
    val J: Double = 0.0, // Container + wet soil (g)
    val K: Double = 0.0, // Container + dry soil (g)
    val L: Double = 0.0, // Weight of container (g)
)

data class HoleResult(
    val D: Double, // Sand in cone (g)
    val E: Double, // Sand in hole (g)
    val F: Double, // Density of sand
    val G: Double, // Volume of hole
    val H: Double, // Wet density
    val M: Double, // Weight of water
    val N: Double, // Weight of dry soil
    val O: Double, // Moisture content (%)
    val P: Double, // Dry density
    val Q: Double, // MDD
    val R: Double, // OMC
    val S: Double, // Compaction (%)
    val U: Tolerance, // Moisture tolerance (+-2%)
)

/** D = sand in cone, F = density of sand, Q = MDD, R = OMC. */
fun calcHole(h: HoleInput, D: Double, sandDensity: Double, mdd: Double, omc: Double): HoleResult {
    val E = h.B - h.C - D
    val F = sandDensity
    val G = if (F > 0) E / F else 0.0
    val H = if (G > 0) h.A / G else 0.0
    val M = h.J - h.K
    val N = h.K - h.L
    val O = if (N > 0) M * 100 / N else 0.0
    val U = if (omc > 0 && N > 0) {
        if (abs(O - omc) <= 2) Tolerance.OK else Tolerance.NOT_OK
    } else Tolerance.NONE
    val P = if (H > 0) H * 100 / (100 + O) else 0.0
    val Q = mdd
    val R = omc
    val S = if (mdd > 0 && P > 0) P * 100 / mdd else 0.0
    return HoleResult(D, E, F, G, H, M, N, O, P, Q, R, S, U)
}

data class WorksheetSummary(
    val n: Int,
    val Xn: Double?, // average compaction
    val Xo: Double?, // minimum single compaction
    val Xs: Double?, // required average
    val Xmin: Double?, // required minimum
    val status: String, // Awaiting data | No Requirement Yet | Passed | Failed
)

/** compValues = compaction (S) of completed holes only; n = all holes. */
fun summarize(compValues: List<Double>, n: Int, layer: Layer?): WorksheetSummary {
    val Xn = if (compValues.isEmpty()) null else compValues.sum() / compValues.size
    val Xo = compValues.minOrNull()
    val Xs = layer?.let { lookup(n, it.xs) }
    val Xmin = layer?.let { lookup(n, it.xmin) }

    val status = when {
        compValues.isEmpty() -> "Awaiting data"
        Xs == null -> "No Requirement Yet"
        else -> {
            val passAvg = Xn!! >= Xs
            val passMin = Xmin == null || Xo!! >= Xmin
            if (passAvg && passMin) "Passed" else "Failed"
        }
    }
    return WorksheetSummary(n, Xn, Xo, Xs, Xmin, status)
}

data class SingleResult(
    val ok: Boolean = false,
    val error: String? = null,
    val sandHole: Double = 0.0,
    val volume: Double = 0.0,
    val wetDensity: Double = 0.0,
    val dryDensity: Double = 0.0,
    val compaction: Double = 0.0,
    val required: Double? = null,
    val status: String = "",
)

private fun singleError(message: String) = SingleResult(ok = false, error = message)

fun calcSingle(
    initialSand: Double,
    finalSand: Double,
    sandCone: Double,
    sandDensity: Double,
    soil: Double,
    omc: Double,
    mdd: Double,
    required: Double?,
): SingleResult {
    if (sandDensity <= 0) return singleError("Density of Sand must be greater than zero.")
    if (mdd <= 0) return singleError("MDD must be greater than zero.")

    val sandHole = initialSand - finalSand - sandCone
    if (sandHole <= 0) {
        return singleError(
            "The \"Sand in Hole\" calculation (Initial - Final - Sand in Cone) " +
                "returned a negative or zero value. Please recheck the Initial Sand, " +
                "Final Sand, and Cylinder Number values you entered."
        )
    }

    val volume = sandHole / sandDensity
    val wetDensity = soil / volume
    val dryDensity = wetDensity * 100 / (100 + omc)
    val compaction = dryDensity / mdd * 100
    val status = if (required == null) "No Requirement Yet" else if (compaction >= required) "Passed" else "Failed"

    return SingleResult(
        ok = true,
        sandHole = sandHole,
        volume = volume,
        wetDensity = wetDensity,
        dryDensity = dryDensity,
        compaction = compaction,
        required = required,
        status = status,
    )
}

data class CheckRow(val hole: Int, val label: String, val actual: String, val expected: String, val pass: Boolean)

data class SelfTestResult(
    val rows: List<CheckRow>,
    val Xn: Double,
    val Xo: Double,
    val xnPass: Boolean,
    val xoPass: Boolean,
    val hasLayerCheck: Boolean,
    val xsActual: Double,
    val xminActual: Double,
    val xsPass: Boolean,
    val xminPass: Boolean,
    val allPass: Boolean,
)

/** Real field data: Nyerere Road Km 0+575 - Km 0+700, G7, n=4, 9/5/2024, RFI 2024-199. */
fun runSelfTest(layers: List<Layer>): SelfTestResult {
    data class Golden(
        val input: HoleInput,
        val D: Double,
        val expE: Double, val expG: Double, val expH: Double,
        val expM: Double, val expN: Double, val expO: Double,
        val expU: String,
        val expP: Double, val expS: Double,
    )

    val sandDensity = 1.302
    val mdd = 2.254
    val omc = 7.6
    val holes = listOf(
        Golden(HoleInput(6991.0, 6000.0, 1470.0, 353.75, 331.35, 25.71), 682.0, 3848.0, 2955.0, 2.365, 22.4, 305.64, 7.3, "OK", 2.204, 97.8),
        Golden(HoleInput(6872.0, 6000.0, 1463.0, 358.16, 337.45, 27.53), 677.0, 3860.0, 2965.0, 2.318, 20.71, 309.92, 6.7, "OK", 2.173, 96.4),
        Golden(HoleInput(6908.0, 6000.0, 1376.0, 400.25, 376.31, 26.43), 677.0, 3947.0, 3031.0, 2.279, 23.94, 349.88, 6.8, "OK", 2.133, 94.6),
        Golden(HoleInput(6886.0, 6000.0, 1381.0, 309.72, 291.5, 25.73), 682.0, 3937.0, 3024.0, 2.277, 18.22, 265.77, 6.9, "OK", 2.131, 94.6),
    )

    val rows = mutableListOf<CheckRow>()
    var allPass = true
    val comps = mutableListOf<Double>()

    fun numRow(hole: Int, label: String, actual: Double, expected: Double, tol: Double) {
        val pass = abs(actual - expected) <= tol
        if (!pass) allPass = false
        rows += CheckRow(hole, label, format3(actual), format3(expected), pass)
    }

    holes.forEachIndexed { i, g ->
        val c = calcHole(g.input, g.D, sandDensity, mdd, omc)
        comps += c.S
        val hole = i + 1
        numRow(hole, "E (Sand in Hole)", c.E, g.expE, 0.5)
        numRow(hole, "G (Volume)", c.G, g.expG, 1.0)
        numRow(hole, "H (Wet Density)", c.H, g.expH, 0.001)
        numRow(hole, "M (Wt of Water)", c.M, g.expM, 0.01)
        numRow(hole, "N (Wt of Dry Soil)", c.N, g.expN, 0.01)
        numRow(hole, "O (Moisture %)", c.O, g.expO, 0.05)
        numRow(hole, "P (Dry Density)", c.P, g.expP, 0.001)
        numRow(hole, "S (Compaction %)", c.S, g.expS, 0.05)

        val u = when (c.U) {
            Tolerance.OK -> "OK"
            Tolerance.NOT_OK -> "NOT OK"
            Tolerance.NONE -> "-"
        }
        val uPass = u == g.expU
        if (!uPass) allPass = false
        rows += CheckRow(hole, "U (Moisture Tolerance)", u, g.expU, uPass)
    }

    val Xn = comps.sum() / comps.size
    val Xo = comps.min()
    val xnPass = abs(Xn - 95.8) <= 0.05
    val xoPass = abs(Xo - 94.6) <= 0.05
    if (!xnPass || !xoPass) allPass = false

    // Table 7205/1 for G7, n=4. Like the JS/C++ versions this does NOT affect allPass.
    val g7 = findLayer(layers, "g7")
    val xsActual = g7?.let { lookup(4, it.xs) } ?: 0.0
    val xminActual = g7?.let { lookup(4, it.xmin) } ?: 0.0
    val xsPass = abs(xsActual - 93.2) <= 0.05
    val xminPass = abs(xminActual - 87.9) <= 0.05

    return SelfTestResult(
        rows = rows,
        Xn = Xn, Xo = Xo, xnPass = xnPass, xoPass = xoPass,
        hasLayerCheck = g7 != null,
        xsActual = xsActual, xminActual = xminActual,
        xsPass = xsPass, xminPass = xminPass,
        allPass = allPass,
    )
}
