package com.naveen.audioeq.core

import android.media.audiofx.Equalizer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One-shot test: tries to attach a throwaway Equalizer to session 0.
 * If it fails, we tell the user honestly that global EQ is restricted
 * on their device (instead of pretending it always works).
 */
@Singleton
class EffectCompatibilityChecker @Inject constructor() {

    fun isGlobalEqSupported(): Boolean {
        return try {
            val test = Equalizer(0, 0)
            test.release()
            true
        } catch (e: Exception) {
            false
        }
    }
}
