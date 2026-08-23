package com.example.a2.data.health

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

    fun start() {
        if (isStarted) return
        if (!hasPermission) {
            _heartRateState.update { HeartRateState.PermissionRequired }
            return
        }
        if (!isSupported) {
            _heartRateState.update { HeartRateState.Unsupported }
            return
        }
        isStarted = true
        _heartRateState.update { HeartRateState.Measuring }
    }

    fun stop() {
        isStarted = false
        _heartRateState.update { HeartRateState.Waiting }
    }

    fun emitHeartRate(bpm: Double, timestampDuration: Long) {
        if (!isStarted) return
        _heartRateState.update { HeartRateState.Active(bpm) }
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
}
