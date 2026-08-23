package com.example.a2.data.health

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeHealthServicesRepository {
    private val _heartRateState = MutableStateFlow<HeartRateState>(HeartRateState.Waiting)
    val heartRateState: StateFlow<HeartRateState> = _heartRateState.asStateFlow()

    var isStarted = false
    var hasPermission = true
    var isSupported = true
    var registrationCount = 0
    var startDelayMs = 0L

    suspend fun start() {
        if (isStarted) return
        if (startDelayMs > 0) delay(startDelayMs)
        
        if (!hasPermission) {
            _heartRateState.update { HeartRateState.PermissionRequired }
            return
        }
        if (!isSupported) {
            _heartRateState.update { HeartRateState.Unsupported }
            return
        }
        isStarted = true
        registrationCount++
        _heartRateState.update { HeartRateState.Measuring }
    }

    fun stop() {
        isStarted = false
        _heartRateState.update { HeartRateState.Waiting }
    }

    fun emitHeartRate(bpm: Double, timestampDuration: Long) {
        if (!isStarted) return
        if (bpm.isFinite() && bpm > 0) {
            _heartRateState.update { HeartRateState.Active(bpm) }
        }
    }

    fun setAvailability(available: Boolean) {
        if (!isStarted) return
        if (available) {
            if (_heartRateState.value !is HeartRateState.Active) {
                _heartRateState.update { HeartRateState.Measuring }
            }
        } else {
            _heartRateState.update { HeartRateState.Unavailable }
        }
    }

    fun onPermissionDenied() {
        _heartRateState.update { HeartRateState.PermissionRequired }
    }
}
