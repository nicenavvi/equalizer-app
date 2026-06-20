package com.naveen.audioeq.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Global Equalizer", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        Switch(
            checked = state.masterEnabled,
            onCheckedChange = { viewModel.toggleMaster(it) }
        )

        Spacer(Modifier.height(12.dp))
        Text("Status: ${state.statusText}")
        Text("Active Preset: ${state.activePresetName}")

        if (!state.isSupported) {
            Spacer(Modifier.height(24.dp))
            Text(
                "⚠ This device may restrict system-wide audio effects (OEM sound enhancer conflict).",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
