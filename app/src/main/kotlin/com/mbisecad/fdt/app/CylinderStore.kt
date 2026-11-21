package com.mbisecad.fdt.app

import android.content.Context
import com.mbisecad.fdt.Cylinder
import com.mbisecad.fdt.defaultCylinders
import org.json.JSONArray
import org.json.JSONObject

/** User-editable cylinder list used by both calculator and worksheet. */
class CylinderStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("fdt_cylinders", Context.MODE_PRIVATE)

    fun all(): List<Cylinder> {
        val raw = prefs.getString(KEY, null) ?: return defaultCylinders()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Cylinder(
                    n = o.getInt("n"),
                    weight = o.getDouble("weight"),
                    sandInCone = o.getDouble("sandInCone"),
                )
            }.filter { it.n > 0 }.ifEmpty { defaultCylinders() }
        } catch (e: Exception) {
            defaultCylinders()
        }
    }

    fun save(list: List<Cylinder>) {
        val arr = JSONArray()
        list.forEach { c ->
            arr.put(JSONObject().apply {
                put("n", c.n)
                put("weight", c.weight)
                put("sandInCone", c.sandInCone)
            })
        }
        prefs.edit().putString(KEY, arr.toString()).commit()
    }

    fun add(cylinder: Cylinder): Boolean {
        val current = all().toMutableList()
        if (current.any { it.n == cylinder.n }) return false
        current += cylinder
        save(current.sortedBy { it.n })
        return true
    }

    fun remove(n: Int) {
        save(all().filter { it.n != n })
    }

    fun reset() {
        prefs.edit().remove(KEY).commit()
    }

    companion object {
        private const val KEY = "cylinders_json"
    }
}
