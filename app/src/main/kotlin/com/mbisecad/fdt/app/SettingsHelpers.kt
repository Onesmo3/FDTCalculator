package com.mbisecad.fdt.app

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.mbisecad.fdt.Cylinder

internal const val REQ_IMPORT = 5321
private const val REQ_LOGO_COMPANY = 5322
private const val REQ_LOGO_CLIENT = 5323
private const val REQ_LOGO_CONSULTANT = 5324

internal fun SettingsActivity.refreshCylinders() {
    cylindersContainer.removeAllViews()
    val cylinders = cylinderStore.all()
    cylindersContainer.addView(TextView(this).apply {
        text = "Cylinders zilizopo (${cylinders.size}) — zinatumika kwenye single point na worksheet.".loc()
        textSize = FdtType.CAPTION
        setTextColor(FdtColors.MUTED)
        setPadding(dp(4), dp(2), dp(4), dp(6))
    })
    cylinders.forEach { c ->
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(TextView(this).apply {
            text = "Cyl ${c.n} · ${c.weight.toInt()} g · cone ${c.sandInCone.toInt()} g".loc()
            textSize = FdtType.BODY
            setTextColor(FdtColors.TEXT)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f)
        })
        row.fdtButton("Remove", FdtButtonStyle.DANGER, weight = 1f) {
            cylinderStore.remove(c.n)
            refreshCylinders()
            toast("Cyl ${c.n} imeondolewa.".loc())
        }
        cylindersContainer.addView(row)
    }
}

internal fun SettingsActivity.addCylinder() {
    val n = newNo.text.toString().trim().toIntOrNull()
    val weight = newWeight.text.toString().trim().toDoubleOrNull()
    val cone = newCone.text.toString().trim().toDoubleOrNull()
    newNo.fdtError(if (n == null || n <= 0) "Andika namba sahihi." else null)
    newWeight.fdtError(if (weight == null || weight <= 0) "Andika uzito sahihi." else null)
    newCone.fdtError(if (cone == null || cone <= 0) "Andika sand-in-cone sahihi." else null)
    if (n == null || weight == null || cone == null || n <= 0 || weight <= 0 || cone <= 0) {
        toast("Jaza namba, uzito na sand-in-cone sahihi.".loc())
        return
    }
    if (!cylinderStore.add(Cylinder(n, weight, cone))) {
        toast("Cylinder No $n ipo tayari.".loc())
        return
    }
    newNo.text?.clear()
    newWeight.text?.clear()
    newCone.text?.clear()
    listOf(newNo, newWeight, newCone).forEach { it.fdtError(null) }
    refreshCylinders()
    toast("Cylinder $n imeongezwa.".loc())
}

internal fun SettingsActivity.pickLogo(which: String) {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "image/*"
    }
    startActivityForResult(intent, when (which) {
        ProjectStore.COMPANY -> REQ_LOGO_COMPANY
        ProjectStore.CLIENT -> REQ_LOGO_CLIENT
        else -> REQ_LOGO_CONSULTANT
    })
}

internal fun SettingsActivity.pickBackup() {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "application/json"
    }
    startActivityForResult(intent, REQ_IMPORT)
}

internal fun SettingsActivity.handleImport(uri: Uri) {
    try {
        val text = contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        if (text == null) {
            toast("Faili halisomeki.".loc())
            return
        }
        toast("Importd records: ${recordStore.importJson(text)}".loc())
    } catch (e: Exception) {
        toast("Faili si sahihi: ${e.message}".loc())
    }
}

internal fun SettingsActivity.handleLogo(requestCode: Int, uri: Uri) {
    val which = when (requestCode) {
        REQ_LOGO_COMPANY -> ProjectStore.COMPANY
        REQ_LOGO_CLIENT -> ProjectStore.CLIENT
        else -> ProjectStore.CONSULTANT
    }
    toast(if (projectStore.setLogo(which, uri)) "Logo imehifadhiwa.".loc() else "Logo imeshindikana.".loc())
}

internal fun SettingsActivity.exportBackup() {
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
    contentResolver.openOutputStream(uri)?.use {
        it.write(recordStore.exportJson().toByteArray(Charsets.UTF_8))
    }
    toast("Backup: Download/FDT-results-backup.json".loc())
}

internal fun SettingsActivity.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
