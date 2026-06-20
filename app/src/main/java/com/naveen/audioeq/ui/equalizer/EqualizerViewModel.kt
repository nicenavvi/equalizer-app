package com.naveen.audioeq.ui.equalizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveen.audioeq.core.EqConstants
import com.naveen.audioeq.core.GlobalAudioSessionEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EqualizerUiState(
    val isSupported: Boolean = true,
    val minLevel: Float = -15f,   // dB
    val maxLevel: Float = 15f,    // dB
    val bandLevels: List<Float> = List(10) { 0f },   // dB per band
    val bassBoost: Float = 0f,    // 0-100
    val virtualizer: Float = 0f   // 0-100
)

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val engine: GlobalAudioSessionEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(EqualizerUiState())
    val uiState: StateFlow<EqualizerUiState> = _uiState

    val bandLabels = EqConstants.BAND_LABELS

    init {
        viewModelScope.launch {
            engine.ensureStarted()
            if (!engine.isSupported) {
                _uiState.update { it.copy(isSupported = false) }
                return@launch
            }
            val minMb = engine.getMinBandLevel()
            val maxMb = engine.getMaxBandLevel()
            val currentLevels = (0 until 10).map { i ->
                engine.getBandLevel(i.toShort()) / 100f
            }
            _uiState.update {
                it.copy(
                    minLevel = minMb / 100f,
                    maxLevel = maxMb / 100f,
                    bandLevels = currentLevels
                )
            }
        }
    }

    /** levelDb is in dB (e.g. -15.0 to 15.0); engine expects millibels (dB * 100). */
    fun setBandLevel(band: Int, levelDb: Float) {
        engine.setBandLevel(band.toShort(), (levelDb * 100).toInt().toShort())
        _uiState.update { state ->
            val updated = state.bandLevels.toMutableList()
            updated[band] = levelDb
            state.copy(bandLevels = updated)
        }
    }

    /** strength is 0-100 as shown on the slider; AudioEffect strength range is 0-1000. */
    fun setBassBoost(strength: Float) {
        engine.setBassBoostStrength((strength * 10).toInt().toShort())
        _uiState.update { it.copy(bassBoost = strength) }
    }

    fun setVirtualizer(strength: Float) {
        engine.setVirtualizerStrength((strength * 10).toInt().toShort())
        _uiState.update { it.copy(virtualizer = strength) }
    }

    fun resetAll() {
        for (i in 0 until 10) setBandLevel(i, 0f)
        setBassBoost(0f)
        setVirtualizer(0f)
    }
}
