package com.naveen.audioeq.core

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Attaches Android AudioEffects to the global output mix (session 0).
 * This is the officially documented technique used by apps like Wavelet/Poweramp
 * to apply EQ to ALL audio on the device (Spotify, YouTube, Chrome, etc.)
 * without root, via android.media.audiofx.* with sessionId = 0.
 */
@Singleton
class GlobalAudioSessionEngine @Inject constructor() {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var presetReverb: PresetReverb? = null

    var isEnabled: Boolean = false
        private set

    var isSupported: Boolean = true
        private set

    fun start() {
        try {
            equalizer = Equalizer(0, 0).apply { enabled = true }
            bassBoost = BassBoost(0, 0).apply { enabled = true }
            virtualizer = Virtualizer(0, 0).apply { enabled = true }
            presetReverb = PresetReverb(0, 0).apply { enabled = true }
            isEnabled = true
            isSupported = true
        } catch (e: Exception) {
            // Some OEMs (Samsung/MIUI) or DRM-protected audio paths block session 0 effects.
            Log.e("GlobalAudioEngine", "Global session unsupported on this device", e)
            isSupported = false
            isEnabled = false
        }
    }

    /** Starts the engine if it isn't already running (used by screens that need live effects). */
    fun ensureStarted() {
        if (equalizer == null) start()
    }

    fun stop() {
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
        presetReverb?.release()
        equalizer = null
        bassBoost = null
        virtualizer = null
        presetReverb = null
        isEnabled = false
    }

    fun setBandLevel(band: Short, levelMillibels: Short) {
        equalizer?.setBandLevel(band, levelMillibels)
    }

    fun getNumberOfBands(): Short = equalizer?.numberOfBands ?: 0

    fun getBandFreqRange(band: Short): IntArray =
        equalizer?.getBandFreqRange(band) ?: intArrayOf(0, 0)

    fun getBandLevel(band: Short): Short = equalizer?.getBandLevel(band) ?: 0

    fun getMinBandLevel(): Short = equalizer?.bandLevelRange?.getOrNull(0) ?: -1500

    fun getMaxBandLevel(): Short = equalizer?.bandLevelRange?.getOrNull(1) ?: 1500

    fun setBassBoostStrength(strength: Short) {
        bassBoost?.setStrength(strength)
    }

    fun setVirtualizerStrength(strength: Short) {
        virtualizer?.setStrength(strength)
    }

    fun setReverbPreset(preset: Short) {
        presetReverb?.preset = preset
    }
}
