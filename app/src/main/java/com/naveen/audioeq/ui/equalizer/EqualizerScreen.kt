package com.naveen.audioeq.ui.equalizer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.roundToInt

/**
 * ఈక్వలైజర్ స్క్రీన్.
 *
 * ఫిక్స్ చేసిన పాయింట్లు:
 * 1. ప్రతి బ్యాండ్ స్లయిడర్ కింద దాని ఫ్రీక్వెన్సీ పేరు + శాతం (ఉదా: "31Hz · 20%") చూపిస్తుంది.
 * 2. Bass Boost, Virtualizer స్లయిడర్ల కింద కూడా అదే పేరు + శాతం చూపిస్తుంది.
 * 3. డీఫాల్ట్‌గా (యాప్ మొదటిసారి తెరిచినప్పుడు) అన్ని విలువలూ 0% / మధ్యస్థంగా ఉంటాయి —
 *    అంటే సాంగ్ ఒరిజినల్ సౌండ్‌లోనే ప్లే అవుతుంది. స్లయిడర్ కదిలించినప్పుడు మాత్రమే మారుతుంది.
 * 4. ఇంజిన్ సపోర్ట్ చేయకపోతే ("ఈ డివైజ్‌లో సపోర్ట్ లేదు") మెసేజ్ చూపించి,
 *    స్లయిడర్లు డిసేబుల్ అవుతాయి — క్రాష్ అవ్వదు.
 */
@Composable
fun EqualizerScreen(
    viewModel: EqualizerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val bandLabels = viewModel.bandLabels

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "10-Band Equalizer",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (!uiState.isSupported) {
            // ----- సపోర్ట్ లేని డివైజ్ కేసు -----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.errorMessage
                        ?: "ఈ డివైజ్‌లో గ్లోబల్ ఈక్వలైజర్ సపోర్ట్ లేదు",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
            return@Column
        }

        // ----- 10 బ్యాండ్ స్లయిడర్లు (ప్రతి దాని కింద ఫ్రీక్వెన్సీ పేరు + శాతం) -----
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            uiState.bandLevels.forEachIndexed { index, level ->
                val percent = levelToPercent(level, uiState.minLevel, uiState.maxLevel)
                BandSliderColumn(
                    label = bandLabels.getOrElse(index) { "" },
                    percent = percent,
                    value = level,
                    valueRange = uiState.minLevel..uiState.maxLevel,
                    onValueChange = { newValue ->
                        viewModel.setBandLevel(index, newValue)
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Divider()
        }

        // ----- Bass Boost -----
        LabeledHorizontalSlider(
            label = "Bass Boost",
            percent = (uiState.bassBoost * 100).roundToInt(),
            value = uiState.bassBoost,
            valueRange = 0f..100f,
            onValueChange = { viewModel.setBassBoost(it) }
        )

        // ----- Virtualizer -----
        LabeledHorizontalSlider(
            label = "Virtualizer (3D / Surround)",
            percent = (uiState.virtualizer * 100).roundToInt(),
            value = uiState.virtualizer,
            valueRange = 0f..100f,
            onValueChange = { viewModel.setVirtualizer(it) }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = { viewModel.resetAll() }) {
                Text("Reset to Default")
            }
        }
    }
}

/**
 * ఒక బ్యాండ్ (వర్టికల్ స్లయిడర్ లాంటి రౌండ్ నాబ్) + దాని కింద పేరు మరియు శాతం.
 * Compose లో native vertical slider లేదు కాబట్టి, ఇది graphicsLayer rotate
 * trick తో సాధించబడింది — ఇది మీ ఒరిజినల్ UI స్క్రీన్‌షాట్‌లో కనిపించిన
 * "రౌండ్ నాబ్‌ల వరుస" డిజైన్‌కి సరిపోతుంది.
 */
@Composable
private fun BandSliderColumn(
    label: String,
    percent: Int,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(34.dp)
    ) {
        Box(
            modifier = Modifier
                .height(160.dp)
                .graphicsLayer { rotationZ = -90f },
            contentAlignment = Alignment.Center
        ) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                modifier = Modifier.width(160.dp)
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = "$percent%",
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Bass Boost / Virtualizer లాంటి హారిజాంటల్ స్లయిడర్‌లకు —
 * పైన లేబుల్, స్లయిడర్, కింద శాతం టెక్స్ట్.
 */
@Composable
private fun LabeledHorizontalSlider(
    label: String,
    percent: Int,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
        Text(
            text = "$percent%",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/** dB విలువని 0-100% స్కేల్‌కి మార్చడానికి సాధారణ లినియర్ మ్యాపింగ్. */
private fun levelToPercent(level: Float, min: Float, max: Float): Int {
    if (max <= min) return 0
    val ratio = (level - min) / (max - min)
    return (ratio * 100f).roundToInt().coerceIn(0, 100)
}
