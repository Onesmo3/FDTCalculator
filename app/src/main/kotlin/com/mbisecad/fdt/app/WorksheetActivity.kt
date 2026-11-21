package com.mbisecad.fdt.app

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.text.InputType
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.mbisecad.fdt.FdtHoleData
import com.mbisecad.fdt.FdtRecord
import com.mbisecad.fdt.HoleInput
import com.mbisecad.fdt.Tolerance
import com.mbisecad.fdt.calcHole
import com.mbisecad.fdt.defaultLayers
import com.mbisecad.fdt.summarize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/** Worksheet screen: many holes -> Xn / Xo / Xs / Xmin -> Pass/Fail. */
class WorksheetActivity : BaseActivity() {
    private data class HoleRow(
        val view: LinearLayout,
        val A: EditText, val B: EditText, val C: EditText, val D: EditText,
        val J: EditText, val K: EditText, val L: EditText,
        val chainage: EditText, val offset: EditText, val depth: EditText,
        val cylinder: EditText, val cylinderWeight: EditText, val containerNo: EditText,
    )

    private lateinit var testDate: EditText
    private lateinit var location: EditText
    private lateinit var road: EditText
    private lateinit var rfiNo: EditText
    private lateinit var technician: EditText
    private lateinit var checkedBy: EditText
    private lateinit var materialSource: EditText
    private lateinit var mddRefNo: EditText
    private lateinit var sandCalibRefNo: EditText
    private lateinit var maxLayerThickness: EditText
    private lateinit var depthBelowFormation: EditText
    private lateinit var remarks: EditText
    private lateinit var density: EditText
    private lateinit var mdd: EditText
    private lateinit var omc: EditText
    private lateinit var layerSpinner: Spinner
    private lateinit var layerNo: EditText
    private lateinit var holesContainer: LinearLayout
    private lateinit var summaryPanel: FdtSummaryPanel

    private val layers = defaultLayers()
    private val cylinders by lazy { CylinderStore(this).all() }
    private val holeRows = mutableListOf<HoleRow>()
    private var lastRecord: FdtRecord? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(FdtColors.BACKGROUND)
            setPadding(0, 0, 0, dp(24))
        }
        form.fdtHeader(
            "FDT Worksheet",
            "Sand Cone Method · multi-hole summary",
        )

        form.fdtCard("Report info") {
            fun grid2(firstLabel: String, secondLabel: String): Pair<EditText, EditText> {
                val r = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
                addView(r)
                return r.fdtCompactField(firstLabel, "", false) to r.fdtCompactField(secondLabel, "", false)
            }

            (grid2("Test date", "Location")).let { testDate = it.first; location = it.second }
            (grid2("Road", "RFI No")).let { road = it.first; rfiNo = it.second }
            (grid2("Technician", "Checked by")).let { technician = it.first; checkedBy = it.second }
            (grid2("Material source", "MDD reference No")).let { materialSource = it.first; mddRefNo = it.second }
            (grid2("Sand calibration ref No", "Max layer thickness (mm)")).let {
                sandCalibRefNo = it.first; maxLayerThickness = it.second
            }
            (grid2("Depth below formation (mm)", "Remarks")).let {
                depthBelowFormation = it.first; remarks = it.second
            }
        }

        form.fdtCard("Test parameters") {
            val r1 = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            addView(r1)
            density = r1.fdtCompactField("Density of sand (g/cm³)", "1.302")
            mdd = r1.fdtCompactField("MDD (g/cm³)", "2.254")

            val r2 = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            addView(r2)
            omc = r2.fdtCompactField("OMC (%)", "7.6")
            layerNo = r2.fdtCompactField("Layer no (hiari, mf. 1ˢᵗ layer)", "", false)

            val g7 = layers.indexOfFirst { it.name.equals("G7", ignoreCase = true) }
            layerSpinner = fdtSpinner(
                items = layers,
                label = "Layer (Table 7205/1)",
                selectedIndex = if (g7 >= 0) g7 else 0,
            ) { it.name }
            chainFocus(density, mdd, omc, layerNo)
        }

        form.fdtCard("Holes") {
            holesContainer = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
            addView(holesContainer)

            // Golden field data: Nyerere Road Km 0+575 - Km 0+700, G7, n=4.
            val golden = listOf(
                mapOf("A" to "6991", "B" to "6000", "C" to "1470", "D" to "682", "J" to "353.75", "K" to "331.35", "L" to "25.71"),
                mapOf("A" to "6872", "B" to "6000", "C" to "1463", "D" to "677", "J" to "358.16", "K" to "337.45", "L" to "27.53"),
                mapOf("A" to "6908", "B" to "6000", "C" to "1376", "D" to "677", "J" to "400.25", "K" to "376.31", "L" to "26.43"),
                mapOf("A" to "6886", "B" to "6000", "C" to "1381", "D" to "682", "J" to "309.72", "K" to "291.50", "L" to "25.73"),
            )
            golden.forEach { addHole(it) }

            val actions = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            addView(actions)
            actions.fdtButton("Add hole", FdtButtonStyle.SECONDARY, weight = 1f) { addHole(null) }
            actions.fdtButton("Remove last", FdtButtonStyle.GHOST, weight = 1f) {
                if (holeRows.isNotEmpty()) {
                    val row = holeRows.removeAt(holeRows.lastIndex)
                    holesContainer.removeView(row.view)
                }
            }
        }

        // Vitufe vyote vitatu viko safu moja; vimepunguzwa ili vitoshee.
        form.fdtCard("Actions") {
            val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            addView(row)
            row.fdtButton(
                "Calculate worksheet", FdtButtonStyle.PRIMARY, weight = 1f,
                textSizeSp = 10.5f, minHeightDp = 38, padHDp = 2, padVDp = 4,
            ) { calculate() }
            row.fdtButton(
                "Save result", FdtButtonStyle.SECONDARY, weight = 1f,
                textSizeSp = 10.5f, minHeightDp = 38, padHDp = 2, padVDp = 4,
            ) {
                val record = lastRecord
                if (record == null) {
                    Toast.makeText(this@WorksheetActivity, "Calculate kwanza.".loc(), Toast.LENGTH_SHORT).show()
                } else {
                    val count = RecordStore(this@WorksheetActivity).save(record)
                    Toast.makeText(
                        this@WorksheetActivity, "Imehifadhiwa ($count records).".loc(), Toast.LENGTH_SHORT
                    ).show()
                }
            }
            row.fdtButton(
                "← Single point", FdtButtonStyle.GHOST, weight = 1f,
                textSizeSp = 10.5f, minHeightDp = 38, padHDp = 2, padVDp = 4,
            ) { finish() }
        }

        val resultCard = form.fdtCard("Worksheet result")
        summaryPanel = resultCard.fdtSummaryPanel()
        summaryPanel.setRows(listOf("Ready" to "Data ya Nyerere Road imejazwa tayari."))
        summaryPanel.setStatus("Ready")

        setContentView(ScrollView(this).apply { addView(form) })
    }

    private fun addHole(preset: Map<String, String>?) {
        val number = holeRows.size + 1
        holesContainer.fdtCard("Hole $number") {
            val edits = mutableMapOf<String, EditText>()

            fun gridRow(vararg specs: Triple<String, String, Boolean>) {
                val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
                addView(row)
                specs.forEach { (label, key, numeric) ->
                    edits[key] = row.fdtCompactField(label, preset?.get(key) ?: "", numeric)
                }
                if (specs.size == 1) {
                    row.addView(android.view.View(context).apply {
                        layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
                    })
                }
            }

            // Mtiririko wa kushoto → kulia, safu mbili kwa kila mstari.
            gridRow(
                Triple("A · soil (g)", "A", true),
                Triple("B · initial sand (g)", "B", true),
            )
            gridRow(
                Triple("C · final sand (g)", "C", true),
                Triple("D · cone (g)", "D", true),
            )
            gridRow(
                Triple("J · container + wet (g)", "J", true),
                Triple("K · container + dry (g)", "K", true),
            )
            gridRow(
                Triple("L · container (g)", "L", true),
                Triple("Chainage", "chainage", false),
            )
            gridRow(
                Triple("Offset / Side (m)", "offset", false),
                Triple("Depth (mm)", "depth", false),
            )
            gridRow(
                Triple("Cylinder No", "cylinder", false),
                Triple("W · cylinder weight (g)", "cylinderWeight", true),
            )
            gridRow(Triple("Container No (I)", "containerNo", false))

            val cylinderPreset = fdtSpinner(
                items = cylinders,
                label = "Cylinder preset (auto-fills D + weight)",
                selectedIndex = 0,
            ) { "Cyl ${it.n} · cone ${it.sandInCone.toInt()} g · ${it.weight.toInt()} g" }
            cylinderPreset.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val c = cylinders[position]
                    edits["D"]?.setText(c.sandInCone.toInt().toString())
                    edits["cylinder"]?.setText(c.n.toString())
                    edits["cylinderWeight"]?.setText(c.weight.toInt().toString())
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            val a = edits["A"]!!
            val b = edits["B"]!!
            val c = edits["C"]!!
            val d = edits["D"]!!
            val j = edits["J"]!!
            val k = edits["K"]!!
            val l = edits["L"]!!
            val chainage = edits["chainage"]!!
            val offset = edits["offset"]!!
            val depth = edits["depth"]!!
            val cylinder = edits["cylinder"]!!
            val cylinderWeight = edits["cylinderWeight"]!!
            val containerNo = edits["containerNo"]!!

            chainFocus(
                a, b, c, d, j, k, l,
                chainage, offset, depth, cylinder, cylinderWeight, containerNo,
            )

            holeRows.add(
                HoleRow(
                    this, a, b, c, d, j, k, l,
                    chainage, offset, depth, cylinder, cylinderWeight, containerNo,
                )
            )
        }
    }

    private fun number(field: EditText): Double? {
        val text = field.text.toString().trim()
        if (text.isEmpty()) {
            field.fdtError("Jaza sehemu hii.")
            return null
        }
        val value = text.toDoubleOrNull()
        if (value == null) field.fdtError("Andika namba sahihi.") else field.fdtError(null)
        return value
    }

    /** Cylinder weight is optional; an empty field must not be marked as an error. */
    private fun optionalNumber(field: EditText): Double? {
        val text = field.text.toString().trim()
        if (text.isEmpty()) {
            field.fdtError(null)
            return null
        }
        val value = text.toDoubleOrNull()
        if (value == null) field.fdtError("Andika namba sahihi.") else field.fdtError(null)
        return value
    }

    private fun showError(message: String) {
        summaryPanel.error(message)
    }

    private fun calculate() {
        val sandDensity = number(density)
        val mddValue = number(mdd)
        val omcValue = number(omc)
        if (sandDensity == null || mddValue == null || omcValue == null) {
            showError("Jaza Density of Sand, MDD na OMC kwanza.")
            return
        }
        if (sandDensity <= 0 || mddValue <= 0) {
            showError("Density of Sand na MDD lazima ziwe zaidi ya sifuri.")
            return
        }

        val comps = mutableListOf<Double>()
        val details = mutableListOf<String>()
        val holeData = mutableListOf<FdtHoleData>()
        holeRows.forEachIndexed { index, row ->
            val a = number(row.A)
            val b = number(row.B)
            val c = number(row.C)
            val d = number(row.D)
            val j = number(row.J)
            val k = number(row.K)
            val l = number(row.L)
            if (a == null || b == null || c == null || d == null || j == null || k == null || l == null) {
                details += "Hole ${index + 1}: incomplete - skipped"
                return@forEachIndexed
            }

            val hole = calcHole(
                HoleInput(A = a, B = b, C = c, J = j, K = k, L = l),
                D = d,
                sandDensity = sandDensity,
                mdd = mddValue,
                omc = omcValue,
            )
            comps += hole.S
            val u = when (hole.U) {
                Tolerance.OK -> "OK"
                Tolerance.NOT_OK -> "NOT OK"
                Tolerance.NONE -> "-"
            }
            holeData += FdtHoleData(
                A = a, B = b, C = c, D = d, J = j, K = k, L = l,
                sandInHole = hole.E,
                sandDensityOfHole = sandDensity,
                volume = hole.G,
                wetDensity = hole.H,
                dryDensity = hole.P,
                moisture = hole.O,
                compaction = hole.S,
                tolerance = u,
                chainage = row.chainage.text.toString().trim(),
                offset = row.offset.text.toString().trim(),
                depth = row.depth.text.toString().trim(),
                cylinder = row.cylinder.text.toString().trim(),
                cylinderWeight = optionalNumber(row.cylinderWeight) ?: 0.0,
                containerNo = row.containerNo.text.toString().trim(),
            )
            details += "Hole ${index + 1}: E=${fmt(hole.E, 0)}  S=${fmt(hole.S, 2)}%  U=$u"
        }

        if (comps.isEmpty()) {
            showError("Hakuna hole iliyokamilika. Jaza angalau hole moja.")
            return
        }

        listOf(density, mdd, omc).forEach { it.fdtError(null) }
        holeRows.forEach { row ->
            listOf(
                row.A, row.B, row.C, row.D, row.J, row.K, row.L, row.cylinderWeight
            ).forEach { it.fdtError(null) }
        }

        val layer = layers[layerSpinner.selectedItemPosition]
        val summary = summarize(comps, holeRows.size, layer)
        val ordinal = layerNo.text.toString().trim().trim('(', ')').trim()
        val layerLabel = if (ordinal.isEmpty()) layer.name else "${layer.name}($ordinal)"

        lastRecord = FdtRecord(
            id = UUID.randomUUID().toString(),
            createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()),
            testDate = testDate.text.toString().trim(),
            mode = "WORKSHEET",
            layer = layerLabel,
            sandDensity = sandDensity,
            mdd = mddValue,
            omc = omcValue,
            holes = holeData,
            status = summary.status,
            xn = summary.Xn,
            xo = summary.Xo,
            xs = summary.Xs,
            xmin = summary.Xmin,
            location = location.text.toString().trim(),
            technician = technician.text.toString().trim(),
            checkedBy = checkedBy.text.toString().trim(),
            materialSource = materialSource.text.toString().trim(),
            road = road.text.toString().trim(),
            rfiNo = rfiNo.text.toString().trim(),
            mddRefNo = mddRefNo.text.toString().trim(),
            sandCalibRefNo = sandCalibRefNo.text.toString().trim(),
            maxLayerThickness = maxLayerThickness.text.toString().trim(),
            depthBelowFormation = depthBelowFormation.text.toString().trim(),
            remarks = remarks.text.toString().trim(),
        )

        val rows = mutableListOf<Pair<String, String>>()
        rows += "Layer" to layerLabel
        rows += "Holes (n)" to "${summary.n}  (${comps.size} complete)"
        rows += "Xn  (average)" to "${fmt(summary.Xn ?: 0.0, 2)} %"
        rows += "Xo  (minimum)" to "${fmt(summary.Xo ?: 0.0, 2)} %"
        rows += "Xs  (required)" to "${summary.Xs?.let { fmt(it, 2) } ?: "-" } %"
        rows += "Xmin (required)" to "${summary.Xmin?.let { fmt(it, 2) } ?: "-" } %"
        details.forEach { rows += "•" to it }
        summaryPanel.setRows(rows)
        summaryPanel.setStatus(summary.status)
    }

    private fun fmt(value: Double, decimals: Int): String =
        String.format(Locale.US, "%.${decimals}f", value)
}
