package com.mbisecad.fdt.app

/** Short placeholder copy; inaweza kuhaririwa bila kubadilika muundo wa app. */
object LegalText {
    const val PRIVACY = "PRIVACY"
    const val DISCLAIMER = "DISCLAIMER"
    const val TERMS = "TERMS"
    const val COPYRIGHT = "COPYRIGHT"
    const val CONTACT = "CONTACT"
    const val ABOUT = "ABOUT"

    val items = listOf(PRIVACY, DISCLAIMER, TERMS, COPYRIGHT, CONTACT, ABOUT)

    fun title(type: String): String = when (type) {
        PRIVACY -> "Privacy Policy"
        DISCLAIMER -> "Disclaimer"
        TERMS -> "Terms of Service"
        COPYRIGHT -> "Copyrights"
        CONTACT -> "Contact Us"
        ABOUT -> "About Us"
        else -> "FDT Calculator"
    }

    fun body(type: String): String = when (type) {
        PRIVACY -> """
            FDT Calculator huhifadhi matokeo yote kwenye kifaa chako pekee (SharedPreferences).
            Hakuna data inayotumwa kwenye seva ya nje. Backup na PDF unazotengeneza
            zinaandikwa kwenye Download ya kifaa chako.

            Ruhusa za kufikia faili (SAF/MediaStore) hutumiwa tu unapochagua kuhamisha
            au kuleta backup/logo. Unaweza kufuta data yote wakati wowote kutoka
            Settings → Backup & Restore → Clear all.
        """.trimIndent()

        DISCLAIMER -> """
            Matokeo ya FDT Calculator ni makadirio yanayotokana na data uliyoingiza.
            Programu hii ni msaada wa uhandisi, si mbadala wa majaribio ya maabara
            yaliyothibitishwa, uidhinishaji wa mhandisi, wala viwango vya kampuni.

            Kila wakati thibitisha matokeo kwa kufuata BS 1377: Part 9 / CML 1.9
            na taratibu za mradi wako. Mtumiaji ana jukumu la uhakiki wa mwisho.
        """.trimIndent()

        TERMS -> """
            Kwa kutumia programu hii, unakubali kuitumia kwa madhumuni halali ya
            uhandisi na kuhakikisha usahihi wa data unayoingiza.

            Programu inatolewa "kama ilivyo" bila dhamana ya aina yoyote. Mabadiliko
            ya formulas au muundo yanaweza kufanywa ili kuboresha usahihi na matumizi.
        """.trimIndent()

        COPYRIGHT -> """
            © 2026 Onesmo (GitHub: Onesmo3). Haki zote zimehifadhiwa.

            Kanuni za hesabu zilitokana na utekelezaji wa awali wa Qt/C++ wa
            Field Density Test Calculator, uliobadilishwa kuwa Kotlin kwa Android.
            Nembo na majina ya kampuni ni mali ya wenyewe.
        """.trimIndent()

        CONTACT -> """
            GitHub: https://github.com/Onesmo3/FDTCalculator

            Kwa masuala ya kuboresha programu, ripoti ya hitilafu, au maoni kuhusu
            muundo wa ripoti ya PDF, tafadhali fungua issue kwenye repository hiyo.
        """.trimIndent()

        ABOUT -> """
            FDT Calculator — Sand Cone Method.

            Programu ya Android ya kukokotoa field density kwa njia ya mchanga:
            single point, worksheet yenye holes nyingi (Xn/Xo/Xs/Xmin),
            hifadhi ya ndani, backup/restore, na ripoti ya PDF inayofuata
            mpangilio wa toleo la Qt.

            Engine iko kwenye module tofauti ya Kotlin isiyo tegemea Android,
            ili hesabu ziweze kujaribiwa kabla ya kusambazwa.
        """.trimIndent()

        else -> "Hakuna maudhui."
    }
}
