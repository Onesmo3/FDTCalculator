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
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.mbisecad.fdt.Cylinder

class SettingsActivity : BaseActivity() {
    internal lateinit var cylindersContainer: LinearLayout
    internal lateinit var newNo: EditText
    internal lateinit var newWeight: EditText
    internal lateinit var newCone: EditText

    internal val projectStore by lazy { ProjectStore(this) }
    internal val cylinderStore by lazy { CylinderStore(this) }
    internal val recordStore by lazy { RecordStore(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(FdtColors.BACKGROUND)
            setPadding(0, 0, 0, dp(24))
        }
        root.fdtHeader("Settings", "Report Setup · Cylinder details · Backup")

        val p = projectStore.load()
        root.fdtCard("Language") {
            val selected = L10n.current()
            val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            addView(row)
            row.fdtButton(
                "English",
                if (selected == L10n.EN) FdtButtonStyle.PRIMARY else FdtButtonStyle.GHOST,
                weight = 1f,
            ) {
                L10n.set(this@SettingsActivity, L10n.EN)
                recreate()
            }
            row.fdtButton(
                "Swahili",
                if (selected == L10n.SW) FdtButtonStyle.PRIMARY else FdtButtonStyle.GHOST,
                weight = 1f,
            ) {
                L10n.set(this@SettingsActivity, L10n.SW)
                recreate()
            }
        }

        root.fdtCard("Report Setup (PDF letterhead)") {
            val projectName = fdtField("Project / Contract name", p.name, numeric = false)
            val contractor = fdtField("Contractor's Name", p.company, numeric = false)
            val client = fdtField("Client Name (optional)", p.clientName, numeric = false)
            val consultant = fdtField("Consultant Name", p.consultant, numeric = false)
            val logos = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            addView(logos)
            logos.fdtButton("Co. logo", FdtButtonStyle.GHOST, weight = 1f) { pickLogo(ProjectStore.COMPANY) }
            logos.fdtButton("Client logo", FdtButtonStyle.GHOST, weight = 1f) { pickLogo(ProjectStore.CLIENT) }
            logos.fdtButton("Con. logo", FdtButtonStyle.GHOST, weight = 1f) { pickLogo(ProjectStore.CONSULTANT) }
            addView(TextView(context).apply {
                text = "Client fields ni hiari. Zinatumika kwenye PDF ikiwa zimejazwa.".loc()
                textSize = FdtType.CAPTION
                setTextColor(FdtColors.MUTED)
                setPadding(dp(4), dp(6), dp(4), dp(4))
            })
            fdtButton("Save report info", FdtButtonStyle.PRIMARY) {
                projectStore.save(
                    projectName.text.toString().trim(),
                    contractor.text.toString().trim(),
                    client.text.toString().trim(),
                    consultant.text.toString().trim(),
                )
                toast("Project info imehifadhiwa.".loc())
            }
        }

        root.fdtCard("Cylinder details") {
            cylindersContainer = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
            addView(cylindersContainer)
            newNo = fdtField("Cylinder number", hint = "mf. 5")
            newWeight = fdtField("Weight of cylinder (g)", hint = "mf. 1604")
            newCone = fdtField("Sand in cone (g)", hint = "mf. 640")
            chainFocus(newNo, newWeight, newCone)
            val actions = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            addView(actions)
            actions.fdtButton("Add cylinder", FdtButtonStyle.SECONDARY, weight = 1f) { addCylinder() }
            actions.fdtButton("Restore defaults", FdtButtonStyle.GHOST, weight = 1f) {
                AlertDialog.Builder(this@SettingsActivity)
                    .setTitle("Restore defaults".loc())
                    .setMessage("Rudisha cylinders 4 za awali?".loc())
                    .setPositiveButton("Restore") { _, _ ->
                        cylinderStore.reset()
                        refreshCylinders()
                        toast("Cylinder defaults zimerudishwa.".loc())
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            refreshCylinders()
        }

        root.fdtCard("Backup & Restore") {
            val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            addView(row)
            row.fdtButton("Export data", FdtButtonStyle.SECONDARY, weight = 1f) { exportBackup() }
            row.fdtButton("Import data", FdtButtonStyle.SECONDARY, weight = 1f) { pickBackup() }
            fdtButton("Clear all results", FdtButtonStyle.DANGER) {
                AlertDialog.Builder(this@SettingsActivity)
                    .setTitle("Clear all".loc())
                    .setMessage("Futa matokeo yote yaliyohifadhiwa?".loc())
                    .setPositiveButton("Futa") { _, _ ->
                        recordStore.clear()
                        toast("Records zimefutwa.".loc())
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        root.addView(nav)
        nav.fdtButton("← Back to menu", FdtButtonStyle.GHOST, weight = 1f) { finish() }
        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        val uri: Uri = data?.data ?: return
        if (requestCode == REQ_IMPORT) handleImport(uri) else handleLogo(requestCode, uri)
    }
}
