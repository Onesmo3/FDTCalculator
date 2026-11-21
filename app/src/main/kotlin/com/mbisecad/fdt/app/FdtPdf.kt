package com.mbisecad.fdt.app

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.mbisecad.fdt.FdtHoleData
import com.mbisecad.fdt.FdtRecord
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/**
 * PDF exporter ported 1:1 in layout from Qt fdtpdf.cpp (QPdfWriter + QPainter).
 * Uses Android PdfDocument + Canvas; 1 unit = 1 pt (A4 = 595.28 x 841.89).
 */
class FdtPdf(
    private val project: PdfProject,
    output: OutputStream,
) {
    private val document = PdfDocument()
    private val out = output
    private var page: PdfDocument.Page? = null
    private var canvas: Canvas? = null
    private var pageNo = 0
    var y = 0.0
        private set

    private val paintCache = mutableMapOf<String, Paint>()

    private companion object {
        const val PAGE_W = 595.28
        const val PAGE_H = 841.89
        const val TOP = 36.0
        const val BOTTOM = 36.0
        const val INK = 0xFF141414.toInt()
        const val HEAD_FILL = 0xFF20261F.toInt()
        const val GRID = 0xFFB4B4B4.toInt()
        const val SIG_FILL = 0xFFF5F4EC.toInt()
        const val SECTION_FILL = 0xFFE6E6E0.toInt()
        const val OK_GREEN = 0xFF2E7D32.toInt()
        const val FAIL_RED = 0xFFC62828.toInt()
        const val GRAY = 0xFF787878.toInt()
        const val WHITE = 0xFFFFFFFF.toInt()
    }

    // ---------- msingi ----------

    private fun newPage() {
        page?.let { document.finishPage(it) }
        pageNo++
        page = document.startPage(
            PdfDocument.PageInfo.Builder(PAGE_W.toInt(), PAGE_H.toInt(), pageNo).create()
        )
        canvas = page!!.canvas
        y = TOP
    }

    private fun paint(size: Float, bold: Boolean, italic: Boolean, color: Int): Paint =
        paintCache.getOrPut("$size|$bold|$italic|$color") {
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                textSize = size
                typeface = when {
                    bold && italic -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                    bold -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    italic -> Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    else -> Typeface.DEFAULT
                }
                this.color = color
            }
        }

    private enum class Align { LEFT, CENTER, RIGHT }

    private fun textWidth(s: String, p: Paint): Float = p.measureText(s)

    private fun text(
        s: String,
        x: Double,
        baseline: Double,
        size: Float = 8f,
        bold: Boolean = false,
        italic: Boolean = false,
        align: Align = Align.LEFT,
        color: Int = INK,
    ) {
        val p = paint(size, bold, italic, color)
        val px = when (align) {
            Align.LEFT -> x
            Align.CENTER -> x - textWidth(s, p) / 2
            Align.RIGHT -> x - textWidth(s, p)
        }.toFloat()
        canvas!!.drawText(s, px, baseline.toFloat(), p)
    }

    private fun hline(x1: Double, x2: Double, yy: Double, width: Float, color: Int = INK) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            strokeWidth = width
        }
        canvas!!.drawLine(x1.toFloat(), yy.toFloat(), x2.toFloat(), yy.toFloat(), p)
    }

    private fun vline(x: Double, y1: Double, y2: Double, width: Float) {
        val p = Paint().apply {
            color = GRID
            strokeWidth = width
        }
        canvas!!.drawLine(x.toFloat(), y1.toFloat(), x.toFloat(), y2.toFloat(), p)
    }

    private fun wrap(s: String, p: Paint, maxW: Double): List<String> {
        if (s.isEmpty()) return listOf("")
        val words = s.split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (words.isEmpty()) return listOf("")
        val result = mutableListOf<String>()
        var line = ""
        for (word in words) {
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (textWidth(candidate, p) <= maxW || line.isEmpty()) {
                line = candidate
            } else {
                result += line
                line = word
            }
        }
        if (line.isNotEmpty()) result += line
        return result
    }

    // ---------- meza ----------

    private data class Cell(
        val text: String,
        val bold: Boolean = false,
        val italic: Boolean = false,
        val align: Align = Align.LEFT,
        val colSpan: Int = 1,
        val rowSpan: Int = 1,
        val fill: Int? = null,
        val color: Int = INK,
    )

    private fun cell(
        text: String,
        bold: Boolean = false,
        align: Align = Align.LEFT,
        colSpan: Int = 1,
        fill: Int? = null,
        color: Int = INK,
    ) = Cell(text, bold, false, align, colSpan, 1, fill, color)

    private data class TableStyle(
        val fontSize: Float = 8f,
        val pad: Float = 3f,
        val lineWidth: Float = 0.5f,
        val repeatRows: Int = 0,
        val keepTogether: Boolean = false,
    )

    private data class Placed(val cell: Cell, val col: Int, val width: Double, val lines: List<String>)

    private fun rowHeight(placed: List<Placed>, style: TableStyle): Double {
        val lineH = style.fontSize * 1.25f
        return placed.maxOf { it.lines.size } * lineH.toDouble() + style.pad.toDouble() * 2
    }

    /** Returns the new y. Mirrors Qt Pdf::drawTable including page breaks. */
    private fun drawTable(
        x: Double,
        startY: Double,
        widths: List<Double>,
        rows: List<List<Cell>>,
        style: TableStyle,
    ): Double {
        if (rows.isEmpty()) return startY

        fun flatten(row: List<Cell>): List<Placed> {
            val out = mutableListOf<Placed>()
            var col = 0
            for (c in row) {
                val w = (col until min(col + c.colSpan, widths.size)).sumOf { widths[it] }
                val p = paint(style.fontSize, c.bold, c.italic, c.color)
                out += Placed(c, col, w, wrap(c.text, p, w - style.pad * 2))
                col += c.colSpan
            }
            return out
        }

        fun totalHeight(): Double = rows.sumOf { rowHeight(flatten(it), style) }

        if (style.keepTogether && y + totalHeight() > PAGE_H - BOTTOM && totalHeight() < PAGE_H - TOP - BOTTOM) {
            newPage()
        }

        y = startY
        var index = 0
        var pageStart = startY
        while (index < rows.size) {
            val flat = flatten(rows[index])
            val h = rowHeight(flat, style)
            if (y + h > PAGE_H - BOTTOM) {
                newPage()
                pageStart = y
                if (style.repeatRows > 0) {
                    for (r in 0 until min(style.repeatRows, index)) {
                        val rf = flatten(rows[r])
                        drawRow(x, y, rf, style)
                        y += rowHeight(rf, style)
                    }
                }
            }
            drawRow(x, y, flat, style)
            y += h
            index++
        }

        // grid lines are drawn per-row; nothing left here
        @Suppress("UNUSED_VARIABLE") val unused = pageStart
        return y
    }

    private fun drawRow(x: Double, yy: Double, flat: List<Placed>, style: TableStyle) {
        val h = rowHeight(flat, style)
        val lineH = style.fontSize * 1.25f
        val baseline = yy + style.pad + style.fontSize

        var col = 0
        for (p in flat) {
            val cx = x + (0 until p.col).sumOf { widthListCache.getOrElse(it) { 0.0 } }
            // width list is recovered via cell width + span: stored in Placed, use running offset
            val cellX = x + runningOffset(flat, p)
            val cw = p.width
            if (p.cell.fill != null) {
                val bg = Paint().apply { color = p.cell.fill; this.style = android.graphics.Paint.Style.FILL }
                canvas!!.drawRect(RectF(cellX.toFloat(), yy.toFloat(), (cellX + cw).toFloat(), (yy + h).toFloat()), bg)
            }
            val tp = paint(style.fontSize, p.cell.bold, p.cell.italic, p.cell.color)
            p.lines.forEachIndexed { i, line ->
                val lx = when (p.cell.align) {
                    Align.LEFT -> cellX + style.pad
                    Align.CENTER -> cellX + cw / 2 - textWidth(line, tp) / 2
                    Align.RIGHT -> cellX + cw - style.pad - textWidth(line, tp)
                }
                canvas!!.drawText(line, lx.toFloat(), (baseline + i * lineH).toFloat(), tp)
            }
            col += p.cell.colSpan
            @Suppress("UNUSED_VARIABLE") val unusedCol = col
            @Suppress("UNUSED_VARIABLE") val unusedCx = cx
        }
        // grid
        var gx = x
        for (p in flat) gx += p.width
        val gridPaint = Paint().apply { color = GRID; strokeWidth = style.lineWidth }
        canvas!!.drawLine(x.toFloat(), yy.toFloat(), gx.toFloat(), yy.toFloat(), gridPaint)
        canvas!!.drawLine(x.toFloat(), (yy + h).toFloat(), gx.toFloat(), (yy + h).toFloat(), gridPaint)
        var vx = x
        canvas!!.drawLine(vx.toFloat(), yy.toFloat(), vx.toFloat(), (yy + h).toFloat(), gridPaint)
        for (p in flat) {
            vx += p.width
            canvas!!.drawLine(vx.toFloat(), yy.toFloat(), vx.toFloat(), (yy + h).toFloat(), gridPaint)
        }
    }

    private val widthListCache = mutableListOf<Double>()

    private fun runningOffset(flat: List<Placed>, target: Placed): Double {
        var offset = 0.0
        for (p in flat) {
            if (p === target) break
            offset += p.width
        }
        return offset
    }

    // ---------- kichwa (letterhead) ----------

    private fun letterhead(marginX: Double, title: String): Double {
        // Muundo wa 1.png (bila client): contractor kushoto, consultant kulia,
        // jina la mradi katikati kwenye safu moja na logos.
        // Muundo wa 2.png (na client): logos tatu juu, kisha jina la mradi katikati chini yake.
        val hasClient = project.clientLogo != null || project.clientName.isNotBlank()
        val maxLogoW = if (hasClient) 85.0 else 100.0
        val maxLogoH = if (hasClient) 56.0 else 42.0
        val y0 = 30.0
        val centerX = PAGE_W / 2

        val left = project.companyLogo
        val client = project.clientLogo
        val right = project.consultantLogo
        fun dims(b: Bitmap?): Pair<Double, Double> {
            if (b == null) return 0.0 to 0.0
            val scale = min(maxLogoW / b.width, maxLogoH / b.height)
            return (b.width * scale) to (b.height * scale)
        }
        val (ldW, ldH) = dims(left)
        val (cdW, cdH) = dims(client)
        val (rdW, rdH) = dims(right)

        if (left != null) {
            canvas!!.drawBitmap(
                left, null as android.graphics.Rect?,
                RectF(marginX.toFloat(), y0.toFloat(), (marginX + ldW).toFloat(), (y0 + ldH).toFloat()),
                Paint(Paint.FILTER_BITMAP_FLAG)
            )
        }
        if (right != null) {
            canvas!!.drawBitmap(
                right, null as android.graphics.Rect?,
                RectF(
                    (PAGE_W - marginX - rdW).toFloat(), y0.toFloat(),
                    (PAGE_W - marginX).toFloat(), (y0 + rdH).toFloat()
                ),
                Paint(Paint.FILTER_BITMAP_FLAG)
            )
        }
        if (hasClient && client != null) {
            canvas!!.drawBitmap(
                client, null as android.graphics.Rect?,
                RectF(
                    (centerX - cdW / 2).toFloat(), y0.toFloat(),
                    (centerX + cdW / 2).toFloat(), (y0 + cdH).toFloat()
                ),
                Paint(Paint.FILTER_BITMAP_FLAG)
            )
        }

        val logoRowH = maxOf(ldH, rdH, if (hasClient) cdH else 0.0)
        val sideW = 140.0
        val gap = 10.0
        val namesY = if (hasClient) y0 + maxLogoH + 12.0 else y0 + logoRowH / 2 + 3.0
        val centerColW = if (hasClient) {
            PAGE_W - marginX * 2 - 20.0
        } else {
            val leftEdge = if (left != null) marginX + ldW + gap else marginX
            val rightEdge = if (right != null) PAGE_W - marginX - rdW - gap else PAGE_W - marginX
            maxOf(120.0, rightEdge - leftEdge)
        }
        val titleSize = if (hasClient) 10.5f else 11f
        val titleFont = paint(titleSize, true, false, INK)
        val nameFont = paint(7.5f, false, false, INK)

        var centerCy = namesY
        if (project.name.isNotEmpty()) {
            for (line in wrap(project.name.uppercase(Locale.getDefault()), titleFont, centerColW)) {
                text(line, centerX, centerCy, titleSize, true, align = Align.CENTER)
                centerCy += 12.0
            }
        }
        if (hasClient && project.clientName.isNotEmpty()) {
            text("Client: ${project.clientName}", centerX, centerCy, 7.5f, align = Align.CENTER)
            centerCy += 9.0
        }

        var leftCy = namesY
        if (project.company.isNotEmpty()) {
            for (line in wrap(project.company, nameFont, sideW)) {
                text(line, marginX, leftCy, 7.5f)
                leftCy += 8.0
            }
        }
        var rightCy = namesY
        if (project.consultant.isNotEmpty()) {
            for (line in wrap(project.consultant, nameFont, sideW)) {
                text(line, PAGE_W - marginX, rightCy, 7.5f, align = Align.RIGHT)
                rightCy += 8.0
            }
        }

        val rowBottom = maxOf(centerCy, leftCy, rightCy, y0 + logoRowH) + if (hasClient) 4.0 else 2.0
        var yy = rowBottom + 8
        hline(marginX, PAGE_W - marginX, yy, 1f)
        yy += 16
        text(title, PAGE_W / 2, yy, 13f, true, align = Align.CENTER)
        yy += 10
        hline(marginX, PAGE_W - marginX, yy, 0.5f)
        yy += 16
        return yy
    }

    // ---------- wasaidizi wa maandishi ----------

    private fun dashed(s: String) = if (s.isEmpty()) "-" else s

    private fun fixed(v: Double, d: Int): String =
        if (v.isFinite() && v != 0.0) String.format(Locale.US, "%.${d}f", v) else "-"

    private fun raw(v: Double): String {
        if (!v.isFinite()) return "-"
        return if (v == v.toLong().toDouble()) v.toLong().toString()
        else String.format(Locale.US, "%.12g", v)
    }

    private fun whole(v: Double): String =
        if (v.isFinite()) String.format(Locale.US, "%.0f", v) else "-"

    // ---------- Single point ----------

    fun writeSingle(records: List<FdtRecord>) {
        newPage()
        val marginX = 36.0
        y = letterhead(marginX, "FIELD DENSITY TEST REPORT")
        text(
            SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date()),
            marginX, y, 9f, color = GRAY
        )
        y += 16

        val titles = listOf("Pt", "Date", "Chainage", "Cylinder", "Layer", "MDD", "OMC%", "Dry Dens.", "Comp %", "Req %", "Status")
        val colW = listOf(22.0, 58.0, 88.0, 40.0, 72.0, 36.0, 36.0, 46.0, 42.0, 38.0, 45.0)

        val groups = records.groupBy {
            listOf(it.road, it.location).joinToString("__") { v -> v.trim().lowercase() }
        }
        var pointCounter = 0
        for ((_, group) in groups) {
            if (y > 760) newPage()
            val road = group.first().road
            val chain = group.first().location
            val label = buildString {
                if (road.isNotBlank()) append("$road - ")
                append(if (chain.isNotBlank()) "KM $chain" else "KM haijaainishwa")
            }
            text(label, marginX, y, 11f, true)
            y += 8

            val head = titles.map {
                cell(it, bold = true, align = Align.CENTER, fill = HEAD_FILL, color = WHITE)
            }
            val rows = mutableListOf<List<Cell>>(head)
            val comps = mutableListOf<Double>()
            for (r in group) {
                pointCounter++
                val statusColor = when (r.status) {
                    "Passed" -> OK_GREEN
                    "Failed" -> FAIL_RED
                    else -> INK
                }
                rows += listOf(
                    cell(pointCounter.toString()),
                    cell(dashed(r.testDate.ifBlank { r.createdAt.take(10) })),
                    cell(dashed(r.location)),
                    cell("-"),
                    cell(dashed(r.layer)),
                    cell(dashed(fixed(r.mdd, 3))),
                    cell(dashed(fixed(r.omc, 1))),
                    cell("-"),
                    cell(dashed(fixed(r.compaction, 2))),
                    cell(dashed(r.required?.let { fixed(it, 2) } ?: "-")),
                    cell(dashed(r.status), color = statusColor),
                )
                if (r.compaction.isFinite() && r.compaction != 0.0) comps += r.compaction
            }
            widthListCache.clear()
            widthListCache.addAll(colW)
            y = drawTable(marginX, y, colW, rows, TableStyle(fontSize = 8f, pad = 3f, repeatRows = 1)) + 16

            if (comps.isNotEmpty()) {
                if (y > PAGE_H - BOTTOM - 30) newPage()
                text("Minimum single value %: ${fixed(comps.minOrNull()!!, 2)}", marginX, y, 9f, true)
                y += 12
                text("Average value %: ${fixed(comps.average(), 2)}", marginX, y, 9f, true)
                y += 12
            }
            y += 10
        }
        finish()
    }

    // ---------- Full worksheet ----------

    fun writeWorksheet(records: List<FdtRecord>) {
        newPage()
        val marginX = 30.0
        val tableW = PAGE_W - 2 * marginX
        y = letterhead(marginX, "FIELD DENSITY TEST BY SAND REPLACEMENT METHOD")

        records.forEachIndexed { ei, rec ->
            if (ei > 0) {
                newPage()
                y = 34.0
            }
            val holes = rec.holes
            val n = holes.size
            val first = holes.firstOrNull()

            // 1) Maelezo ya juu
            val cw1 = listOf(95.0, 190.0, 95.0, tableW - 380)
            val infoRows = listOf(
                listOf(cell("LOCATION:", true), cell(dashed(rec.location)), cell("TECHNICIAN:", true), cell(dashed(rec.technician))),
                listOf(cell("LAYER:", true), cell(dashed(rec.layer)), cell("CHECKED BY:", true), cell(dashed(rec.checkedBy))),
                listOf(cell("MATERIAL SOURCE:", true), cell(dashed(rec.materialSource)), cell("TEST DATE:", true), cell(dashed(rec.testDate))),
                if (rec.road.isNotBlank())
                    listOf(cell("ROAD:", true), cell(rec.road.trim()), cell("RFI No:", true), cell(dashed(rec.rfiNo)))
                else
                    listOf(cell(""), cell(""), cell("RFI No:", true), cell(dashed(rec.rfiNo))),
            )
            widthListCache.clear(); widthListCache.addAll(cw1)
            y = drawTable(marginX, y, cw1, infoRows, TableStyle(7f, 2.5f, keepTogether = true)) + 12

            // 2) Kiwango cha rejea
            text("Ref. BS1377 : Part 9 : 1990 & CML 1.9 : 2000", PAGE_W / 2, y, 7.5f, true, align = Align.CENTER)
            y += 8

            // 3) Namba za rejea, MDD, Sand density, OMC
            val cw3 = listOf(190.0, 100.0, 190.0, tableW - 480)
            val info3 = listOf(
                listOf(cell("Maximum dry density reference No:", true), cell(dashed(rec.mddRefNo)),
                       cell("Sand Calibration Reference No", true), cell(dashed(rec.sandCalibRefNo))),
                listOf(cell("Maximum dry density (g/cm³)", true), cell(fixed(rec.mdd, 3)),
                       cell("Density of sand (g/cm³)", true), cell(fixed(rec.sandDensity, 3))),
                listOf(cell("Optimum Moisture Content (%)", true), cell(fixed(rec.omc, 1)),
                       cell("Max. Layer Thickness (mm)", true), cell(dashed(rec.maxLayerThickness))),
                listOf(cell("Depth below formation level (mm)", true), cell(dashed(rec.depthBelowFormation)),
                       cell("Layer Type", true), cell(dashed(rec.layer))),
            )
            widthListCache.clear(); widthListCache.addAll(cw3)
            y = drawTable(marginX, y, cw3, info3, TableStyle(7f, 2.5f, keepTogether = true)) + 12

            text("After Compaction", marginX, y, 7.5f)
            y += 3

            // 4) Jedwali kuu (holes = nguzo)
            val cwMain = mutableListOf(13.0, 112.0, 24.0)
            val rest = tableW - 149
            val nCols = max(n, 1)
            repeat(nCols) { cwMain += rest / nCols }

            fun section(label: String) = listOf(cell(label, true, Align.CENTER, 3 + nCols, SECTION_FILL))
            fun dataRow(code: String, label: String, unit: String, fn: (FdtHoleData) -> String): List<Cell> {
                val row = mutableListOf(cell(code, true, Align.CENTER), cell(label), cell(unit))
                holes.forEach { row += cell(fn(it)) }
                return row
            }

            val mainRows = mutableListOf<List<Cell>>()
            mainRows += section("BULK DENSITY DETERMINATION")
            mainRows += listOf(buildList {
                add(cell("", false, Align.CENTER)); add(cell("Hole No")); add(cell(""))
                for (i in 0 until n) add(cell((i + 1).toString()))
            })
            mainRows += dataRow("", "Chainage", "") { dashed(it.chainage) }
            mainRows += dataRow("", "Offset/Side (m)", "") { dashed(it.offset) }
            mainRows += dataRow("", "Depth (mm)", "") { dashed(it.depth) }
            mainRows += dataRow("", "Cylinder", "No.") { dashed(it.cylinder) }
            mainRows += dataRow("", "Weight of Cylinder", "g") { if (it.cylinderWeight != 0.0) raw(it.cylinderWeight) else "-" }
            mainRows += dataRow("A", "Weight of Soil From Hole", "g") { dashed(raw(it.A)) }
            mainRows += dataRow("B", "Initial Sand in Cylinder", "g") { dashed(raw(it.B)) }
            mainRows += dataRow("C", "Final Sand in Cylinder", "g") { dashed(whole(it.C)) }
            mainRows += dataRow("D", "Sand in Cone", "g") { if (it.D != 0.0) raw(it.D) else "-" }
            mainRows += dataRow("E", "Wt of Sand in Hole (B-C-D)", "g") { if (it.sandInHole != 0.0) whole(it.sandInHole) else "-" }
            mainRows += dataRow("F", "Density of Sand (calibrated)", "g/cm³") { fixed(if (it.sandDensityOfHole != 0.0) it.sandDensityOfHole else rec.sandDensity, 3) }
            mainRows += dataRow("G", "Volume of Hole (E/F)", "cm³") { if (it.volume != 0.0) whole(it.volume) else "-" }
            mainRows += dataRow("H", "Wet Density (A/G)", "g/cm³") { fixed(it.wetDensity, 3) }

            mainRows += section("MOISTURE CONTENT DETERMINATION")
            mainRows += dataRow("I", "Container No", "No.") { dashed(it.containerNo) }
            mainRows += dataRow("J", "Container + Wet Soil", "g") { fixed(it.J, 2) }
            mainRows += dataRow("K", "Container + Dry Soil", "g") { fixed(it.K, 2) }
            mainRows += dataRow("L", "Weight of Container", "g") { fixed(it.L, 2) }
            mainRows += dataRow("M", "Weight of Water (J-K)", "g") { fixed(it.J - it.K, 2) }
            mainRows += dataRow("N", "Weight of Dry Soil (K-L)", "g") { fixed(it.K - it.L, 2) }
            mainRows += dataRow("O", "Moisture Content (M*100/N)", "%") { fixed(it.moisture, 2) }
            mainRows += dataRow("U", "Moisture Tolerance (±2.0%)", "%") { dashed(it.tolerance) }

            mainRows += section("DRY DENSITY DETERMINATION")
            mainRows += dataRow("P", "Dry Density (H*100/(O+100))", "g/cm³") { fixed(it.dryDensity, 3) }
            mainRows += dataRow("Q", "M.D.D. (PROCTOR)", "g/cm³") { fixed(rec.mdd, 3) }
            mainRows += dataRow("R", "O.M.C. (PROCTOR)", "%") { fixed(rec.omc, 1) }
            mainRows += dataRow("S", "COMPACTION (P*100/Q)", "%") { fixed(it.compaction, 2) }
            mainRows += listOf(buildList {
                add(cell("T", true, Align.CENTER))
                add(cell("AVERAGE COMPACTION, Xn", true))
                add(cell("%"))
                add(cell(rec.xn?.let { "${fixed(it, 2)}%" } ?: "-", true, Align.CENTER, nCols))
            })

            widthListCache.clear(); widthListCache.addAll(cwMain)
            y = drawTable(marginX, y, cwMain, mainRows, TableStyle(6f, 1.15f, 0.4f)) + 6

            // 5) Matokeo dhidi ya specification
            val cw5 = List(6) { tableW / 6 }
            val hasXn = rec.xn != null
            val hasXo = rec.xo != null
            val hasXs = rec.xs != null
            val hasXmin = rec.xmin != null
            fun passText(known: Boolean, pass: Boolean) = if (!known) "-" else if (pass) "Pass" else "Fail"
            val head5 = listOf("Number of tests, n", "", "Test Results", "Specifications", "Requirement", "Fail/Pass")
                .map { cell(it, true, Align.CENTER, fill = HEAD_FILL, color = WHITE) }
            val r5a = listOf(
                cell(n.toString(), true, Align.CENTER),
                cell("Average Value, %:", false, Align.CENTER),
                cell(if (hasXn) fixed(rec.xn!!, 2) else "-", false, Align.CENTER),
                cell(if (hasXs) fixed(rec.xs!!, 2) else "-", false, Align.CENTER),
                cell("Xn ≥ Xs", false, Align.CENTER),
                cell(passText(hasXn && hasXs, hasXn && hasXs && rec.xn!! >= rec.xs!!), false, Align.CENTER),
            )
            val r5b = listOf(
                cell("", false, Align.CENTER),
                cell("Minimum Single Value, %:", false, Align.CENTER),
                cell(if (hasXo) fixed(rec.xo!!, 2) else "-", false, Align.CENTER),
                cell(if (hasXmin) fixed(rec.xmin!!, 2) else "-", false, Align.CENTER),
                cell("Xo ≥ Xmin", false, Align.CENTER),
                cell(passText(hasXo && hasXmin, hasXo && hasXmin && rec.xo!! >= rec.xmin!!), false, Align.CENTER),
            )
            widthListCache.clear(); widthListCache.addAll(cw5)
            y = drawTable(marginX, y, cw5, listOf(head5, r5a, r5b), TableStyle(7f, 3f, keepTogether = true)) + 12

            text("Specified Compaction Requirements as per Table 7205/1 of SSRW'2000", PAGE_W / 2, y, 6.5f, italic = true, align = Align.CENTER)
            y += 12

            // 6) Remarks
            text("Remarks: " + rec.remarks.ifBlank { ".".repeat(100) }, marginX, y, 7.5f)
            y += 4

            // 7) Sahihi
            val cw7 = List(4) { tableW / 4 }
            val sig = listOf(
                listOf(cell("For Contractor", true, Align.CENTER, 2, SIG_FILL), cell("For Consultant", true, Align.CENTER, 2, SIG_FILL)),
                listOf("Checked by (MT)", "Verified by (ME)", "Checked by (MT)", "Verified by (ME)")
                    .map { cell(it, true, Align.CENTER, fill = SIG_FILL) },
                List(4) { cell("Name:") },
                List(4) { cell("Sign:") },
                List(4) { cell("Date:") },
            )
            widthListCache.clear(); widthListCache.addAll(cw7)
            y = drawTable(marginX, y, cw7, sig, TableStyle(7.5f, 5f, keepTogether = true))
        }
        finish()
    }

    private fun finish() {
        page?.let { document.finishPage(it) }
        document.writeTo(out)
        document.close()
        out.flush()
    }
}
