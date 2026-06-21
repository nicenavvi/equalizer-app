package com.naveen.audioeq.core

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.util.Log

/**
 * గ్లోబల్ (సెషన్ 0) ఆడియో ఎఫెక్ట్స్ ఇంజిన్.
 *
 * ఫిక్స్ చేసిన పాయింట్లు:
 * 1. ప్రతి ఎఫెక్ట్ (Equalizer / BassBoost / Virtualizer / PresetReverb) విడివిడిగా
 *    try-catch లో wrap చేయబడింది. ఒక ఎఫెక్ట్ ఈ డివైజ్‌లో సపోర్ట్ కాకపోయినా,
 *    మిగతా ఎఫెక్ట్‌లు పనిచేస్తూనే ఉంటాయి — మొత్తం engine క్రాష్ కాదు.
 * 2. setBandLevel / setBassBoostStrength / setVirtualizerStrength / setReverbPreset
 *    లాంటి runtime కాల్స్ కూడా try-catch తో wrap చేయబడ్డాయి, ఎందుకంటే ఎఫెక్ట్ object
 *    null కాకపోయినా, దాని మీద కాల్ చేసేటప్పుడు RuntimeException వచ్చే అవకాశం ఉంది
 *    (కొన్ని OEM custom ROMs లో ఇది జరుగుతుంది).
 * 3. isSupported ఇప్పుడు "కనీసం ఒక ఎఫెక్ట్ అయినా పనిచేసిందా" అనే దాన్ని సూచిస్తుంది,
 *    కాబట్టి పాక్షికంగా సపోర్ట్ ఉన్న డివైజ్‌లలో కూడా యాప్ ఉపయోగపడుతుంది.
 */
class GlobalAudioSessionEngine {

    companion object {
        private const val TAG = "GlobalAudioEngine"
        private const val GLOBAL_SESSION_ID = 0

        // ఎఫెక్ట్ priority ని 0 నుండి పెంచితే, OS మన ఎఫెక్ట్‌కి ఎక్కువ ప్రాధాన్యత
        // ఇచ్చి సరిగ్గా apply చేసే అవకాశం పెరుగుతుంది. priority ఎక్కువైతే,
        // అదే సెషన్‌లో వేరే యాప్ ఎఫెక్ట్ తో conflict వచ్చినప్పుడు మనదే గెలుస్తుంది.
        private const val EFFECT_PRIORITY = 100
    }

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var presetReverb: PresetReverb? = null

    var isEnabled: Boolean = false
        private set

    var isSupported: Boolean = false
        private set

    /** equalizer ఇంకా క్రియేట్ కాకపోతే మాత్రమే start() ని కాల్ చేస్తుంది. */
    fun ensureStarted() {
        if (equalizer == null) {
            start()
        }
    }

    fun start() {
        var anySucceeded = false

        // ---- Equalizer ----
        try {
            val eq = Equalizer(EFFECT_PRIORITY, GLOBAL_SESSION_ID)
            eq.enabled = true
            equalizer = eq
            anySucceeded = true
        } catch (t: Throwable) {
            Log.e(TAG, "Equalizer unsupported on this device", t)
            equalizer = null
        }

        // ---- BassBoost ----
        try {
            val bb = BassBoost(EFFECT_PRIORITY, GLOBAL_SESSION_ID)
            bb.enabled = true
            bassBoost = bb
            anySucceeded = true
        } catch (t: Throwable) {
            Log.e(TAG, "BassBoost unsupported on this device", t)
            bassBoost = null
        }

        // ---- Virtualizer ----
        try {
            val vr = Virtualizer(EFFECT_PRIORITY, GLOBAL_SESSION_ID)
            vr.enabled = true
            virtualizer = vr
            anySucceeded = true
        } catch (t: Throwable) {
            Log.e(TAG, "Virtualizer unsupported on this device", t)
            virtualizer = null
        }

        // ---- PresetReverb ----
        try {
            val pr = PresetReverb(EFFECT_PRIORITY, GLOBAL_SESSION_ID)
            pr.enabled = true
            presetReverb = pr
            anySucceeded = true
        } catch (t: Throwable) {
            Log.e(TAG, "PresetReverb unsupported on this device", t)
            presetReverb = null
        }

        isSupported = anySucceeded
        isEnabled = anySucceeded

        if (!anySucceeded) {
            Log.e(TAG, "Global session unsupported on this device — no effect could be created")
        }
    }

    fun stop() {
        safeRelease("Equalizer") { equalizer?.release() }
        safeRelease("BassBoost") { bassBoost?.release() }
        safeRelease("Virtualizer") { virtualizer?.release() }
        safeRelease("PresetReverb") { presetReverb?.release() }

        equalizer = null
        bassBoost = null
        virtualizer = null
        presetReverb = null
        isEnabled = false
    }

    fun getMinBandLevel(): Short {
        return try {
            equalizer?.bandLevelRange?.getOrNull(0) ?: -1500
        } catch (t: Throwable) {
            Log.e(TAG, "getMinBandLevel failed", t)
            -1500
        }
    }

    fun getMaxBandLevel(): Short {
        return try {
            equalizer?.bandLevelRange?.getOrNull(1) ?: 1500
        } catch (t: Throwable) {
            Log.e(TAG, "getMaxBandLevel failed", t)
            1500
        }
    }

    fun getNumberOfBands(): Short {
        return try {
            equalizer?.numberOfBands ?: 0
        } catch (t: Throwable) {
            Log.e(TAG, "getNumberOfBands failed", t)
            0
        }
    }

    fun getBandFreqRange(band: Short): IntArray {
        return try {
            equalizer?.getBandFreqRange(band) ?: intArrayOf(0, 0)
        } catch (t: Throwable) {
            Log.e(TAG, "getBandFreqRange failed", t)
            intArrayOf(0, 0)
        }
    }

    fun getBandLevel(band: Short): Short {
        return try {
            equalizer?.getBandLevel(band) ?: 0
        } catch (t: Throwable) {
            Log.e(TAG, "getBandLevel failed", t)
            0
        }
    }

    fun setBandLevel(band: Short, level: Short) {
        try {
            equalizer?.setBandLevel(band, level)
        } catch (t: Throwable) {
            Log.e(TAG, "setBandLevel failed", t)
        }
    }

    fun setBassBoostStrength(strength: Short) {
        try {
            bassBoost?.setStrength(strength)
        } catch (t: Throwable) {
            Log.e(TAG, "setBassBoostStrength failed", t)
        }
    }

    fun setVirtualizerStrength(strength: Short) {
        try {
            virtualizer?.setStrength(strength)
        } catch (t: Throwable) {
            Log.e(TAG, "setVirtualizerStrength failed", t)
        }
    }

    fun setReverbPreset(preset: Short) {
        try {
            presetReverb?.preset = preset
        } catch (t: Throwable) {
            Log.e(TAG, "setReverbPreset failed", t)
        }
    }

    private inline fun safeRelease(label: String, block: () -> Unit) {
        try {
            block()
        } catch (t: Throwable) {
            Log.e(TAG, "$label release failed", t)
        }
    }
}
