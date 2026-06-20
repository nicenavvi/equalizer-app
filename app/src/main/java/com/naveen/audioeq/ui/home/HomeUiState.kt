package com.naveen.audioeq.ui.home

data class HomeUiState(
    val masterEnabled: Boolean = false,
    val isSupported: Boolean = true,
    val activePresetName: String = "Normal",
    val statusText: String = "OFF"
)
