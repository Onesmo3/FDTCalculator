package com.mbisecad.fdt.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File

/** Project info used by the PDF letterhead, ported from fdtpdf::Project. */
data class PdfProject(
    val name: String = "",
    val company: String = "",
    val clientName: String = "",
    val consultant: String = "",
    val companyLogoPath: String? = null,
    val clientLogoPath: String? = null,
    val consultantLogoPath: String? = null,
) {
    val companyLogo: Bitmap? get() = companyLogoPath?.let { BitmapFactory.decodeFile(it) }
    val clientLogo: Bitmap? get() = clientLogoPath?.let { BitmapFactory.decodeFile(it) }
    val consultantLogo: Bitmap? get() = consultantLogoPath?.let { BitmapFactory.decodeFile(it) }
}

class ProjectStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("fdt_project", Context.MODE_PRIVATE)

    fun load(): PdfProject = PdfProject(
        name = prefs.getString(KEY_NAME, "") ?: "",
        company = prefs.getString(KEY_COMPANY, "") ?: "",
        clientName = prefs.getString(KEY_CLIENT, "") ?: "",
        consultant = prefs.getString(KEY_CONSULTANT, "") ?: "",
        companyLogoPath = existingLogo(COMPANY),
        clientLogoPath = existingLogo(CLIENT),
        consultantLogoPath = existingLogo(CONSULTANT),
    )

    fun save(name: String, company: String, clientName: String, consultant: String) {
        prefs.edit()
            .putString(KEY_NAME, name)
            .putString(KEY_COMPANY, company)
            .putString(KEY_CLIENT, clientName)
            .putString(KEY_CONSULTANT, consultant)
            .commit()
    }

    /** Copies the chosen image into app-internal storage so it survives restores. */
    fun setLogo(which: String, uri: Uri): Boolean {
        val dir = File(context.filesDir, "logos").apply { mkdirs() }
        val file = File(dir, "$which.png")
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            } ?: return false
            true
        } catch (e: Exception) {
            false
        }
    }

    fun removeLogo(which: String) {
        File(File(context.filesDir, "logos"), "$which.png").delete()
    }

    private fun existingLogo(which: String): String? {
        val file = File(File(context.filesDir, "logos"), "$which.png")
        return if (file.exists()) file.absolutePath else null
    }

    companion object {
        const val COMPANY = "company"
        const val CLIENT = "client"
        const val CONSULTANT = "consultant"
        private const val KEY_NAME = "name"
        private const val KEY_COMPANY = "company"
        private const val KEY_CLIENT = "client"
        private const val KEY_CONSULTANT = "consultant"
    }
}
