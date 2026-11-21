package com.mbisecad.fdt.app

import android.app.Activity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** Simple read-only screen for Privacy / Disclaimer / Terms / About items. */
class StaticInfoActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val type = intent.getStringExtra(EXTRA_TYPE) ?: LegalText.ABOUT
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(FdtColors.BACKGROUND)
            setPadding(0, 0, 0, dp(24))
        }
        root.fdtHeader(LegalText.title(type), "FDT Calculator · information")

        root.fdtCard(LegalText.title(type)) {
            addView(TextView(context).apply {
                text = LegalText.body(type)
                tag = "fdt_no_translate"
                textSize = FdtType.BODY
                setTextColor(FdtColors.TEXT)
                setLineSpacing(3f, 1.2f)
                setPadding(dp(4), dp(4), dp(4), dp(8))
            })
        }

        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        root.addView(nav)
        nav.fdtButton("← Back", FdtButtonStyle.GHOST, weight = 1f) { finish() }

        setContentView(ScrollView(this).apply { addView(root) })
    }

    companion object {
        const val EXTRA_TYPE = "type"
    }
}
