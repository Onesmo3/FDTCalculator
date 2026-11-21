package com.mbisecad.fdt.app

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.mbisecad.fdt.FdtRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** List of saved FDT results, with JSON backup/restore. */
class RecordsActivity : BaseActivity() {
    private lateinit var listContainer: LinearLayout
    private val store by lazy { RecordStore(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(FdtColors.BACKGROUND)
            setPadding(0, 0, 0, dp(24))
        }
        root.fdtHeader(
            "Saved results",
            "Backup, restore & PDF export",
        )

        root.fdtCard("Backup & restore") {
            val row1 = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            addView(row1)
            row1.fdtButton("Export JSON", FdtButtonStyle.SECONDARY, weight = 1f) { exportBackup() }
            row1.fdtButton("Import JSON", FdtButtonStyle.SECONDARY, weight = 1f) { pickBackup() }

            val row2 = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            addView(row2)
            row2.fdtButton("Clear all", FdtButtonStyle.DANGER, weight = 1f) {
                AlertDialog.Builder(this@RecordsActivity)
                    .setTitle("Clear all".loc())
                    .setMessage("Futa records zote zilizohifadhiwa?".loc())
                    .setPositiveButton("Futa") { _, _ ->
                        store.clear()
                        refresh()
                        toast("Records zimefutwa.".loc())
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        root.fdtCard("Reports") {
            fdtButton("PDF (all)", FdtButtonStyle.PRIMARY) { exportAllPdf() }
            addView(TextView(context).apply {
                text = "Report info na logos vinawekwa kwenye Settings → Report Setup.".loc()
                textSize = FdtType.CAPTION
                setTextColor(FdtColors.MUTED)
                setPadding(dp(4), dp(8), dp(4), dp(4))
            })
        }

        val recordsCard = root.fdtCard("Records")
        listContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        recordsCard.addView(listContainer)

        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        root.addView(nav)
        nav.fdtButton("← Back", FdtButtonStyle.GHOST, weight = 1f) { finish() }

        setContentView(ScrollView(this).apply { addView(root) })
        refresh()
    }

    private fun refresh() {
        listContainer.removeAllViews()
        val records = store.all()
        if (records.isEmpty()) {
            listContainer.addView(TextView(this).apply {
                text = "Hakuna records bado. Fanya Calculate kisha Save result.".loc()
                textSize = FdtType.BODY
                setTextColor(FdtColors.MUTED)
                setPadding(dp(12), dp(20), dp(12), dp(12))
            })
            addDiagnostics(records.size)
            return
        }

        records.forEach { record ->
            listContainer.fdtCard(null) {
                addView(TextView(context).apply {
                    text = buildString {
                        appendLine("${record.createdAt}  •  ${record.mode}")
                        appendLine("Project: ${record.project.ifBlank { "-" }} | Site: ${record.site.ifBlank { "-" }}".loc())
                        append("Layer: ${record.layer.ifBlank { "-" }}".loc())
                    }
                    textSize = FdtType.BODY
                    setTextColor(FdtColors.TEXT)
                    setLineSpacing(2f, 1.1f)
                })

                addView(TextView(context).apply {
                    text = "Status: ${record.status}".loc()
                    textSize = FdtType.CAPTION
                    setTextColor(
                        when (record.status) {
                            "Passed" -> FdtColors.GREEN
                            "Failed" -> FdtColors.RED
                            else -> FdtColors.MUTED
                        }
                    )
                    setPadding(dp(8), dp(4), dp(8), dp(4))
                    background = GradientDrawable().apply {
                        setColor(
                            when (record.status) {
                                "Passed" -> FdtColors.GREEN_LIGHT
                                "Failed" -> FdtColors.RED_LIGHT
                                else -> FdtColors.FIELD_BG
                            }
                        )
                        cornerRadius = dp(12).toFloat()
                    }
                })

                val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
                addView(row)
                row.fdtButton("View", FdtButtonStyle.SECONDARY, weight = 1f) {
                    showDetails(record)
                }
                row.fdtButton("PDF", FdtButtonStyle.PRIMARY, weight = 1f) {
                    exportPdf(listOf(record), suffix = record.id.take(8))
                }

                val row2 = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
                addView(row2)
                row2.fdtButton("Edit", FdtButtonStyle.SECONDARY, weight = 1f) {
                    startActivity(
                        Intent(this@RecordsActivity, RecordEditActivity::class.java).apply {
                            putExtra(RecordEditActivity.EXTRA_ID, record.id)
                        }
                    )
                }
                row2.fdtButton("Delete", FdtButtonStyle.DANGER, weight = 1f) {
                    store.delete(record.id)
                    refresh()
                    AlertDialog.Builder(this@RecordsActivity)
                        .setTitle("Record imefutwa".loc())
                        .setMessage("Unaweza kuirejesha kwa Undo.".loc())
                        .setPositiveButton("Undo") { _, _ ->
                            store.save(record)
                            refresh()
                            toast("Record imerejeshwa.".loc())
                        }
                        .setNegativeButton("OK", null)
                        .show()
                }
            }
        }
        addDiagnostics(records.size)
    }

    private fun addDiagnostics(count: Int) {
        val error = store.lastError
        val raw = store.raw()
        listContainer.addView(TextView(this).apply {
            text = if (error == null) {
                "diag: $count record(s) • stored ${raw.length} chars".loc()
            } else {
                "diag ERROR: $error\nraw: ${raw.take(160)}".loc()
            }
            textSize = FdtType.CAPTION
            setTextColor(FdtColors.MUTED)
            setPadding(dp(12), dp(12), dp(12), dp(4))
        })
    }

    private fun showDetails(record: FdtRecord) {
        val text = buildString {
            appendLine("Mode      : ${record.mode}".loc())
            appendLine("Date      : ${record.testDate.ifBlank { "-" }}".loc())
            appendLine("Project   : ${record.project.ifBlank { "-" }}".loc())
            appendLine("Site      : ${record.site.ifBlank { "-" }}".loc())
            appendLine("Layer     : ${record.layer.ifBlank { "-" }}".loc())
            appendLine("Sand rho  : ${record.sandDensity}".loc())
            appendLine("MDD       : ${record.mdd}".loc())
            appendLine("OMC       : ${record.omc}".loc())
            appendLine("Required  : ${record.required?.let { String.format(Locale.US, "%.2f", it) } ?: "-"}".loc())
            appendLine("Status    : ${record.status}".loc())
            appendLine()
            if (record.mode == "WORKSHEET") {
                appendLine("n         : ${record.holes.size}".loc())
                appendLine("Xn        : ${record.xn?.let { String.format(Locale.US, "%.2f", it) } ?: "-"}".loc())
                appendLine("Xo        : ${record.xo?.let { String.format(Locale.US, "%.2f", it) } ?: "-"}".loc())
                appendLine("Xs        : ${record.xs?.let { String.format(Locale.US, "%.2f", it) } ?: "-"}".loc())
                appendLine("Xmin      : ${record.xmin?.let { String.format(Locale.US, "%.2f", it) } ?: "-"}".loc())
            } else {
                appendLine("Compaction: ${String.format(Locale.US, "%.2f", record.compaction)} %".loc())
            }
            if (record.holes.isNotEmpty()) {
                appendLine()
                record.holes.forEachIndexed { i, h ->
                    appendLine("Hole ${i + 1}: S=${h.compaction} %  U=${h.tolerance}".loc())
                }
            }
        }
        AlertDialog.Builder(this)
            .setTitle("FDT record".loc())
            .setMessage(text)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun exportBackup() {
        val json = store.exportJson()
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "FDT-results-backup.json")
            put(MediaStore.Downloads.MIME_TYPE, "application/json")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }
        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        if (uri == null) {
            toast("Imeshindikana kufungua Downloads.".loc())
            return
        }
        contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray(Charsets.UTF_8)) }
        toast("Backup: Download/FDT-results-backup.json".loc())
    }

    private fun pickBackup() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        startActivityForResult(intent, REQUEST_IMPORT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        val uri: Uri = data?.data ?: return
        when (requestCode) {
            REQUEST_IMPORT -> try {
                val text = contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (text == null) {
                    toast("Faili halisomeki.".loc())
                    return
                }
                val added = store.importJson(text)
                refresh()
                toast("Importd records: $added".loc())
            } catch (e: Exception) {
                toast("Faili si sahihi: ${e.message}".loc())
            }
        }
    }

    private fun pdfUri(name: String): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, name)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }
        return contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
    }

    private fun exportPdf(records: List<FdtRecord>, suffix: String) {
        if (records.isEmpty()) return
        val mode = records.first().mode
        val locations = records.map { it.location }.distinct()
        val layers = records.map { it.layer }.distinct()
        val location = if (locations.size == 1) locations.first() else "Multiple"
        val layer = if (layers.size == 1) layers.first() else "Multiple"
        val name = fdtPdfName(mode, location, layer, suffix)
        try {
            val uri = pdfUri(name) ?: throw IllegalStateException("Downloads haifunguki")
            val stream = contentResolver.openOutputStream(uri) ?: throw IllegalStateException("File haiandikiki")
            val pdf = FdtPdf(ProjectStore(this).load(), stream)
            if (mode == "WORKSHEET") pdf.writeWorksheet(records) else pdf.writeSingle(records)
            toast("PDF: Download/$name".loc())
        } catch (e: Exception) {
            toast("PDF imeshindikana: ${e.message}".loc())
        }
    }

    private fun exportAllPdf() {
        val all = store.all()
        val singles = all.filter { it.mode == "SINGLE" }
        val sheets = all.filter { it.mode == "WORKSHEET" }
        if (singles.isEmpty() && sheets.isEmpty()) {
            toast("Hakuna records za kutengeneza PDF.".loc())
            return
        }
        if (singles.isNotEmpty()) exportPdf(singles, "single-all")
        if (sheets.isNotEmpty()) exportPdf(sheets, "worksheet-all")
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_IMPORT = 4321
    }
}
