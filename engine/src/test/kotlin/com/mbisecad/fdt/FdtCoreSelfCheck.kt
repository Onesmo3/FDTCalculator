package com.mbisecad.fdt

import kotlin.math.abs
import kotlin.system.exitProcess

/**
 * Dependency-free test runner so the core can be verified with just kotlinc/kotlin.
 * Run: ./run-tests.sh
 */

private var passedCount = 0
private val failures = mutableListOf<String>()

private fun test(name: String, body: () -> Unit) {
    try {
        body()
        passedCount++
        println("PASS  $name")
    } catch (e: Throwable) {
        failures += "$name -> ${e.message}"
        println("FAIL  $name -> ${e.message}")
    }
}

private fun expect(condition: Boolean, message: String) {
    if (!condition) throw AssertionError(message)
}

private fun expectClose(actual: Double, expected: Double, tolerance: Double, label: String) {
    expect(abs(actual - expected) <= tolerance, "$label: expected $expected +/- $tolerance, got $actual")
}

fun runAll(): List<String> {
    test("lookup uses the last matching breakpoint and treats 0 as absent") {
        val ys = listOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)
        expect(lookup(2, ys) == null, "n < 3 must return null")
        expectClose(lookup(3, ys)!!, 1.0, 0.0, "n=3")
        expectClose(lookup(4, ys)!!, 1.0, 0.0, "n=4 falls back to breakpoint 3")
        expectClose(lookup(5, ys)!!, 2.0, 0.0, "n=5")
        expectClose(lookup(9, ys)!!, 6.0, 0.0, "n=9")
        expectClose(lookup(20, ys)!!, 6.0, 0.0, "n>9 uses the last value")
        expect(lookup(5, listOf(1.0, 0.0, 3.0, 4.0, 5.0, 6.0)) == null, "0 must mean 'no value'")
    }

    test("findLayer is case-insensitive and ignores surrounding spaces") {
        val layers = defaultLayers()
        expect(layers.size == 22, "expected 22 default layers, got ${layers.size}")
        expect(findLayer(layers, "  g7 ")?.name == "G7", "G7 lookup failed")
        expect(findLayer(layers, "c1-base")?.name == "C1-Base", "C1-Base lookup failed")
        expect(findLayer(layers, "unknown") == null, "unknown layer must be null")
        expect(findLayer(layers, "  ") == null, "blank layer must be null")
    }

    test("findCylinder returns the matching cylinder") {
        expect(findCylinder(defaultCylinders(), 2)?.sandInCone == 656.0, "cylinder 2 mismatch")
        expect(findCylinder(defaultCylinders(), 99) == null, "unknown cylinder must be null")
    }

    test("calcHole matches the Nyerere Road golden data (hole 1)") {
        val h = HoleInput(6991.0, 6000.0, 1470.0, 353.75, 331.35, 25.71)
        val r = calcHole(h, D = 682.0, sandDensity = 1.302, mdd = 2.254, omc = 7.6)
        expectClose(r.E, 3848.0, 0.5, "E sand in hole")
        expectClose(r.G, 2955.0, 1.0, "G volume")
        expectClose(r.H, 2.365, 0.001, "H wet density")
        expectClose(r.M, 22.4, 0.01, "M water weight")
        expectClose(r.N, 305.64, 0.01, "N dry soil weight")
        expectClose(r.O, 7.3, 0.05, "O moisture %")
        expectClose(r.P, 2.204, 0.001, "P dry density")
        expectClose(r.S, 97.8, 0.05, "S compaction %")
        expect(r.U == Tolerance.OK, "moisture tolerance should be OK")
    }

    test("summarize handles awaiting, missing requirement, pass and fail") {
        val g7 = findLayer(defaultLayers(), "g7")!!
        expect(summarize(emptyList(), 4, g7).status == "Awaiting data", "empty data")
        expect(summarize(listOf(95.0, 96.0), 2, g7).status == "No Requirement Yet", "n<3")
        expect(summarize(listOf(95.0, 96.0), 4, null).status == "No Requirement Yet", "no layer")

        val passed = summarize(listOf(95.0, 96.0), 4, g7)
        expect(passed.status == "Passed", "expected Passed, got ${passed.status}")
        expectClose(passed.Xn!!, 95.5, 0.0001, "Xn")
        expectClose(passed.Xo!!, 95.0, 0.0001, "Xo")
        expectClose(passed.Xs!!, 93.2, 0.0001, "Xs")
        expectClose(passed.Xmin!!, 87.9, 0.0001, "Xmin")

        // Xn = 93.5 passes Xs (93.2), but Xo = 86 fails Xmin (87.9)
        val failedByMin = summarize(listOf(101.0, 86.0), 4, g7)
        expect(failedByMin.status == "Failed", "Xmin violation must fail, got ${failedByMin.status}")
    }

    test("calcSingle computes correctly and reports statuses") {
        val base = calcSingle(
            initialSand = 6000.0, finalSand = 1470.0, sandCone = 682.0,
            sandDensity = 1.302, soil = 6991.0, omc = 7.6, mdd = 2.254,
            required = 93.2,
        )
        expect(base.ok, "calcSingle should succeed: ${base.error}")
        expectClose(base.sandHole, 3848.0, 0.5, "sandHole")
        expectClose(base.compaction, 97.5, 0.1, "compaction")
        expect(base.status == "Passed", "expected Passed, got ${base.status}")

        val noRequirement = calcSingle(6000.0, 1470.0, 682.0, 1.302, 6991.0, 7.6, 2.254, null)
        expect(noRequirement.status == "No Requirement Yet", "missing requirement")

        val failed = calcSingle(6000.0, 1470.0, 682.0, 1.302, 6991.0, 7.6, 2.254, 99.0)
        expect(failed.status == "Failed", "compaction below requirement")

        expect(calcSingle(1.0, 1.0, 1.0, 0.0, 1.0, 7.6, 2.254, 93.2).error != null, "zero sand density")
        expect(calcSingle(1.0, 1.0, 1.0, 1.302, 1.0, 7.6, 0.0, 93.2).error != null, "zero MDD")
        expect(calcSingle(100.0, 90.0, 20.0, 1.302, 1.0, 7.6, 2.254, 93.2).error != null, "non-positive sand in hole")
    }

    test("runSelfTest passes all golden checks") {
        val result = runSelfTest(defaultLayers())
        expect(result.rows.size == 36, "expected 36 check rows, got ${result.rows.size}")
        expect(result.rows.all { it.pass }, "some golden checks failed: ${result.rows.filter { !it.pass }}")
        expect(result.allPass, "self-test allPass is false")
        expect(result.xnPass && result.xoPass, "Xn/Xo checks failed")
        expect(result.hasLayerCheck, "G7 layer check missing")
        expect(result.xsPass && result.xminPass, "layer table checks failed")
        expectClose(result.Xn, 95.8, 0.05, "self-test Xn")
        expectClose(result.Xo, 94.6, 0.05, "self-test Xo")
    }

    println()
    println("$passedCount passed, ${failures.size} failed")
    return failures
}

fun main() {
    val failures = runAll()
    if (failures.isNotEmpty()) {
        failures.forEach { println("  - $it") }
        exitProcess(1)
    }
}
