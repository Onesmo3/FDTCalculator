# FDT Calculator

Programu ya Android ya **Field Density Test (Sand Cone Method)**, iliyotokana na
engine ya Qt/C++ (`FieldDensityTestCalculator`) na toleo la awali la PWA la repo hii.
Jina rasmi ni **FDT Calculator**.

## Vipengele

- `:engine` — pure Kotlin calculation core, huru kutoka Android (AASHTO T 191 / ASTM D1556).
- `:app` — Android UI yenye design system ya ndani (`Ui.kt`).
- Single point: hesabu ya density/compaction, inline validation, one-tap PDF.
- Worksheet: holes nyingi → **Xn / Xo / Xs / Xmin** na Pass/Fail.
- Records: kuhifadhi, kuhariri, kufuta kwa Undo, backup/restore JSON.
- PDF reports: mpangilio unaofuata `fdtpdf.cpp` ya Qt, client logo, contractor/consultant,
  na jina la faili linalojumuisha **location + layer**.
- Cylinder presets zinazotumika kwenye single point na worksheet.
- Engine self-test: **7/7 JUnit** + **36/36 golden checks** kwenye kifaa.

## Muundo

```
settings.gradle.kts, build.gradle.kts, gradle.properties
gradlew, gradle/wrapper/          # Gradle 9.9.1
run-tests.sh                      # :engine:test
build-apk.sh                      # :app:assembleDebug
engine/                           # pure Kotlin formulas + FdtRecord model
app/                              # Android 26+, target 35, AGP 9.5.1
pwa-archive/                      # toleo la awali la HTML/JS (archive)
```

## Kujenga

```sh
./run-tests.sh     # 7/7 PASS inatarajiwa
./build-apk.sh     # APK: app/build/outputs/apk/debug/app-debug.apk
```

Kwenye Termux unaweza pia kutumia `./gradle-local.sh ...`; script hii sasa inatumia
`./gradlew` kwanza, kisha `gradle` ya system, bila njia ngumu za ndani.

## Uhamiaji wa repo

Hadi kutolewa kwa idhini ya mwisho, repo hii ilikuwa PWA (HTML/JS). Historia yote
imehifadhiwa. Kabla ya kubadilisha yaliyomo, alama **``pwa-final``** ilitolewa
kwenye commit ya mwisho ya PWA (`63bd949`). Chanzo cha PWA kimehifadhiwa pia kwenye
`pwa-archive/`. Uhamiaji ulifanywa kwa commit ya kawaida — hakuna force-push.

```sh
git show pwa-final
git show pwa-final:index.html
```

## Leseni

Angalia `LICENSE`.
