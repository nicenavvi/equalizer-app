# Audio Equalizer (Global System-Wide EQ)

A Kotlin/Compose Android app that applies EQ, Bass Boost, and Virtualizer effects
to the device's global output mix (session 0) — affecting Spotify, YouTube, Chrome,
and any other app's audio, without root.

## What's included in this build
- Home screen with Master ON/OFF switch
- `GlobalAudioSessionEngine` — attaches `Equalizer/BassBoost/Virtualizer/PresetReverb` to session 0
- Foreground service (`AudioSessionListenerService`) keeping the engine alive
- `EffectCompatibilityChecker` — honestly detects when a device (e.g. Samsung/MIUI) blocks session 0
- Hilt DI wiring, Gradle Wrapper, and a GitHub Actions workflow that auto-builds a debug APK

## How to get an APK without Android Studio
1. Create a new GitHub repository.
2. Upload **all files in this zip** (including the hidden `.github` folder and `gradlew`) to the repo root.
3. Go to the repo's **Actions** tab — a "Build APK" workflow run will start automatically.
4. When it finishes (green check), open the run → scroll to **Artifacts** → download `app-debug-apk`.
5. Unzip it, transfer `app-debug.apk` to your phone, and install (allow "install from unknown sources").

## Known limitations (by design, not a bug)
- Some OEM skins (Samsung, MIUI) block session-0 effects when their own sound enhancer is active.
- DRM-protected audio paths in some apps may not be affected.
- Bluetooth codecs with hardware offload (aptX/LDAC) bypass software EQ.

This is the **foundation module** (Home + Engine + Service). Equalizer band sliders, Presets,
Settings, and AI suggestion screens are placeholder stubs ready to be filled in next.
