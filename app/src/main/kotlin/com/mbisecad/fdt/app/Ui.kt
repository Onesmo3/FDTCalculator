package com.mbisecad.fdt.app

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView

/**
 * Small in-app design system shared by every screen.
 * Style: Engineering green — derived from the PDF letterhead (kHeadFill #20261F).
 */
object FdtColors {
    const val DARK = 0xFF20261F.toInt()
    const val GREEN = 0xFF2E7D32.toInt()
    const val GREEN_LIGHT = 0xFFE8F1E8.toInt()
    const val AMBER = 0xFFF5A623.toInt()
    const val RED = 0xFFC62828.toInt()
    const val RED_LIGHT = 0xFFFFEBEE.toInt()
    const val BACKGROUND = 0xFFF7F7F3.toInt()
    const val SURFACE = 0xFFFFFFFF.toInt()
    const val FIELD_BG = 0xFFFFFFFC.toInt()
    const val BORDER = 0xFFD8D8D0.toInt()
    const val TEXT = 0xFF141414.toInt()
    const val MUTED = 0xFF6B6B63.toInt()
    const val ON_DARK = 0xFFF5F4EC.toInt()
    const val ON_DARK_MUTED = 0xFFC7CDC4.toInt()
}

object FdtType {
    const val TITLE = 20f
    const val SECTION = 16f
    const val BODY = 14f
    const val LABEL = 13f
    const val CAPTION = 12f
}

fun Context.dp(value: Int): Int =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics
    ).toInt()

private fun shape(fill: Int, stroke: Int?, radius: Int, strokeWidth: Int = 1): GradientDrawable =
    GradientDrawable().apply {
        setColor(fill)
        cornerRadius = radius.toFloat()
        if (stroke != null) setStroke(strokeWidth, stroke)
    }

enum class FdtButtonStyle { PRIMARY, SECONDARY, GHOST, DANGER }

/** Dark app bar with brand + subtitle. */
fun LinearLayout.fdtHeader(title: String, subtitle: String): LinearLayout {
    val ctx = context
    addView(LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        background = shape(FdtColors.DARK, null, 0)
        setPadding(ctx.dp(20), ctx.dp(18), ctx.dp(20), ctx.dp(18))
        addView(TextView(ctx).apply {
            text = title.loc()
            textSize = FdtType.TITLE
            setTextColor(FdtColors.ON_DARK)
            typeface = Typeface.DEFAULT_BOLD
        })
        addView(TextView(ctx).apply {
            text = subtitle.loc()
            textSize = FdtType.CAPTION
            setTextColor(FdtColors.ON_DARK_MUTED)
            setPadding(0, ctx.dp(2), 0, 0)
        })
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
    })
    return this
}

/** White elevated card with optional section title. */
fun LinearLayout.fdtCard(title: String? = null, block: LinearLayout.() -> Unit = {}): LinearLayout {
    val ctx = context
    val card = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        background = shape(FdtColors.SURFACE, FdtColors.BORDER, ctx.dp(12))
        setPadding(ctx.dp(16), ctx.dp(14), ctx.dp(16), ctx.dp(16))
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = ctx.dp(12) }
        if (title != null) {
            addView(TextView(ctx).apply {
                text = title.loc()
                textSize = FdtType.SECTION
                setTextColor(FdtColors.DARK)
                typeface = Typeface.DEFAULT_BOLD
                setPadding(0, 0, 0, ctx.dp(10))
            })
        }
        block()
    }
    addView(card)
    return card
}

fun LinearLayout.fdtLabel(text: String): TextView = TextView(context).apply {
    this.text = text.loc()
    textSize = FdtType.LABEL
    setTextColor(FdtColors.MUTED)
    setPadding(0, context.dp(8), 0, context.dp(4))
    layoutParams = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )
}.also { addView(it) }

/** Styles an EditText that will be placed inside an inline row. */
fun EditText.fdtInput(): EditText = apply {
    background = shape(FdtColors.FIELD_BG, FdtColors.BORDER, context.dp(8))
    setPadding(context.dp(10), context.dp(8), context.dp(10), context.dp(8))
    setTextSize(TypedValue.COMPLEX_UNIT_SP, FdtType.BODY)
    setTextColor(FdtColors.TEXT)
    setHintTextColor(FdtColors.MUTED)
    minimumHeight = context.dp(44)
    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
}

/** Inline validation: red border + Android error bubble, or restore normal field. */
fun EditText.fdtError(message: String?) {
    if (message == null) {
        error = null
        background = shape(FdtColors.FIELD_BG, FdtColors.BORDER, context.dp(8))
    } else {
        error = message.loc()
        background = shape(FdtColors.RED_LIGHT, FdtColors.RED, context.dp(8), 2)
    }
}

/** Wires keyboard Next/Done and explicit focus order across generated fields. */
fun chainFocus(vararg fields: EditText) {
    fields.forEachIndexed { index, field ->
        if (field.id == android.view.View.NO_ID) {
            field.id = android.view.View.generateViewId()
        }
        if (index < fields.lastIndex) {
            val next = fields[index + 1]
            if (next.id == android.view.View.NO_ID) next.id = android.view.View.generateViewId()
            field.setNextFocusForwardId(next.id)
            field.setNextFocusDownId(next.id)
            field.imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_NEXT
        } else {
            field.imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_DONE
        }
    }
}

fun LinearLayout.fdtField(
    label: String,
    initial: String = "",
    numeric: Boolean = true,
    hint: String? = null,
): EditText {
    fdtLabel(label)
    return EditText(context).apply {
        if (numeric) {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        setText(initial)
        if (hint != null) setHint(hint.loc())
        background = shape(FdtColors.FIELD_BG, FdtColors.BORDER, context.dp(8))
        setPadding(context.dp(12), context.dp(10), context.dp(12), context.dp(10))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, FdtType.BODY)
        setTextColor(FdtColors.TEXT)
        setHintTextColor(FdtColors.MUTED)
        minimumHeight = context.dp(44)
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = context.dp(2) }
        addView(this)
    }
}

/** Grid cell: small caption above a compact input; use two per row. */
fun LinearLayout.fdtCompactField(
    label: String,
    initial: String = "",
    numeric: Boolean = true,
    hint: String? = null,
): EditText {
    val cell = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
            leftMargin = context.dp(4)
            rightMargin = context.dp(4)
            bottomMargin = context.dp(8)
        }
    }
    addView(cell)
    cell.addView(android.widget.TextView(context).apply {
        text = label.loc()
        textSize = FdtType.CAPTION
        setTextColor(FdtColors.MUTED)
        maxLines = 1
        ellipsize = android.text.TextUtils.TruncateAt.END
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = context.dp(2) }
    })
    return EditText(context).apply {
        if (numeric) {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        setText(initial)
        if (hint != null) setHint(hint.loc())
        background = shape(FdtColors.FIELD_BG, FdtColors.BORDER, context.dp(8))
        setPadding(context.dp(10), context.dp(8), context.dp(10), context.dp(8))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, FdtType.BODY)
        setTextColor(FdtColors.TEXT)
        setHintTextColor(FdtColors.MUTED)
        minimumHeight = context.dp(42)
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        cell.addView(this)
    }
}

/** Structured result panel: label/value rows + a coloured status chip. */
data class FdtSummaryPanel(
    val view: LinearLayout,
    private val rowsContainer: LinearLayout,
    private val statusView: android.widget.TextView,
) {
    fun setRows(items: List<Pair<String, String>>) {
        rowsContainer.removeAllViews()
        items.forEach { (label, value) ->
            val row = LinearLayout(rowsContainer.context).apply {
                orientation = LinearLayout.HORIZONTAL
                background = shape(FdtColors.SURFACE, FdtColors.BORDER, context.dp(6))
                setPadding(context.dp(10), context.dp(8), context.dp(10), context.dp(8))
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = context.dp(4) }
            }
            row.addView(android.widget.TextView(row.context).apply {
                text = label.loc()
                textSize = FdtType.CAPTION
                setTextColor(FdtColors.MUTED)
                layoutParams = LinearLayout.LayoutParams(context.dp(132), ViewGroup.LayoutParams.WRAP_CONTENT)
            })
            row.addView(android.widget.TextView(row.context).apply {
                text = value.loc()
                textSize = FdtType.BODY
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setTextColor(FdtColors.TEXT)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })
            rowsContainer.addView(row)
        }
    }

    fun setStatus(value: String) {
        val upper = value.uppercase()
        val passed = upper.startsWith("PASS")
        val failed = upper.startsWith("FAIL") || upper.startsWith("ERROR")
        statusView.visibility = android.view.View.VISIBLE
        statusView.text = "STATUS".loc() + "   " + value.loc()
        statusView.setTextColor(
            when {
                passed -> FdtColors.GREEN
                failed -> FdtColors.RED
                else -> FdtColors.DARK
            }
        )
        statusView.background = shape(
            when {
                passed -> FdtColors.GREEN_LIGHT
                failed -> FdtColors.RED_LIGHT
                else -> FdtColors.FIELD_BG
            },
            FdtColors.BORDER,
            statusView.context.dp(8),
        )
    }

    fun error(message: String) {
        setRows(listOf("Message" to message))
        setStatus("Error")
    }
}

fun LinearLayout.fdtSummaryPanel(): FdtSummaryPanel {
    val panel = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        background = shape(FdtColors.GREEN_LIGHT, FdtColors.BORDER, context.dp(10))
        setPadding(context.dp(12), context.dp(12), context.dp(12), context.dp(12))
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = context.dp(8) }
    }
    addView(panel)
    val rows = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    panel.addView(rows)
    val status = android.widget.TextView(context).apply {
        textSize = FdtType.LABEL
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        setPadding(context.dp(10), context.dp(8), context.dp(10), context.dp(8))
        visibility = android.view.View.GONE
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = context.dp(6) }
    }
    panel.addView(status)
    return FdtSummaryPanel(panel, rows, status)
}

fun <T> LinearLayout.fdtSpinner(
    items: List<T>,
    label: String,
    selectedIndex: Int = 0,
    labelOf: (T) -> String,
): Spinner {
    fdtLabel(label)
    val spinner = Spinner(context).apply {
        adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            items.map { labelOf(it).loc() },
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        setSelection(selectedIndex)
        setBackgroundColor(FdtColors.FIELD_BG)
        setPadding(context.dp(10), context.dp(8), context.dp(10), context.dp(8))
        minimumHeight = context.dp(44)
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = context.dp(4) }
    }
    addView(spinner)
    return spinner
}

fun LinearLayout.fdtButton(
    text: String,
    style: FdtButtonStyle = FdtButtonStyle.PRIMARY,
    weight: Float? = null,
    textSizeSp: Float = FdtType.BODY,
    minHeightDp: Int = 48,
    padHDp: Int = 12,
    padVDp: Int = 8,
    onClick: () -> Unit,
): Button {
    val ctx = context
    return Button(ctx).apply {
        this.text = text.loc()
        isAllCaps = false
        typeface = Typeface.DEFAULT_BOLD
        textSize = textSizeSp
        minimumHeight = ctx.dp(minHeightDp)
        setPadding(ctx.dp(padHDp), ctx.dp(padVDp), ctx.dp(padHDp), ctx.dp(padVDp))
        when (style) {
            FdtButtonStyle.PRIMARY -> {
                background = shape(FdtColors.GREEN, null, ctx.dp(8))
                setTextColor(FdtColors.SURFACE)
            }
            FdtButtonStyle.SECONDARY -> {
                background = shape(FdtColors.SURFACE, FdtColors.GREEN, ctx.dp(8))
                setTextColor(FdtColors.GREEN)
            }
            FdtButtonStyle.GHOST -> {
                background = shape(FdtColors.FIELD_BG, FdtColors.BORDER, ctx.dp(8))
                setTextColor(FdtColors.MUTED)
            }
            FdtButtonStyle.DANGER -> {
                background = shape(FdtColors.RED_LIGHT, FdtColors.RED, ctx.dp(8))
                setTextColor(FdtColors.RED)
            }
        }
        layoutParams = LinearLayout.LayoutParams(
            if (weight == null) ViewGroup.LayoutParams.WRAP_CONTENT else 0,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            weight ?: 0f,
        ).apply {
            if (weight == null) {
                topMargin = ctx.dp(8)
            } else {
                leftMargin = ctx.dp(4)
                rightMargin = ctx.dp(4)
                topMargin = ctx.dp(10)
            }
        }
        setOnClickListener { onClick() }
        addView(this)
    }
}

fun LinearLayout.fdtResultView(): TextView = TextView(context).apply {
    textSize = FdtType.BODY
    setTextColor(FdtColors.TEXT)
    setLineSpacing(2f, 1.15f)
    setPadding(context.dp(12), context.dp(12), context.dp(12), context.dp(12))
    background = shape(FdtColors.GREEN_LIGHT, FdtColors.GREEN, context.dp(8))
    layoutParams = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )
}.also { addView(it) }
