package com.mbisecad.fdt.app

/** Official legal copy in English (default) and Kiswahili. */
object LegalText {
    const val PRIVACY = "PRIVACY"
    const val DISCLAIMER = "DISCLAIMER"
    const val TERMS = "TERMS"
    const val COPYRIGHT = "COPYRIGHT"
    const val CONTACT = "CONTACT"
    const val ABOUT = "ABOUT"

    val items = listOf(PRIVACY, DISCLAIMER, TERMS, COPYRIGHT, CONTACT, ABOUT)

    fun title(type: String): String = when (type) {
        PRIVACY -> "Privacy Policy".loc()
        DISCLAIMER -> "Disclaimer".loc()
        TERMS -> "Terms of Service".loc()
        COPYRIGHT -> "Copyrights".loc()
        CONTACT -> "Contact Us".loc()
        ABOUT -> "About Us".loc()
        else -> "FDT Calculator"
    }

    fun body(type: String): String {
        val sw = L10n.isSw()
        return when (type) {
            PRIVACY -> if (sw) PRIVACY_SW else PRIVACY_EN
            DISCLAIMER -> if (sw) DISCLAIMER_SW else DISCLAIMER_EN
            TERMS -> if (sw) TERMS_SW else TERMS_EN
            COPYRIGHT -> if (sw) COPYRIGHT_SW else COPYRIGHT_EN
            CONTACT -> if (sw) CONTACT_SW else CONTACT_EN
            ABOUT -> if (sw) ABOUT_SW else ABOUT_EN
            else -> "No content.".loc()
        }
    }

    private val PRIVACY_EN = """
        PRIVACY POLICY — FDT CALCULATOR
        Effective 21 November 2026

        1. Data controller
        The data controller for this application is Onesmo Komba (GitHub: Onesmo3),
        through the FDT Calculator project. No third party receives your data
        from this application.

        2. Information we store
        The application stores on your device only:
        - test results (single point and worksheet), including A–D, J–L, chainage,
          offset, depth, cylinder and container details;
        - project information (project name, contractor, consultant, client, test
          date, RFI, technician, checked by and remarks);
        - settings (density of sand, MDD, OMC, required compaction, cylinder list
          and PDF logos).
        This information is not used for advertising, user profiling or tracking.

        3. No external transfer and no analytics
        FDT Calculator has no server, no user account and no remote collection
        system. There are no analytics, crash reports or identifiers transmitted
        outside your device.

        4. Device permissions
        - MediaStore/Downloads: used only to save PDFs and backups that you create.
        - Storage Access Framework (SAF): used only when you import a backup or
          choose a client/contractor logo.
        These permissions are not needed for ordinary calculations.

        5. Retention and deletion
        Data remains on your device until you delete it through
        Settings → Backup & Restore → Clear all, or until you uninstall the app.
        Uninstalling normally removes all app data as well.

        6. Access, correction and deletion
        You can view, edit or delete records through Records → Edit/Delete.
        Settings and logos are managed in Settings.

        7. Children
        This application is not intended for children under 13 and does not
        knowingly collect their information.

        8. Changes
        Changes to this policy will be shown in the application with a new
        effective date.

        9. Contact
        Questions: https://github.com/Onesmo3/FDTCalculator (open an Issue).
    """.trimIndent()

    private val PRIVACY_SW = """
        PRIVACY POLICY — FDT CALCULATOR
        Inatumika tarehe 21 Novemba 2026

        1. Mdhibiti wa taarifa
        Mdhibiti wa taarifa zinazohusiana na programu hii ni Onesmo Komba
        (GitHub: Onesmo3), kupitia mradi wa FDT Calculator. Hakuna mamlaka
        nyingine inayopokea taarifa zako kutoka kwenye programu hii.

        2. Taarifa tunazohifadhi
        Programu huhifadhi kwenye kifaa chako pekee:
        - matokeo ya majaribio (single point na worksheet), ikiwemo A–D, J–L,
          chainage, offset, depth, cylinder na container;
        - taarifa za mradi (jina la mradi, contractor, consultant, client,
          tarehe ya jaribio, RFI, technician, checked by na remarks);
        - mipangilio (density of sand, MDD, OMC, required compaction, orodha
          ya cylinders, na logo za PDF).
        Taarifa hizi hazitumiwi kwa utangazwaji, uundaji wa wasifu wa mtumiaji,
        wala ufuatiliaji wa tabia.

        3. Hakuna uhamisho wa nje wala analytics
        FDT Calculator haina seva, akaunti ya mtumiaji, wala mfumo wa kukusanya
        taarifa kwa mbali. Hakuna analytics, crash reporting, au vitambulisho
        vinavyotumwa nje ya kifaa chako.

        4. Ruhusa za kifaa
        - MediaStore/Downloads: hutumiwa kuhifadhi PDF na backup unazotengeneza
          mwenyewe.
        - Storage Access Framework (SAF): hutumiwa unapochagua kuleta backup
          au kuingiza client/contractor logo.
        Ruhusa hizi hutumiwa tu unapoanzisha kitendo husika.

        5. Uhifadhi na ufutaji
        Data inabaki kwenye kifaa hadi ufute mwenyewe kupitia
        Settings → Backup & Restore → Clear all, au ufute programu.

        6. Kutazama, kusahihisha na kufuta
        Unaweza kutazama, kuhariri au kufuta rekodi kupitia Records → Edit/Delete.
        Mipangilio na logo zinasimamiwa kwenye Settings.

        7. Watoto
        Programu hii haikusudiwa watoto walio chini ya miaka 13 na haikusanyi
        taarifa zao kwa makusudi.

        8. Mabadiliko ya sera
        Mabadiliko yataonyeshwa kwenye programu pamoja na tarehe mpya ya
        kuanza kutumika.

        9. Mawasiliano
        Maswali au maombi: https://github.com/Onesmo3/FDTCalculator (fungua Issue).
    """.trimIndent()

    private val DISCLAIMER_EN = """
        DISCLAIMER — FDT CALCULATOR
        Effective 21 November 2026

        1. Engineering aid only
        FDT Calculator is a tool for estimating field density by the sand cone
        method. Its results are an engineering aid; they are not an accredited
        laboratory report, a certificate, or a substitute for the judgement of a
        qualified engineer.

        2. Accuracy of data
        Results depend entirely on the measurements and data you enter (A–D, J–L,
        density of sand, MDD, OMC and required compaction). You are responsible
        for calibrated equipment, valid calibration and accurate data entry.

        3. Standards and procedures
        Calculations follow AASHTO T 191 / ASTM D1556 and are compared with
        project standards such as BS 1377: Part 9 / CML 1.9 and Table 7205/1
        (SSRW 2000) where applicable. You must confirm that the method and the
        acceptance criteria (Xs/Xmin) match your project specification.

        4. No warranty
        The application is provided "as is". It does not warrant that results
        will satisfy any legal, contractual or laboratory requirement.

        5. Limitation of liability
        To the extent permitted by law, Onesmo Komba and the project contributors
        are not liable for any loss, cost or decision arising from use of the
        results without review by an engineer or laboratory.

        6. Professional use
        Do not use this application as the sole evidence for accepting or
        rejecting work, making payment, or approving a project without review by
        the relevant authority.
    """.trimIndent()

    private val DISCLAIMER_SW = """
        DISCLAIMER — FDT CALCULATOR
        Inatumika tarehe 21 Novemba 2026

        1. Zana ya kusaidia uhandisi tu
        FDT Calculator ni zana ya kukokotoa makadirio ya field density kwa njia
        ya mchanga (sand cone). Matokeo yake ni msaidizi wa uhandisi; si ripoti
        ya maabara iliyoidhinishwa, si cheti, wala si mbadala wa uamuzi wa
        mhandisi aliyehitimu.

        2. Usahihi wa data
        Matokeo hutegemea kabisa vipimo na data unayoingiza (A–D, J–L, density
        of sand, MDD, OMC na required compaction). Mtumiaji ana jukumu la
        kuhakikisha vyombo vimesahihishwa, calibration imefanywa, na data
        imeandikwa kwa usahihi.

        3. Viwango na taratibu
        Hesabu zinafuata kanuni za AASHTO T 191 / ASTM D1556 na zinalinganishwa
        na viwango vya mradi kama BS 1377: Part 9 / CML 1.9 na Table 7205/1
        (SSRW 2000) pale vinapotumika. Ni wajibu wako kuthibitisha kwamba njia
        na kigezo cha kukubali (Xs/Xmin) vinakubaliana na specification ya
        mradi wako.

        4. Hakuna dhamana
        Programu inatolewa "kama ilivyo". Haidhamini kwamba matokeo yatakidhi
        mahitaji yoyote ya kisheria, ya kandarasi, au ya maabara.

        5. Kizuizi cha dhima
        Kwa kiwango kinachoruhusiwa na sheria, Onesmo Komba na washiriki wa
        mradi hawatawajibika kwa hasara, gharama, au uamuzi wowote unaotokana
        na matumizi ya matokeo ya programu hii bila uhakiki wa mhandisi au
        maabara.

        6. Matumizi ya kitaaluma
        Usitumie programu hii kama ushahidi pekee wa kukubali au kukataliwa
        kwa kazi, malipo, au uidhinishaji wa mradi bila uhakiki wa mamlaka
        husika.
    """.trimIndent()

    private val TERMS_EN = """
        TERMS OF SERVICE — FDT CALCULATOR
        Effective 21 November 2026

        1. Acceptance
        By opening or using FDT Calculator, you accept these terms, the Privacy
        Policy and the Disclaimer. If you do not agree, do not use the app.

        2. Licence
        You are granted a personal, non-transferable, non-exclusive licence to
        use the application on your device for lawful engineering work. You may
        not sell, rent or commercially distribute it without permission.

        3. User responsibilities
        - Enter accurate data and ensure instruments are calibrated.
        - Do not use the application for unlawful or harmful purposes.
        - Review results before including them in formal reports.
        - Keep your own backups; they are stored on your device, not our server.

        4. Intellectual property
        The calculation engine, application design and documentation belong to
        the project. Source code is available on GitHub under the repository
        licence. Client, contractor and consultant names and logos remain the
        property of their owners.

        5. No warranty and limitation of liability
        As stated in the Disclaimer, the application is provided without
        warranty. We are not liable for losses arising from its use beyond the
        extent permitted by law.

        6. Changes
        We may improve formulas, the interface or the PDF layout. Significant
        changes will be published in a new version with a new date.

        7. Termination
        You may stop using the application at any time and delete all data
        stored on your device.

        8. Governing law and contact
        The laws of the territory where you use the application apply.
        Questions: https://github.com/Onesmo3/FDTCalculator (open an Issue).
    """.trimIndent()

    private val TERMS_SW = """
        TERMS OF SERVICE — FDT CALCULATOR
        Inatumika tarehe 21 Novemba 2026

        1. Kukubaliwa
        Kwa kufungua au kutumia FDT Calculator, unakubali sheria hizi, Privacy
        Policy na Disclaimer. Kama hukubali, usitumie programu.

        2. Leseni ya matumizi
        Unapewa leseni ya kibinafsi, isiyohamishwa na isiyo ya kipekee ya
        kutumia programu kwenye kifaa chako kwa madhumuni halali ya uhandisi.
        Huna haki ya kuuza, kukodisha, au kusambaza kwa njia ya kibiashara bila
        idhini ya mmiliki wa mradi.

        3. Majukumu ya mtumiaji
        - Kuingiza data sahihi na kuhakikisha vyombo vimepimwa (calibrated).
        - Kutotumia programu kwa madhumuni yaliyokatazwa na sheria au
          yanayoweza kusababisha madhara.
        - Kukagua matokeo kabla ya kuyatuma au kuyategemea kwenye ripoti rasmi.
        - Kutunza backup zako mwenyewe; huhifadhiwa kwenye kifaa chako.

        4. Mali ya kiakili
        Kanuni za hesabu, muundo wa programu na nyaraka ni mali ya mradi huu.
        Chanzo kinapatikana kwenye GitHub chini ya leseni iliyotajwa kwenye
        repository. Nembo na majina ya client, contractor au consultant ni
        mali ya wenyewe na hayahamishwi kwako.

        5. Hakuna dhamana na kizuizi cha dhima
        Kama ilivyoelezwa kwenye Disclaimer, programu inatolewa bila dhamana.
        Hatuwajibiki kwa hasara inayotokana na matumizi yake zaidi ya kipimo
        kinachoruhusiwa na sheria.

        6. Mabadiliko
        Tunaweza kuboresha formulas, muundo wa kiolesura au mpangilio wa PDF.
        Mabadiliko makubwa yataonyeshwa kwenye toleo jipya pamoja na tarehe.

        7. Kusitisha matumizi
        Unaweza kuacha kutumia programu wakati wowote na kufuta data yote
        iliyohifadhiwa kwenye kifaa chako.

        8. Sheria inayotumika na mawasiliano
        Sheria zinazotumika ni zile za eneo unalotumia programu. Maswali:
        https://github.com/Onesmo3/FDTCalculator (fungua Issue).
    """.trimIndent()

    private val COPYRIGHT_EN = """
        © 2026 Onesmo (GitHub: Onesmo3). All rights reserved.

        The calculation rules were derived from the original Qt/C++ Field
        Density Test Calculator, ported to Kotlin for Android. Company names
        and logos remain the property of their respective owners.
    """.trimIndent()

    private val COPYRIGHT_SW = """
        © 2026 Onesmo (GitHub: Onesmo3). Haki zote zimehifadhiwa.

        Kanuni za hesabu zilitokana na utekelezaji wa awali wa Qt/C++ wa
        Field Density Test Calculator, uliobadilishwa kuwa Kotlin kwa Android.
        Nembo na majina ya kampuni ni mali ya wenyewe.
    """.trimIndent()

    private val CONTACT_EN = """
        GitHub: https://github.com/Onesmo3/FDTCalculator

        For improvements, bug reports, or feedback on the PDF report layout,
        please open an issue in that repository.
    """.trimIndent()

    private val CONTACT_SW = """
        GitHub: https://github.com/Onesmo3/FDTCalculator

        Kwa masuala ya kuboresha programu, ripoti ya hitilafu, au maoni kuhusu
        muundo wa ripoti ya PDF, tafadhali fungua issue kwenye repository hiyo.
    """.trimIndent()

    private val ABOUT_EN = """
        FDT Calculator — Sand Cone Method.

        An Android application for computing field density by the sand cone
        method: single point, multi-hole worksheet (Xn/Xo/Xs/Xmin), local
        storage, backup/restore, and a PDF report following the original Qt
        layout.

        The engine lives in a separate Kotlin module with no Android dependency,
        so calculations can be tested before distribution.
    """.trimIndent()

    private val ABOUT_SW = """
        FDT Calculator — Sand Cone Method.

        Programu ya Android ya kukokotoa field density kwa njia ya mchanga:
        single point, worksheet yenye holes nyingi (Xn/Xo/Xs/Xmin),
        hifadhi ya ndani, backup/restore, na ripoti ya PDF inayofuata
        mpangilio wa toleo la Qt.

        Engine iko kwenye module tofauti ya Kotlin isiyo tegemea Android,
        ili hesabu ziweze kujaribiwa kabla ya kusambazwa.
    """.trimIndent()
}
