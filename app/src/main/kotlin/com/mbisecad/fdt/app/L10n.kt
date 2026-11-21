package com.mbisecad.fdt.app

import android.content.Context

/**
 * App-wide localization. English is the default; Swahili is optional.
 * PDF rendering never calls [loc], so exported reports remain English.
 */
object L10n {
    const val EN = "en"
    const val SW = "sw"

    private const val PREF = "fdt_language"
    private const val KEY = "lang"
    private var cached: String? = null

    fun init(context: Context) {
        if (cached == null) {
            cached = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getString(KEY, EN) ?: EN
        }
    }

    fun current(): String = cached ?: EN

    fun set(context: Context, language: String) {
        cached = if (language == SW) SW else EN
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY, current()).apply()
    }

    fun isSw(): Boolean = current() == SW

    private val toEnglish: List<Pair<String, String>> = listOf(
        "Sand Cone Method · menu kuu" to "Sand Cone Method · main menu",
        "Jaza data ya field kisha bonyeza Calculate." to "Fill in the field data, then tap Calculate.",
        "Jaza sehemu hii." to "Fill in this field.",
        "Jaza sehemu ya" to "Fill in",
        "kabla ya kubonyeza Calculate." to "before tapping Calculate.",
        "Andika namba sahihi." to "Enter a valid number.",
        "Thamani ya" to "Value of",
        "si namba sahihi." to "is not a valid number.",
        "Hesabu imeshindikana." to "Calculation failed.",
        "Calculate kwanza." to "Calculate first.",
        "Imehifadhiwa (" to "Saved (",
        "Layer no (hiari, mf." to "Layer no (optional, e.g.",
        "Data ya Nyerere Road" to "Nyerere Road data",
        "imejazwa tayari." to "is pre-filled.",
        "Jaza Density of Sand, MDD na OMC kwanza." to "Fill in Density of Sand, MDD and OMC first.",
        "Density of Sand na MDD lazima ziwe zaidi ya sifuri." to "Density of Sand and MDD must be greater than zero.",
        "Hakuna hole iliyokamilika. Jaza angalau hole moja." to "No complete hole. Fill in at least one hole.",
        "hole moja" to "one hole",
        "holes nyingi" to "many holes",
        "ziko kwenye Settings." to "are in Settings.",
        "Kotlin port · v1.0" to "Kotlin port · v1.0",
        "Futa records zote zilizohifadhiwa?" to "Delete all saved records?",
        "Records zimefutwa." to "Records deleted.",
        "Report info na logos" to "Report info and logos",
        "vinawekwa kwenye" to "are set in",
        "Hakuna records bado." to "No records yet.",
        "Fanya Calculate kisha Save result." to "Calculate, then tap Save result.",
        "Record imefutwa" to "Record deleted",
        "Unaweza kuirejesha kwa Undo." to "You can restore it with Undo.",
        "Record imerejeshwa." to "Record restored.",
        "Imeshindikana kufungua" to "Could not open",
        "Downloads haifunguki" to "Downloads cannot be opened",
        "File haiandikiki" to "File cannot be written",
        "PDF imeshindikana:" to "PDF failed:",
        "Faili halisomeki." to "File cannot be read.",
        "Importd records:" to "Imported records:",
        "Faili si sahihi:" to "Invalid file:",
        "Hakuna records za kutengeneza PDF." to "No records to generate PDF.",
        "Record haipatikani." to "Record not found.",
        "Single point — hakuna hole rows za kuhariri." to "Single point — no hole rows to edit.",
        "si sahihi." to "is invalid.",
        "lazima > 0." to "must be > 0.",
        "imeshindikana:" to "failed:",
        "tarajio" to "expected",
        "mf." to "e.g.",
        " na " to " and ",
        "Project info imehifadhiwa." to "Project info saved.",
        "Client fields ni hiari. Zinatumika kwenye PDF ikiwa zimejazwa." to "Client fields are optional. They are used in the PDF when filled.",
        "Rudisha cylinders 4 za awali?" to "Restore the original 4 cylinders?",
        "Cylinder defaults zimerudishwa." to "Cylinder defaults restored.",
        "Futa matokeo yote yaliyohifadhiwa?" to "Delete all saved results?",
        "Cylinders zilizopo" to "Available cylinders",
        "zinatumika kwenye single point na worksheet." to "used in single point and worksheet.",
        "imeondolewa." to "removed.",
        "Jaza namba, uzito na sand-in-cone sahihi." to "Enter a valid number, weight and sand-in-cone.",
        "ipo tayari." to "already exists.",
        "imeongezwa." to "added.",
        "Logo imehifadhiwa." to "Logo saved.",
        "Logo imeshindikana." to "Logo failed.",
    ).sortedByDescending { it.first.length }

    private val toSwahili: List<Pair<String, String>> = listOf(
        "Sand Cone Method · main menu" to "Sand Cone Method · menu kuu",
        "Fill in the field data, then tap Calculate." to "Jaza data ya field kisha bonyeza Calculate.",
        "Fill in this field." to "Jaza sehemu hii.",
        "Fill in" to "Jaza sehemu ya",
        "before tapping Calculate." to "kabla ya kubonyeza Calculate.",
        "Enter a valid number." to "Andika namba sahihi.",
        "Value of" to "Thamani ya",
        "is not a valid number." to "si namba sahihi.",
        "Calculation failed." to "Hesabu imeshindikana.",
        "Calculate first." to "Calculate kwanza.",
        "Saved (" to "Imehifadhiwa (",
        "Layer no (optional, e.g." to "Layer no (hiari, mf.",
        "Nyerere Road data" to "Data ya Nyerere Road",
        "is pre-filled." to "imejazwa tayari.",
        "Fill in Density of Sand, MDD and OMC first." to "Jaza Density of Sand, MDD na OMC kwanza.",
        "Density of Sand and MDD must be greater than zero." to "Density of Sand na MDD lazima ziwe zaidi ya sifuri.",
        "No complete hole. Fill in at least one hole." to "Hakuna hole iliyokamilika. Jaza angalau hole moja.",
        "one hole" to "hole moja",
        "many holes" to "holes nyingi",
        "are in Settings." to "ziko kwenye Settings.",
        "Delete all saved records?" to "Futa records zote zilizohifadhiwa?",
        "Records deleted." to "Records zimefutwa.",
        "Report info and logos" to "Report info na logos",
        "are set in" to "vinawekwa kwenye",
        "No records yet." to "Hakuna records bado.",
        "Calculate, then tap Save result." to "Fanya Calculate kisha Save result.",
        "Record deleted" to "Record imefutwa",
        "You can restore it with Undo." to "Unaweza kuirejesha kwa Undo.",
        "Record restored." to "Record imerejeshwa.",
        "Could not open" to "Imeshindikana kufungua",
        "Downloads cannot be opened" to "Downloads haifunguki",
        "File cannot be written" to "File haiandikiki",
        "PDF failed:" to "PDF imeshindikana:",
        "File cannot be read." to "Faili halisomeki.",
        "Imported records:" to "Importd records:",
        "Invalid file:" to "Faili si sahihi:",
        "No records to generate PDF." to "Hakuna records za kutengeneza PDF.",
        "Record not found." to "Record haipatikani.",
        "Single point — no hole rows to edit." to "Single point — hakuna hole rows za kuhariri.",
        "is invalid." to "si sahihi.",
        "must be > 0." to "lazima > 0.",
        "failed:" to "imeshindikana:",
        "expected" to "tarajio",
        "e.g." to "mf.",
        " and " to " na ",
        "Project info saved." to "Project info imehifadhiwa.",
        "Client fields are optional. They are used in the PDF when filled." to "Client fields ni hiari. Zinatumika kwenye PDF ikiwa zimejazwa.",
        "Restore the original 4 cylinders?" to "Rudisha cylinders 4 za awali?",
        "Cylinder defaults restored." to "Cylinder defaults zimerudishwa.",
        "Delete all saved results?" to "Futa matokeo yote yaliyohifadhiwa?",
        "Available cylinders" to "Cylinders zilizopo",
        "used in single point and worksheet." to "zinatumika kwenye single point na worksheet.",
        " removed." to " imeondolewa.",
        "Enter a valid number, weight and sand-in-cone." to "Jaza namba, uzito na sand-in-cone sahihi.",
        "already exists." to "ipo tayari.",
        " added." to " imeongezwa.",
        "Logo saved." to "Logo imehifadhiwa.",
        "Logo failed." to "Logo imeshindikana.",

        // Titles, cards, buttons
        "Sand Cone Method · Field Density Test" to "Njia ya Mchanga · Jaribio la Field Density",
        "Sand Cone Method · multi-hole summary" to "Njia ya Mchanga · muhtasari wa mashimo mengi",
        "Single point test" to "Jaribio la pointi moja",
        "Report info" to "Maelezo ya ripoti",
        "Test parameters" to "Vigezo vya jaribio",
        "Actions" to "Vitendo",
        "Worksheet result" to "Matokeo ya worksheet",
        "Result" to "Matokeo",
        "Holes" to "Mashimo",
        "Tools & data" to "Zana na data",
        "Engine self-test" to "Jaribio la engine",
        "Save single result" to "Hifadhi matokeo ya pointi moja",
        "Saved results" to "Matokeo yaliyohifadhiwa",
        "Legal & About" to "Kisheria & Maelezo",
        "Settings" to "Mipangilio",
        "Backup & restore" to "Backup & kurejesha",
        "Backup, restore & PDF export" to "Backup, kurejesha & export ya PDF",
        "Report Setup" to "Usanidi wa ripoti",
        "Cylinder details" to "Maelezo ya cylinder",
        "Privacy Policy" to "Sera ya Faragha",
        "Disclaimer" to "Kanusho",
        "Terms of Service" to "Masharti ya Huduma",
        "Copyrights" to "Hakimiliki",
        "Contact Us" to "Wasiliana Nasi",
        "About Us" to "Kuhusu Sisi",
        "Save changes" to "Hifadhi mabadiliko",
        "Cancel" to "Ghairi",
        "Edit" to "Hariri",
        "Delete" to "Futa",
        "View" to "Tazama",
        "Add hole" to "Ongeza shimo",
        "Remove last" to "Ondoa la mwisho",
        "Calculate worksheet" to "Kokotoa worksheet",
        "Save result" to "Hifadhi matokeo",
        "← Single point" to "← Pointi moja",
        "Calculate" to "Kokotoa",
        "Clear" to "Futa",
        "Export PDF for this result" to "Export PDF kwa matokeo haya",
        "Export JSON" to "Export JSON",
        "Import JSON" to "Import JSON",
        "Clear all" to "Futa zote",
        "PDF (all)" to "PDF (zote)",
        "← Back" to "← Nyuma",
        "Language" to "Lugha",
        "FDT Calculator · information" to "FDT Calculator · maelezo",
        "English" to "English",
        "Swahili" to "Kiswahili",

        // Field labels
        "Project (optional)" to "Mradi (hiari)",
        "Site (optional)" to "Tovuti (hiari)",
        "Test date" to "Tarehe ya jaribio",
        "Location" to "Mahali",
        "Road" to "Barabara",
        "RFI No" to "Namba ya RFI",
        "Technician" to "Fundi",
        "Checked by" to "Aliyekagua",
        "Material source" to "Chanzo cha nyenzo",
        "MDD reference No" to "Namba ya rejea ya MDD",
        "Sand calibration reference No" to "Namba ya rejea ya calibration ya mchanga",
        "Sand calibration ref No" to "Namba ya rejea ya calibration ya mchanga",
        "Max layer thickness (mm)" to "Unene wa juu wa tabaka (mm)",
        "Depth below formation (mm)" to "Kina chini ya formation (mm)",
        "Remarks" to "Maoni",
        "Density of sand (g/cm³)" to "Msongamano wa mchanga (g/cm³)",
        "MDD (g/cm³)" to "MDD (g/cm³)",
        "OMC (%)" to "OMC (%)",
        "Required compaction (%) — optional" to "Compaction inayohitajika (%) — hiari",
        "Location / chainage" to "Mahali / chainage",
        "Layer tested (mf. Backfill)" to "Tabaka lililojaribiwa (mf. Backfill)",
        "Layer (Table 7205/1)" to "Tabaka (Jedwali 7205/1)",
        "Layer no (optional, e.g. 1ˢᵗ layer)" to "Layer no (hiari, mf. 1ˢᵗ layer)",
        "Initial sand in cylinder (g)" to "Mchanga wa awali kwenye cylinder (g)",
        "Final sand in cylinder (g)" to "Mchanga wa mwisho kwenye cylinder (g)",
        "Sand in cone (g)" to "Mchanga kwenye cone (g)",
        "Weight of soil from hole (g)" to "Uzito wa udongo kutoka shimoni (g)",
        "A · soil (g)" to "A · udongo (g)",
        "B · initial sand (g)" to "B · mchanga wa awali (g)",
        "C · final sand (g)" to "C · mchanga wa mwisho (g)",
        "D · cone (g)" to "D · cone (g)",
        "J · container + wet (g)" to "J · kontena + mvua (g)",
        "K · container + dry (g)" to "K · kontena + kavu (g)",
        "L · container (g)" to "L · kontena (g)",
        "Chainage" to "Chainage",
        "Offset / Side (m)" to "Offset / Upande (m)",
        "Depth (mm)" to "Kina (mm)",
        "Cylinder No" to "Namba ya cylinder",
        "W · cylinder weight (g)" to "W · uzito wa cylinder (g)",
        "Container No (I)" to "Namba ya kontena (I)",
        "Cylinder preset (auto-fills D + weight)" to "Cylinder preset (hujaza D + uzito)",
        "Project" to "Mradi",
        "Site" to "Tovuti",
        "Layer" to "Tabaka",
        "Client Name" to "Jina la Client",
        "Client Logo" to "Logo ya Client",

        // Summary + status
        "STATUS" to "STATUS",
        "Ready" to "Tayari",
        "Passed" to "Imefaulu",
        "Failed" to "Imeshindwa",
        "Error" to "Hitilafu",
        "Message" to "Ujumbe",
        "Sand in Hole" to "Mchanga shimoni",
        "Volume" to "Kiasi",
        "Wet Density" to "Msongamano wenye unyevu",
        "Dry Density" to "Msongamano mkavu",
        "Compaction" to "Compaction",
        "Required" to "Inahitajika",
        "Holes (n)" to "Mashimo (n)",
        "Xn  (average)" to "Xn (wastani)",
        "Xo  (minimum)" to "Xo (kiwango cha chini)",
        "Xs  (required)" to "Xs (inahitajika)",
        "Xmin (required)" to "Xmin (inahitajika)",
        "complete" to "kamilika",
        "Hole " to "Shimo ",
        "holes " to "mashimo ",
    ).sortedByDescending { it.first.length }

    fun translate(value: String): String {
        val pairs = if (isSw()) toSwahili else toEnglish
        var result = value
        pairs.forEach { (from, to) -> result = result.replace(from, to) }
        return result
    }
}

fun String.loc(): String = L10n.translate(this)

/** Applies the saved language before any view is inflated. */
abstract class BaseActivity : android.app.Activity() {
    private val translatedViews = java.util.IdentityHashMap<android.view.View, Boolean>()

    override fun attachBaseContext(newBase: android.content.Context) {
        L10n.init(newBase.applicationContext)
        super.attachBaseContext(newBase)
    }

    override fun setContentView(view: android.view.View?) {
        translateAndWatch(view)
        super.setContentView(view)
    }

    /** Translates every label/button/caption, including views added later (extra holes). */
    private fun translateAndWatch(view: android.view.View?) {
        if (view == null || translatedViews.containsKey(view)) return
        if (view.tag as? String == "fdt_no_translate") return
        translatedViews[view] = true
        when (view) {
            is android.widget.TextView -> {
                val text = view.text
                if (!text.isNullOrEmpty()) view.setText(text.toString().loc())
                val hint = view.hint
                if (!hint.isNullOrEmpty()) view.hint = hint.toString().loc()
                val err = view.error
                if (!err.isNullOrEmpty()) view.error = err.toString().loc()
            }
            is android.view.ViewGroup -> {
                for (i in 0 until view.childCount) translateAndWatch(view.getChildAt(i))
                view.setOnHierarchyChangeListener(object : android.view.ViewGroup.OnHierarchyChangeListener {
                    override fun onChildViewAdded(parent: android.view.View?, child: android.view.View?) {
                        translateAndWatch(child)
                    }
                    override fun onChildViewRemoved(parent: android.view.View?, child: android.view.View?) {}
                })
            }
        }
    }

    fun toast(message: String, duration: Int = android.widget.Toast.LENGTH_SHORT) {
        android.widget.Toast.makeText(this, message.loc(), duration).show()
    }
}
