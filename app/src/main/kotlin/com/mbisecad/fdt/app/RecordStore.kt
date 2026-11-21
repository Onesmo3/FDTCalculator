package com.mbisecad.fdt.app

import android.content.Context
import com.mbisecad.fdt.FdtHoleData
import com.mbisecad.fdt.FdtRecord
import org.json.JSONArray
import org.json.JSONObject

/** SharedPreferences-backed store with JSON backup/restore. No external dependencies. */
class RecordStore(private val context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Set when stored JSON cannot be parsed - surfaced in RecordsActivity diagnostics. */
    var lastError: String? = null
        private set

    fun raw(): String = prefs.getString(KEY_RECORDS, "[]") ?: "[]"

    fun all(): List<FdtRecord> {
        lastError = null
        val raw = raw()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).map { array.getJSONObject(it).toRecord() }
        } catch (e: Exception) {
            lastError = e.message ?: "JSON parse error"
            emptyList()
        }
    }

    fun save(record: FdtRecord): Int {
        val records = all().filterNot { it.id == record.id }.toMutableList()
        records.add(0, record)
        persist(records)
        return records.size
    }

    fun delete(id: String) {
        persist(all().filterNot { it.id == id })
    }

    fun clear() {
        persist(emptyList())
    }

    fun exportJson(): String {
        val array = JSONArray()
        all().forEach { array.put(it.toJson()) }
        return array.toString(2)
    }

    /** Returns the number of newly imported records (duplicates by id are skipped). */
    fun importJson(json: String): Int {
        val incoming = JSONArray(json)
        val records = all().toMutableList()
        var added = 0
        for (i in 0 until incoming.length()) {
            val record = incoming.getJSONObject(i).toRecord()
            if (records.none { it.id == record.id }) {
                records.add(record)
                added++
            }
        }
        persist(records)
        return added
    }

    private fun persist(records: List<FdtRecord>) {
        val array = JSONArray()
        records.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_RECORDS, array.toString()).commit()
    }

    companion object {
        private const val PREFS_NAME = "fdt_records"
        private const val KEY_RECORDS = "records"
    }
}

private fun JSONObject.optionalDouble(name: String): Double? =
    if (!has(name) || isNull(name)) null else getDouble(name)

private fun FdtHoleData.toJson(): JSONObject = JSONObject().apply {
    put("A", A); put("B", B); put("C", C); put("D", D)
    put("J", J); put("K", K); put("L", L)
    put("sandInHole", sandInHole)
    put("volume", volume)
    put("wetDensity", wetDensity)
    put("dryDensity", dryDensity)
    put("moisture", moisture)
    put("compaction", compaction)
    put("tolerance", tolerance)
    put("chainage", chainage)
    put("offset", offset)
    put("depth", depth)
    put("cylinder", cylinder)
    put("cylinderWeight", cylinderWeight)
    put("containerNo", containerNo)
}

private fun JSONObject.toHole(): FdtHoleData = FdtHoleData(
    A = getDouble("A"), B = getDouble("B"), C = getDouble("C"), D = getDouble("D"),
    J = getDouble("J"), K = getDouble("K"), L = getDouble("L"),
    sandInHole = getDouble("sandInHole"),
    volume = getDouble("volume"),
    wetDensity = getDouble("wetDensity"),
    dryDensity = getDouble("dryDensity"),
    moisture = getDouble("moisture"),
    compaction = getDouble("compaction"),
    tolerance = optString("tolerance"),
    chainage = optString("chainage"),
    offset = optString("offset"),
    depth = optString("depth"),
    cylinder = optString("cylinder"),
    cylinderWeight = optDouble("cylinderWeight"),
    containerNo = optString("containerNo"),
)

private fun FdtRecord.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("createdAt", createdAt)
    put("project", project)
    put("site", site)
    put("testDate", testDate)
    put("mode", mode)
    put("layer", layer)
    put("sandDensity", sandDensity)
    put("mdd", mdd)
    put("omc", omc)
    put("required", required ?: JSONObject.NULL)
    val holesArray = JSONArray()
    holes.forEach { holesArray.put(it.toJson()) }
    put("holes", holesArray)
    put("compaction", compaction)
    put("status", status)
    put("xn", xn ?: JSONObject.NULL)
    put("xo", xo ?: JSONObject.NULL)
    put("xs", xs ?: JSONObject.NULL)
    put("xmin", xmin ?: JSONObject.NULL)
    put("location", location)
    put("technician", technician)
    put("checkedBy", checkedBy)
    put("materialSource", materialSource)
    put("road", road)
    put("rfiNo", rfiNo)
    put("mddRefNo", mddRefNo)
    put("sandCalibRefNo", sandCalibRefNo)
    put("maxLayerThickness", maxLayerThickness)
    put("depthBelowFormation", depthBelowFormation)
    put("remarks", remarks)
}

private fun JSONObject.toRecord(): FdtRecord {
    val holesArray = optJSONArray("holes")
    val holes = if (holesArray == null) emptyList() else
        (0 until holesArray.length()).map { holesArray.getJSONObject(it).toHole() }

    return FdtRecord(
        id = optString("id"),
        createdAt = optString("createdAt"),
        project = optString("project"),
        site = optString("site"),
        testDate = optString("testDate"),
        mode = optString("mode"),
        layer = optString("layer"),
        sandDensity = optDouble("sandDensity"),
        mdd = optDouble("mdd"),
        omc = optDouble("omc"),
        required = optionalDouble("required"),
        holes = holes,
        compaction = optDouble("compaction"),
        status = optString("status"),
        xn = optionalDouble("xn"),
        xo = optionalDouble("xo"),
        xs = optionalDouble("xs"),
        xmin = optionalDouble("xmin"),
        location = optString("location"),
        technician = optString("technician"),
        checkedBy = optString("checkedBy"),
        materialSource = optString("materialSource"),
        road = optString("road"),
        rfiNo = optString("rfiNo"),
        mddRefNo = optString("mddRefNo"),
        sandCalibRefNo = optString("sandCalibRefNo"),
        maxLayerThickness = optString("maxLayerThickness"),
        depthBelowFormation = optString("depthBelowFormation"),
        remarks = optString("remarks"),
    )
}
