package com.naveen.audioeq.ui.home

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.naveen.audioeq.core.EffectCompatibilityChecker
import com.naveen.audioeq.service.AudioSessionListenerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val compatibilityChecker: EffectCompatibilityChecker
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        viewModelScope.launch {
            val supported = compatibilityChecker.isGlobalEqSupported()
            _uiState.update { it.copy(isSupported = supported) }
        }
    }

    fun toggleMaster(enabled: Boolean) {
        val context = getApplication<Application>()
        val intent = Intent(context, AudioSessionListenerService::class.java)
        if (enabled) {
            context.startForegroundService(intent)
        } else {
            context.stopService(intent)
        }
        _uiState.update {
            it.copy(masterEnabled = enabled, statusText = if (enabled) "ON" else "OFF")
        }
    }
}
