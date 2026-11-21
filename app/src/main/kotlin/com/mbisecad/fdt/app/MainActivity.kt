package com.mbisecad.fdt.app

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.AdapterView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.mbisecad.fdt.FdtRecord
import com.mbisecad.fdt.calcSingle
import com.mbisecad.fdt.defaultLayers
import com.mbisecad.fdt.runSelfTest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/** Real single-point FDT screen backed by the pure [com.mbisecad.fdt] engine. */
class MainActivity : BaseActivity() {
    private lateinit var initialSand: EditText
    private lateinit var finalSand: EditText
    private lateinit var sandCone: EditText
    private lateinit var densitySand: EditText
    private lateinit var soil: EditText
    private lateinit var omc: EditText
    private lateinit var mdd: EditText
    private lateinit var required: EditText
    private lateinit var location: EditText
    private lateinit var layer: EditText
    private lateinit var summaryPanel: FdtSummaryPanel
    private var lastSingleRecord: FdtRecord? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(FdtColors.BACKGROUND)
            setPadding(0, 0, 0, dp(24))
        }
        form.fdtHeader(
            "FDT Calculator",
            "Sand Cone Method · Field Density Test",
        )

        val cylinders = CylinderStore(this).all()

        form.fdtCard("Single point test") {
            val cylinderSpinner = fdtSpinner(
                items = cylinders,
                label = "Cylinder",
            ) { c -> "Cylinder ${c.n} (cone ${c.sandInCone.toInt()} g)" }

            fun grid2(l1: String, v1: String, l2: String, v2: String): Pair<EditText, EditText> {
                val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
                addView(row)
                return row.fdtCompactField(l1, v1) to row.fdtCompactField(l2, v2)
            }

            (grid2("Initial sand in cylinder (g)", "6000", "Final sand in cylinder (g)", ""))
                .let { initialSand = it.first; finalSand = it.second }
            (grid2("Sand in cone (g)", "640", "Density of sand (g/cm³)", "1.302"))
                .let { sandCone = it.first; densitySand = it.second }
            (grid2("Weight of soil from hole (g)", "", "OMC (%)", "7.6"))
                .let { soil = it.first; omc = it.second }
            (grid2("MDD (g/cm³)", "2.254", "Required compaction (%) — optional", ""))
                .let { mdd = it.first; required = it.second }
            (grid2("Location / chainage", "", "Layer tested (mf. Backfill)", ""))
                .let { location = it.first; layer = it.second }

            // Focus order: Initial → Final → Cone → Density → Soil → OMC → MDD → Required → Location → Layer.
            chainFocus(
                initialSand, finalSand, sandCone, densitySand, soil, omc, mdd, required,
                location, layer,
            )

            cylinderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long,
                ) {
                    sandCone.setText(cylinders[position].sandInCone.toInt().toString())
                }

                override fun onNothingSelected(parent: AdapterView<*>) = Unit
            }

            val actions = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            addView(actions)
            actions.fdtButton("Calculate", FdtButtonStyle.PRIMARY, weight = 1f) { calculate() }
            actions.fdtButton("Clear", FdtButtonStyle.GHOST, weight = 1f) { clearForm() }
        }

        form.fdtCard("Tools & data") {
            val tools1 = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            addView(tools1)
            tools1.fdtButton("Engine self-test", FdtButtonStyle.SECONDARY, weight = 1f) {
                showSelfTest()
            }
            tools1.fdtButton("Worksheet", FdtButtonStyle.SECONDARY, weight = 1f) {
                startActivity(Intent(this@MainActivity, WorksheetActivity::class.java))
            }

            val tools2 = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            addView(tools2)
            tools2.fdtButton("Save single result", FdtButtonStyle.PRIMARY, weight = 1f) {
                val record = lastSingleRecord
                if (record == null) {
                    Toast.makeText(this@MainActivity, "Calculate kwanza.".loc(), Toast.LENGTH_SHORT).show()
                } else {
                    val count = RecordStore(this@MainActivity).save(record)
                    Toast.makeText(
                        this@MainActivity, "Imehifadhiwa ($count records).".loc(), Toast.LENGTH_SHORT
                    ).show()
                }
            }
            tools2.fdtButton("Saved results", FdtButtonStyle.SECONDARY, weight = 1f) {
                startActivity(Intent(this@MainActivity, RecordsActivity::class.java))
            }
        }

        val resultCard = form.fdtCard("Result")
        summaryPanel = resultCard.fdtSummaryPanel()
        summaryPanel.setRows(listOf("Ready" to "Jaza data ya field kisha bonyeza Calculate."))
        summaryPanel.setStatus("Ready")
        resultCard.fdtButton("Export PDF for this result", FdtButtonStyle.SECONDARY) {
            exportPdf()
        }

        setContentView(ScrollView(this).apply { addView(form) })
    }

    private fun valueOf(field: EditText, label: String): Double? {
        val text = field.text.toString().trim()
        if (text.isEmpty()) {
            field.fdtError("Jaza sehemu hii.")
            showError("Jaza sehemu ya \"$label\" kabla ya kubonyeza Calculate.")
            field.requestFocus()
            return null
        }
        return text.toDoubleOrNull().also {
            if (it == null) {
                field.fdtError("Andika namba sahihi.")
                showError("Thamani ya \"$label\" si namba sahihi.")
                field.requestFocus()
            } else {
                field.fdtError(null)
            }
        }
    }

    private fun calculate() {
        val initial = valueOf(initialSand, "Initial Sand") ?: return
        val final = valueOf(finalSand, "Final Sand") ?: return
        val cone = valueOf(sandCone, "Sand in Cone") ?: return
        val density = valueOf(densitySand, "Density of Sand") ?: return
        val soilWeight = valueOf(soil, "Soil from Hole") ?: return
        val moisture = valueOf(omc, "OMC") ?: return
        val mddValue = valueOf(mdd, "MDD") ?: return

        val requiredText = required.text.toString().trim()
        val requiredValue = if (requiredText.isEmpty()) null else requiredText.toDoubleOrNull()
        if (requiredValue == null && requiredText.isNotEmpty()) {
            required.fdtError("Andika namba sahihi.")
            showError("Thamani ya \"Required Compaction\" si namba sahihi.")
            required.requestFocus()
            return
        }
        required.fdtError(null)

        listOf(initialSand, finalSand, sandCone, densitySand, soil, omc, mdd)
            .forEach { it.fdtError(null) }

        val r = calcSingle(
            initialSand = initial,
            finalSand = final,
            sandCone = cone,
            sandDensity = density,
            soil = soilWeight,
            omc = moisture,
            mdd = mddValue,
            required = requiredValue,
        )

        if (!r.ok) {
            showError(r.error ?: "Hesabu imeshindikana.")
            return
        }

        val req = r.required
        summaryPanel.setRows(
            listOf(
                "Sand in Hole" to "${fmt(r.sandHole, 2)} g",
                "Volume" to "${fmt(r.volume, 2)} cm³",
                "Wet Density" to "${fmt(r.wetDensity, 3)} g/cm³",
                "Dry Density" to "${fmt(r.dryDensity, 3)} g/cm³",
                "Compaction" to "${fmt(r.compaction, 2)} %",
                "Required" to if (req == null) "-" else "${fmt(req, 2)} %",
            )
        )
        summaryPanel.setStatus(r.status)

        lastSingleRecord = FdtRecord(
            id = UUID.randomUUID().toString(),
            createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()),
            mode = "SINGLE",
            location = location.text.toString().trim(),
            layer = layer.text.toString().trim(),
            sandDensity = density,
            mdd = mddValue,
            omc = moisture,
            required = r.required,
            holes = emptyList(),
            compaction = r.compaction,
            status = r.status,
        )
    }

    private fun showError(message: String) {
        summaryPanel.error(message)
    }

    private fun clearForm() {
        listOf(finalSand, soil, required, location, layer).forEach { it.text?.clear() }
        listOf(initialSand, finalSand, sandCone, densitySand, soil, omc, mdd, required, location, layer)
            .forEach { it.fdtError(null) }
        initialSand.setText("6000")
        sandCone.setText("640")
        densitySand.setText("1.302")
        omc.setText("7.6")
        mdd.setText("2.254")
        summaryPanel.setRows(listOf("Ready" to "Jaza data ya field kisha bonyeza Calculate."))
        summaryPanel.setStatus("Ready")
    }

    private fun showSelfTest() {
        val t = runSelfTest(defaultLayers())
        val message = buildString {
            appendLine("Self-test: ${if (t.allPass) "PASS" else "FAIL"}".loc())
            appendLine("Golden checks: ${t.rows.count { it.pass }}/${t.rows.size}".loc())
            appendLine("Xn: ${fmt(t.Xn, 3)} (tarajio 95.8)".loc())
            appendLine("Xo: ${fmt(t.Xo, 3)} (tarajio 94.6)".loc())
            appendLine("G7 Xs/Xmin: ${fmt(t.xsActual, 1)} / ${fmt(t.xminActual, 1)}".loc())
        }
        AlertDialog.Builder(this)
            .setTitle("Engine self-test".loc())
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun exportPdf() {
        val record = lastSingleRecord
        if (record == null) {
            Toast.makeText(this, "Calculate kwanza.".loc(), Toast.LENGTH_SHORT).show()
            return
        }
        val name = fdtPdfName("SINGLE", record.location, record.layer, "single")
        try {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, name)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
            }
            val uri: Uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("Downloads haifunguki")
            val stream = contentResolver.openOutputStream(uri)
                ?: throw IllegalStateException("File haiandikiki")
            stream.use { FdtPdf(ProjectStore(this).load(), it).writeSingle(listOf(record)) }
            Toast.makeText(this, "PDF: Download/$name".loc(), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "PDF imeshindikana: ${e.message}".loc(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun fmt(value: Double, decimals: Int): String =
        String.format(Locale.US, "%.${decimals}f", value)
}
