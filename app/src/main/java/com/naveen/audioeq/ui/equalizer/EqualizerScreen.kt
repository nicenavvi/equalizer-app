package com.naveen.audioeq.ui.equalizer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun EqualizerScreen(viewModel: EqualizerViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("10-Band Equalizer", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = { viewModel.resetAll() }) { Text("Reset") }
        }

        if (!state.isSupported) {
            Spacer(Modifier.height(16.dp))
            Text(
                "⚠ Global EQ isn't supported on this device — sliders won't audibly change system audio.",
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(8.dp))

        // Band sliders row
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            state.bandLevels.forEachIndexed { index, level ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxHeight().weight(1f)
                ) {
                    Text("%.0f".format(level), style = MaterialTheme.typography.labelSmall)

                    Box(
                        modifier = Modifier.weight(1f).width(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Slider(
                            value = level,
                            onValueChange = { viewModel.setBandLevel(index, it) },
                            valueRange = state.minLevel..state.maxLevel,
                            modifier = Modifier
                                .graphicsLayer { rotationZ = -90f }
                                .width(160.dp)
                        )
                    }

                    Text(viewModel.bandLabels[index], style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Divider()
        Spacer(Modifier.height(16.dp))

        Text("Bass Boost", style = MaterialTheme.typography.titleMedium)
        Slider(
            value = state.bassBoost,
            onValueChange = { viewModel.setBassBoost(it) },
            valueRange = 0f..100f
        )
        Text("${state.bassBoost.toInt()}%")

        Spacer(Modifier.height(16.dp))

        Text("Virtualizer (3D / Surround)", style = MaterialTheme.typography.titleMedium)
        Slider(
            value = state.virtualizer,
            onValueChange = { viewModel.setVirtualizer(it) },
            valueRange = 0f..100f
        )
        Text("${state.virtualizer.toInt()}%")
    }
}
