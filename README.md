# NoGboardBottomInset v2 - Modulo LSPosed per Gboard

Versione 2.0: Hook su **InputMethodService** (classe pubblica del framework Android),
NON su classi offuscate di Gboard. Questo rende il modulo stabile tra le build.

## Build verificata
- Gboard: 18.0.3.954559732-beta-arm64-v8a
- Android: 16 QPR (Pixel 11 Pro)

## Come compilare

1. **Scarica XposedBridgeApi-82.jar** da https://mvnrepository.com/artifact/de.robv.android.xposed/api/82
2. **Crea la cartella** `app/libs/` e mettici dentro `XposedBridgeApi-82.jar`
3. **Apri il progetto** in Android Studio
4. **Build → Clean Project**, poi **Build → Rebuild Project**
   oppure: `.\gradlew.bat clean assembleDebug`

## Come installare

1. Disinstalla eventuali versioni precedenti:
   ```
   ./adb uninstall com.example.nogboardinset
   ```
2. Installa la nuova versione:
   ```
   ./adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
3. Apri Vector/LSPosed Manager → Moduli → NoGboardBottomInset v2
4. **Scope: SELEZIONA SOLO** `com.google.android.inputmethod.latin` (Gboard)
5. Attiva il modulo
6. **RIAVVIA** il telefono

## Cosa fa questa versione

1. **`canImeRenderGesturalNavButtons()` → FALSE**: impedisce a Gboard di disegnare
   i suoi bottoni di navigazione custom (freccia/globo/pill interna)
2. **`onComputeInsets()`**: logga i valori degli inset per diagnosi
3. **`onCreateInputView()`**: logga il `paddingBottom` della view radice
4. **`View.setPadding()` safety-net**: azzera qualsiasi padding bottom > 0
5. **`onCustomImeSwitcherButtonRequestedVisible()`**: se presente su InputMethodService,
   lo forza a false

## Debug

Dopo il riavvio, apri un campo di testo e lancia:
```
./adb logcat -d | Select-String -Pattern "NoGboard" -CaseSensitive:$false
```

Devi vedere almeno:
```
NoGboardBottomInset: === GBOARD CARICATO === package=...
NoGboardBottomInset: hook su InputMethodService.canImeRenderGesturalNavButtons() installato
NoGboardBottomInset: onCreateInputView -> ... paddingBottom=XXX
```

Se vedi `paddingBottom=XXX` con XXX > 0, il safety-net lo azzera.

## Per pill/globo/freccia di sistema

Questo modulo gestisce solo Gboard. Per nascondere la pill di sistema:
- Usa PixelXpert (che già hai) o NavTweaks (modulo Magisk separato)

## Note

I nomi dei metodi hookati (`canImeRenderGesturalNavButtons`, `onComputeInsets`, ecc.)
sono API **pubbliche del framework Android**, non offuscate. Questo modulo dovrebbe
funzionare su qualsiasi build di Gboard che estende InputMethodService.
