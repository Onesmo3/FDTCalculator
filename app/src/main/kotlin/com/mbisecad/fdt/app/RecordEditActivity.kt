package com.mbisecad.fdt.app

import android.app.Activity
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import com.mbisecad.fdt.FdtRecord

/** Edits metadata + hole values, then recomputes worksheet results on save. */
class RecordEditActivity : BaseActivity() {
    private lateinit var record: FdtRecord
    private val holeEdits = mutableListOf<HoleEdit>()

    private lateinit var project: EditText
    private lateinit var site: EditText
    private lateinit var testDate: EditText
    private lateinit var location: EditText
    private lateinit var road: EditText
    private lateinit var rfiNo: EditText
    private lateinit var technician: EditText
    private lateinit var checkedBy: EditText
    private lateinit var materialSource: EditText
    private lateinit var layerEdit: EditText
    private lateinit var mddRefNo: EditText
    private lateinit var sandCalibRefNo: EditText
    private lateinit var maxLayerThickness: EditText
    private lateinit var depthBelowFormation: EditText
    private lateinit var remarks: EditText
    private lateinit var sandDensity: EditText
    private lateinit var mdd: EditText
    private lateinit var omc: EditText
    private lateinit var required: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getStringExtra(EXTRA_ID)
        record = RecordStore(this).all().firstOrNull { it.id == id } ?: run {
            Toast.makeText(this, "Record haipatikani.".loc(), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(FdtColors.BACKGROUND)
            setPadding(0, 0, 0, dp(24))
        }
        root.fdtHeader("Edit record", "${record.mode} · ${record.createdAt}")

        fun grid2(l1: String, v1: String, l2: String, v2: String): Pair<EditText, EditText> {
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            root.addView(row)
            return row.fdtCompactField(l1, v1, false) to row.fdtCompactField(l2, v2, false)
        }

        val reportCard = root.fdtCard("Report info")
        fun reportRow(a: String, b: String, av: String, bv: String): Pair<EditText, EditText> {
            val row = LinearLayout(reportCard.context).apply { orientation = LinearLayout.HORIZONTAL }
            reportCard.addView(row)
            return row.fdtCompactField(a, av, false) to row.fdtCompactField(b, bv, false)
        }
        (reportRow("Project", "Site", record.project, record.site)).let { project = it.first; site = it.second }
        (reportRow("Test date", "Location", record.testDate, record.location)).let { testDate = it.first; location = it.second }
        (reportRow("Road", "RFI No", record.road, record.rfiNo)).let { road = it.first; rfiNo = it.second }
        (reportRow("Technician", "Checked by", record.technician, record.checkedBy)).let { technician = it.first; checkedBy = it.second }
        (reportRow("Material source", "Layer", record.materialSource, record.layer)).let { materialSource = it.first; layerEdit = it.second }
        (reportRow("MDD reference No", "Sand calibration ref No", record.mddRefNo, record.sandCalibRefNo)).let { mddRefNo = it.first; sandCalibRefNo = it.second }
        (reportRow("Max layer thickness (mm)", "Depth below formation (mm)", record.maxLayerThickness, record.depthBelowFormation)).let { maxLayerThickness = it.first; depthBelowFormation = it.second }
        (reportRow("Remarks", "", record.remarks, "")).let { remarks = it.first }

        val paramsCard = root.fdtCard("Test parameters")
        val p1 = LinearLayout(paramsCard.context).apply { orientation = LinearLayout.HORIZONTAL }
        paramsCard.addView(p1)
        sandDensity = p1.fdtCompactField("Density of sand (g/cm³)", record.sandDensity.toString())
        mdd = p1.fdtCompactField("MDD (g/cm³)", record.mdd.toString())
        val p2 = LinearLayout(paramsCard.context).apply { orientation = LinearLayout.HORIZONTAL }
        paramsCard.addView(p2)
        omc = p2.fdtCompactField("OMC (%)", record.omc.toString())
        required = p2.fdtCompactField("Required compaction (%) — optional", record.required?.toString() ?: "")
        chainFocus(sandDensity, mdd, omc, required)

        val holesCard = root.fdtCard("Holes")
        if (record.holes.isEmpty()) {
            holesCard.addView(android.widget.TextView(holesCard.context).apply {
                text = "Single point — hakuna hole rows za kuhariri.".loc()
                textSize = FdtType.CAPTION
                setTextColor(FdtColors.MUTED)
                setPadding(dp(4), dp(8), dp(4), dp(4))
            })
        } else {
            record.holes.forEachIndexed { index, h ->
                holesCard.fdtCard("Hole ${index + 1}") {
                    fun row2(l1: String, v1: String, l2: String, v2: String, n1: Boolean = true, n2: Boolean = true): Pair<EditText, EditText> {
                        val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
                        addView(row)
                        return row.fdtCompactField(l1, v1, n1) to row.fdtCompactField(l2, v2, n2)
                    }
                    val (a, b) = row2("A · soil (g)", h.A.toString(), "B · initial sand (g)", h.B.toString())
                    val (c, d) = row2("C · final sand (g)", h.C.toString(), "D · cone (g)", h.D.toString())
                    val (j, k) = row2("J · container + wet (g)", h.J.toString(), "K · container + dry (g)", h.K.toString())
                    val (l, chainage) = row2("L · container (g)", h.L.toString(), "Chainage", h.chainage, true, false)
                    val (offset, depth) = row2("Offset / Side (m)", h.offset, "Depth (mm)", h.depth, false, false)
                    val (cylinder, cylinderWeight) = row2("Cylinder No", h.cylinder, "W · cylinder weight (g)", h.cylinderWeight.toString(), false, true)
                    val (containerNo, _) = row2("Container No (I)", h.containerNo, "", "", false, false)
                    holeEdits += HoleEdit(a, b, c, d, j, k, l, chainage, offset, depth, cylinder, cylinderWeight, containerNo)
                }
            }
        }

        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        root.addView(actions)
        actions.fdtButton("Save changes", FdtButtonStyle.PRIMARY, weight = 1f) { save() }
        actions.fdtButton("Cancel", FdtButtonStyle.GHOST, weight = 1f) { finish() }

        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun num(field: EditText, label: String): Double? {
        val value = field.text.toString().trim().toDoubleOrNull()
        if (value == null) field.fdtError("$label si namba sahihi.")
        else field.fdtError(null)
        return value
    }

    private fun save() {
        val sand = num(sandDensity, "Density of sand") ?: return
        val mddValue = num(mdd, "MDD") ?: return
        val omcValue = num(omc, "OMC") ?: return
        val reqText = required.text.toString().trim()
        val reqValue = if (reqText.isEmpty()) null else reqText.toDoubleOrNull()
        if (reqValue == null && reqText.isNotEmpty()) {
            required.fdtError("Required si namba sahihi.")
            return
        }
        required.fdtError(null)
        if (sand <= 0 || mddValue <= 0) {
            Toast.makeText(this, "Density of sand na MDD lazima > 0.".loc(), Toast.LENGTH_SHORT).show()
            return
        }

        var updated = record.copy(
            project = project.text.toString().trim(),
            site = site.text.toString().trim(),
            testDate = testDate.text.toString().trim(),
            location = location.text.toString().trim(),
            road = road.text.toString().trim(),
            rfiNo = rfiNo.text.toString().trim(),
            technician = technician.text.toString().trim(),
            checkedBy = checkedBy.text.toString().trim(),
            materialSource = materialSource.text.toString().trim(),
            layer = layerEdit.text.toString().trim(),
            mddRefNo = mddRefNo.text.toString().trim(),
            sandCalibRefNo = sandCalibRefNo.text.toString().trim(),
            maxLayerThickness = maxLayerThickness.text.toString().trim(),
            depthBelowFormation = depthBelowFormation.text.toString().trim(),
            remarks = remarks.text.toString().trim(),
            sandDensity = sand,
            mdd = mddValue,
            omc = omcValue,
            required = reqValue,
        )

        if (updated.mode == "WORKSHEET" && holeEdits.isNotEmpty()) {
            val (recomputed, error) = recomputeWorksheet(
                updated, holeEdits, sand, mddValue, omcValue, updated.layer
            )
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                return
            }
            updated = recomputed!!
        }

        RecordStore(this).save(updated)
        Toast.makeText(this, "Record imehaririwa.".loc(), Toast.LENGTH_SHORT).show()
        finish()
    }

    companion object {
        const val EXTRA_ID = "record_id"
    }
}
