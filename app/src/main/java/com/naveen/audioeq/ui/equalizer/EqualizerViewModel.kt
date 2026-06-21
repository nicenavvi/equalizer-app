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
    val minLevel: Float = -15f,
    val maxLevel: Float = 15f,
    val bandLevels: List<Float> = List(10) { 0f },
    val bassBoost: Float = 0f,
    val virtualizer: Float = 0f,
    /**
     * కొత్తగా చేర్చబడిన ఫీల్డ్: ఏదైనా ఎర్రర్ వచ్చినప్పుడు యూజర్‌కి చూపించడానికి
     * ఒక సింపుల్ మెసేజ్. ఇది null అయితే ఎర్రర్ లేదని అర్థం.
     */
    val errorMessage: String? = null
)

@HiltViewModel
class EqualizerViewModel @Inject constructor() : ViewModel() {

    private val engine = GlobalAudioSessionEngine()

    private val _uiState = MutableStateFlow(EqualizerUiState())
    val uiState: StateFlow<EqualizerUiState> = _uiState

    val bandLabels: List<String> = EqConstants.BAND_LABELS

    init {
        viewModelScope.launch {
            // ఇంజిన్ స్టార్ట్ చేసే ప్రాసెస్ మొత్తం ఇప్పుడు try-catch తో రక్షించబడింది.
            // ఇంజిన్ లోపలే ప్రతి AudioEffect విడివిడిగా wrap చేయబడింది (GlobalAudioSessionEngine.kt చూడండి),
            // కానీ ఇక్కడ కూడా ఒక అదనపు safety net పెట్టాం — ఏ unexpected exception వచ్చినా
            // యాప్ క్రాష్ అవ్వకుండా, యూజర్‌కి అర్థమయ్యే మెసేజ్ చూపించి ఆగిపోతుంది.
            try {
                engine.ensureStarted()

                if (!engine.isSupported) {
                    _uiState.update {
                        it.copy(
                            isSupported = false,
                            errorMessage = "ఈ డివైజ్‌లో గ్లోబల్ ఈక్వలైజర్ సపోర్ట్ లేదు"
                        )
                    }
                    return@launch
                }

                val min = engine.getMinBandLevel()
                val max = engine.getMaxBandLevel()

                val levels = (0 until 10).map { index ->
                    engine.getBandLevel(index.toShort()) / 100f
                }

                _uiState.update {
                    it.copy(
                        isSupported = true,
                        minLevel = min / 100f,
                        maxLevel = max / 100f,
                        bandLevels = levels,
                        errorMessage = null
                    )
                }
            } catch (t: Throwable) {
                // ఇది చివరి safety net. ఇక్కడికి వస్తే అర్థం ఏంటంటే ఇంజిన్‌లో
                // మనం ఊహించని ఏదైనా exception వచ్చిందని — అయినా యాప్ క్రాష్ అవ్వదు,
                // బదులుగా "సపోర్ట్ లేదు" స్టేట్ చూపిస్తుంది.
                _uiState.update {
                    it.copy(
                        isSupported = false,
                        errorMessage = "ఈక్వలైజర్ లోడ్ చేయడంలో ఎర్రర్ వచ్చింది: ${t.message ?: "తెలియని ఎర్రర్"}"
                    )
                }
            }
        }
    }

    fun setBandLevel(index: Int, value: Float) {
        if (!_uiState.value.isSupported) return
        try {
            engine.setBandLevel(index.toShort(), (value * 100).toInt().toShort())
        } catch (t: Throwable) {
            // setBandLevel ఇంజిన్ లోపలే safe గా ఉంది, కానీ UI స్టేట్ అప్‌డేట్ లాజిక్
            // ఇక్కడ ఉంది కాబట్టి దీన్నీ wrap చేస్తున్నాం.
            return
        }
        _uiState.update { current ->
            val updated = current.bandLevels.toMutableList()
            updated[index] = value
            current.copy(bandLevels = updated)
        }
    }

    fun setBassBoost(value: Float) {
        if (!_uiState.value.isSupported) return
        try {
            engine.setBassBoostStrength((value * 10).toInt().toShort())
        } catch (t: Throwable) {
            return
        }
        _uiState.update { it.copy(bassBoost = value) }
    }

    fun setVirtualizer(value: Float) {
        if (!_uiState.value.isSupported) return
        try {
            engine.setVirtualizerStrength((value * 10).toInt().toShort())
        } catch (t: Throwable) {
            return
        }
        _uiState.update { it.copy(virtualizer = value) }
    }

    fun resetAll() {
        if (!_uiState.value.isSupported) return
        for (i in 0 until 10) {
            setBandLevel(i, 0f)
        }
        setBassBoost(0f)
        setVirtualizer(0f)
    }

    override fun onCleared() {
        super.onCleared()
        // స్క్రీన్ నుండి బయటికి వెళ్ళినప్పుడు ఎఫెక్ట్స్ ని విడిచిపెట్టడం వల్ల
        // మెమొరీ లీక్స్ మరియు stale AudioEffect handles ఉండవు.
        engine.stop()
    }
}
