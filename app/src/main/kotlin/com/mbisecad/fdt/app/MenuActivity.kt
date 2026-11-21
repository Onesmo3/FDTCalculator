package com.mbisecad.fdt.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.mbisecad.fdt.defaultLayers
import com.mbisecad.fdt.runSelfTest

/** Launcher: single entry point for tests, data, settings and information. */
class MenuActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(FdtColors.BACKGROUND)
            setPadding(0, 0, 0, dp(24))
        }
        root.fdtHeader(
            "FDT Calculator",
            "Sand Cone Method · menu kuu",
        )

        root.fdtCard("Tests") {
            val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            addView(row)
            row.fdtButton("Single point", FdtButtonStyle.PRIMARY, weight = 1f) {
                open(MainActivity::class.java)
            }
            row.fdtButton("Worksheet", FdtButtonStyle.SECONDARY, weight = 1f) {
                open(WorksheetActivity::class.java)
            }
            addView(TextView(context).apply {
                text = "Single point: hole moja. Worksheet: holes nyingi → Xn/Xo/Xs/Xmin.".loc()
                textSize = FdtType.CAPTION
                setTextColor(FdtColors.MUTED)
                setPadding(dp(4), dp(8), dp(4), dp(4))
            })
        }

        root.fdtCard("Data") {
            val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            addView(row)
            row.fdtButton("Saved results", FdtButtonStyle.SECONDARY, weight = 1f) {
                open(RecordsActivity::class.java)
            }
            row.fdtButton("Settings", FdtButtonStyle.SECONDARY, weight = 1f) {
                open(SettingsActivity::class.java)
            }
            addView(TextView(context).apply {
                text = "History, backup/restore, report setup na cylinder details ziko kwenye Settings.".loc()
                textSize = FdtType.CAPTION
                setTextColor(FdtColors.MUTED)
                setPadding(dp(4), dp(8), dp(4), dp(4))
            })
        }

        root.fdtCard("Diagnostics") {
            fdtButton("Engine self-test", FdtButtonStyle.GHOST) { showSelfTest() }
        }

        root.fdtCard("Legal & About") {
            val r1 = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            addView(r1)
            r1.fdtButton("Privacy", FdtButtonStyle.GHOST, weight = 1f) { openInfo(LegalText.PRIVACY) }
            r1.fdtButton("Disclaimer", FdtButtonStyle.GHOST, weight = 1f) { openInfo(LegalText.DISCLAIMER) }

            val r2 = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            addView(r2)
            r2.fdtButton("Terms", FdtButtonStyle.GHOST, weight = 1f) { openInfo(LegalText.TERMS) }
            r2.fdtButton("Copyrights", FdtButtonStyle.GHOST, weight = 1f) { openInfo(LegalText.COPYRIGHT) }

            val r3 = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            addView(r3)
            r3.fdtButton("Contact", FdtButtonStyle.GHOST, weight = 1f) { openInfo(LegalText.CONTACT) }
            r3.fdtButton("About", FdtButtonStyle.GHOST, weight = 1f) { openInfo(LegalText.ABOUT) }
        }

        root.addView(TextView(this).apply {
            text = "FDT Calculator · Kotlin port · v1.0".loc()
            textSize = FdtType.CAPTION
            setTextColor(FdtColors.MUTED)
            setPadding(dp(12), dp(16), dp(12), dp(4))
        })

        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun open(activity: Class<*>) {
        startActivity(Intent(this, activity))
    }

    private fun openInfo(type: String) {
        startActivity(
            Intent(this, StaticInfoActivity::class.java).apply {
                putExtra(StaticInfoActivity.EXTRA_TYPE, type)
            }
        )
    }

    private fun showSelfTest() {
        val r = runSelfTest(defaultLayers())
        val passed = r.rows.count { it.pass }
        val message = buildString {
            appendLine(if (r.allPass) "RESULT: PASS ✅".loc() else "RESULT: FAIL ❌".loc())
            appendLine("Checks: $passed / ${r.rows.size}".loc())
            appendLine()
            appendLine("Xn ${r.Xn}  (pass: ${r.xnPass})".loc())
            appendLine("Xo ${r.Xo}  (pass: ${r.xoPass})".loc())
            if (r.hasLayerCheck) {
                appendLine("Xs ${r.xsActual} (${r.xsPass}) · Xmin ${r.xminActual} (${r.xminPass})".loc())
            }
            if (!r.allPass) {
                appendLine()
                appendLine("Failures:".loc())
                r.rows.filter { !it.pass }.take(10).forEach {
                    appendLine("Hole ${it.hole} · ${it.label}: ${it.actual} ≠ ${it.expected}".loc())
                }
            }
        }
        AlertDialog.Builder(this)
            .setTitle("Engine self-test".loc())
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
