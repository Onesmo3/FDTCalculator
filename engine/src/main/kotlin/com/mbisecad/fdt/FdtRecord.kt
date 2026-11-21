package com.mbisecad.fdt

/**
 * Stored result of one FDT test session.
 *
 * Kept in the pure engine module so the shape is shared by every UI and can
 * be serialised by any platform (JSON on Android, files on desktop, etc.).
 */
data class FdtHoleData(
    val A: Double, val B: Double, val C: Double, val D: Double,
    val J: Double, val K: Double, val L: Double,
    val sandInHole: Double,
    val sandDensityOfHole: Double = 0.0,
    val volume: Double,
    val wetDensity: Double,
    val dryDensity: Double,
    val moisture: Double,
    val compaction: Double,
    val tolerance: String,
    val chainage: String = "",
    val offset: String = "",
    val depth: String = "",
    val cylinder: String = "",
    val cylinderWeight: Double = 0.0,
    val containerNo: String = "",
)

data class FdtRecord(
    val id: String,
    val createdAt: String,
    val project: String = "",
    val site: String = "",
    val testDate: String = "",
    val mode: String = "WORKSHEET", // SINGLE | WORKSHEET
    val layer: String = "",
    val sandDensity: Double = 0.0,
    val mdd: Double = 0.0,
    val omc: Double = 0.0,
    val required: Double? = null,
    val holes: List<FdtHoleData> = emptyList(),
    val compaction: Double = 0.0,
    val status: String = "",
    val xn: Double? = null,
    val xo: Double? = null,
    val xs: Double? = null,
    val xmin: Double? = null,
    // Metadata inayotumiwa na ripoti ya Qt (PDF). Defaults keep old records valid.
    val location: String = "",
    val technician: String = "",
    val checkedBy: String = "",
    val materialSource: String = "",
    val road: String = "",
    val rfiNo: String = "",
    val mddRefNo: String = "",
    val sandCalibRefNo: String = "",
    val maxLayerThickness: String = "",
    val depthBelowFormation: String = "",
    val remarks: String = "",
)
